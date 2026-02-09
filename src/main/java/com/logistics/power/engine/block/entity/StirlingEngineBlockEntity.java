package com.logistics.power.engine.block.entity;

import com.logistics.LogisticsPower;
import com.logistics.core.lib.engine.StirlingEngineSpec;
import com.logistics.core.lib.engine.state.EngineCycleState;
import com.logistics.core.lib.engine.storage.EngineSerde;
import com.logistics.core.lib.power.SidedEnergyProvider;
import com.logistics.power.engine.block.StirlingEngineBlock;
import com.logistics.power.engine.ui.StirlingEngineScreenHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.EnergyStorageUtil;

/**
 * Stirling Engine BE (thin adapter):
 * - delegates behavior to {@link StirlingEngineSpec}
 * - handles Minecraft/TR energy IO + persistence + output direction
 */
public final class StirlingEngineBlockEntity extends BlockEntity implements Container, ExtendedScreenHandlerFactory<BlockPos> {

    private final StirlingEngineSpec spec = new StirlingEngineSpec();
    private boolean overheated = false;

    // 1-slot fuel inventory (slot 0)
    private ItemStack fuelStack = ItemStack.EMPTY;

    // TR energy container (authoritative for external IO).
    // We mirror spec.energy <-> battery each tick and on load.
    public final SidedEnergyProvider battery = new SidedEnergyProvider() {
        @Override
        public long getCapacity() {
            return StirlingEngineSpec.CAPACITY;
        }

        @Override
        public long getMaxInsert(@Nullable Direction side) {
            return 0; // Stirling engine cannot accept energy externally
        }

        @Override
        public long getMaxExtract(@Nullable Direction side) {
            return (side != null && side == getOutputDirection())
                    ? StirlingEngineSpec.MAX_OUTPUT
                    : 0L;
        }

        @Override
        protected void onFinalCommit() {
            setChanged();
        }
    };

    public StirlingEngineBlockEntity(BlockPos pos, BlockState state) {
        super(LogisticsPower.ENTITY.STIRLING_ENGINE_BLOCK_ENTITY, pos, state);
    }

    // =========================
    // Tick (server side)
    // =========================

    public static void serverTick(Level level, BlockPos pos, BlockState state, StirlingEngineBlockEntity be) {
        if (level.isClientSide()) return;
        be.tickServer(level, state);
    }

    private void tickServer(Level level, BlockState state) {
        // 0) Mirror TR energy into pure state
        spec.energy.set(battery.getEnergy());

        boolean powered = isRedstonePowered(level, state);

        // Fuel burn is independent of redstone once ignited.
        // Tick it down whenever it is currently burning.
        if (spec.fuel.isBurning()) {
            spec.fuel.tickDown();
        }

        boolean isBurning = spec.fuel.isBurning();

        // Only ignite new fuel if we're powered and not overheated.
        if (powered && !isBurning && !overheated) {
            tryIgniteFuelFromInventory();
            isBurning = spec.fuel.isBurning();
        }

        // Running for production/output requires redstone + burning fuel + not overheated.
        boolean running = powered && isBurning && !overheated;

        // 1) Producer: increase RF
        spec.producer.tick(running, spec.energy);

        // 2) Drain: drains energy while NOT running (per your drain model)
        spec.drain.tick(running, spec.energy);

        // 3) Thermal: temp proportional to stored energy ratio
        spec.thermal.update(spec.energy, spec.temp);

        // NOTE: If you later decouple heat/energy, switch this to spec.temp.ratio().
        overheated = spec.energy.ratio() >= 1.0;

        // 4) Overheat latch: clear remaining fuel and stop running.
        if (overheated) {
            spec.cycle.reset();
            spec.producer.reset();
            spec.fuel.reset();

            battery.setEnergy(spec.energy.energy());
            syncRenderToClient();
            setChanged();
            return;
        }

        // 5) If not running (unpowered or out of fuel): stop motion/production.
        // Do NOT clear fuel ticks (it can continue burning to completion).
        if (!running) {
            spec.cycle.reset();
            spec.producer.reset();

            battery.setEnergy(spec.energy.energy());
            syncRenderToClient();
            setChanged();
            return;
        }

        // 6) Advance piston cycle based on temp ratio -> speed
        EngineCycleState.AdvanceResult res = spec.cycle.advance(spec.pistonSpeed());

        // 7) Output (continuous): up to MAX_OUTPUT per tick.
        // If you want proportional output, swap this for spec.output.maxSend(spec.energy, res).
        long maxSend = Math.min(StirlingEngineSpec.MAX_OUTPUT, spec.energy.energy());
        if (maxSend > 0) {
            long sent = sendEnergy(level, maxSend);
            if (sent > 0) {
                spec.energy.remove(sent);
            }
        }

        // 8) Mirror pure energy back into TR container
        battery.setEnergy(spec.energy.energy());

        // 9) sync render
        syncRenderToClient();
        setChanged();
    }

    /**
     * Moves up to maxSend RF out of the engine to the neighbor on the output face.
     * Returns the amount actually moved.
     */
    private long sendEnergy(Level level, long maxSend) {
        Direction out = getOutputDirection();
        BlockPos targetPos = worldPosition.relative(out);

        EnergyStorage target = EnergyStorage.SIDED.find(level, targetPos, out.getOpposite());
        if (target == null) return 0L;

        EnergyStorage source = battery.getSideStorage(out);

        long before = battery.getEnergy();
        EnergyStorageUtil.move(source, target, maxSend, null);
        long after = battery.getEnergy();
        return Math.max(0L, before - after);
    }

    /**
     * Attempts to ignite fuel from the inventory.
     * If successful, consumes one item and starts burning.
     */
    private void tryIgniteFuelFromInventory() {
        if (fuelStack.isEmpty()) return;

        assert level != null;
        int burnTime = level.fuelValues().burnDuration(fuelStack);
        if (burnTime <= 0) return;

        // Start burning
        spec.fuel.ignite(burnTime);

        // Consume one item
        fuelStack.shrink(1);
        if (fuelStack.isEmpty()) {
            fuelStack = ItemStack.EMPTY;
        }

        setChanged();
    }

    // =========================
    // Client rendering accessors
    // =========================

    public float getPistonProgress01() {
        return spec.cycle.progress();
    }

    public float getPistonSpeed() {
        return spec.pistonSpeed();
    }

    public long getTemperatureC() {
        return spec.temp.celsius();
    }

    public boolean isRunning() {
        if (level == null) return false;
        boolean powered = isRedstonePowered(level, getBlockState());
        return powered && spec.fuel.isBurning() && !overheated;
    }

    // =========================
    // BE update packets for render data
    // =========================

    private int renderSyncCooldown = 0;

    private void syncRenderToClient() {
        if (level == null || level.isClientSide()) return;

        if (--renderSyncCooldown > 0) return;
        renderSyncCooldown = 4;

        BlockState st = getBlockState();
        level.sendBlockUpdated(worldPosition, st, st, Block.UPDATE_CLIENTS);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        EngineSerde.Snapshot snap = new EngineSerde.Snapshot(
                spec.energy.energy(),
                spec.temp.celsius(),
                spec.cycle.progress(),
                overheated
        );

        CompoundTag root = new CompoundTag();
        CompoundTag engine = EngineSerde.writeSnapshot(snap);

        // Send burn ticks to client so isRunning() works for animation
        engine.putInt("burnTicks", spec.fuel.getBurnTicks());

        root.put(EngineSerde.KEY_ENGINE, engine);
        return root;
    }

    // =========================
    // Redstone + Facing
    // =========================

    private static boolean isRedstonePowered(Level level, BlockState state) {
        return state.getValue(StirlingEngineBlock.POWERED);
    }

    private Direction getOutputDirection() {
        return getBlockState().getValue(StirlingEngineBlock.FACING);
    }

    public boolean isOutputDirection(@Nullable Direction direction) {
        return direction == getOutputDirection();
    }

    // =========================
    // Inventory (1-slot fuel)
    // =========================

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return fuelStack.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot == 0 ? fuelStack : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot != 0 || amount <= 0 || fuelStack.isEmpty()) return ItemStack.EMPTY;

        ItemStack result = fuelStack.split(amount);
        if (fuelStack.isEmpty()) fuelStack = ItemStack.EMPTY;

        setChanged();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot != 0) return ItemStack.EMPTY;

        ItemStack result = fuelStack;
        fuelStack = ItemStack.EMPTY;
        return result;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot != 0) return;
        fuelStack = (stack == null) ? ItemStack.EMPTY : stack;
        if (!fuelStack.isEmpty() && fuelStack.getCount() > getMaxStackSize()) {
            fuelStack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public boolean stillValid(Player player) {
        // Standard distance check for containers.
        if (level == null) return false;
        if (level.getBlockEntity(worldPosition) != this) return false;
        return player.distanceToSqr(
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5
        ) <= 64.0;
    }

    @Override
    public void clearContent() {
        fuelStack = ItemStack.EMPTY;
        setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot != 0) return false;
        if (stack == null || stack.isEmpty()) return true;
        // Only accept items that are valid fuels.
        // (This is conservative; ignition will also validate burn time.)
        return level != null && level.fuelValues().isFuel(stack);
    }

    // =========================
    // Persistence
    // =========================

    @Override
    protected void saveAdditional(ValueOutput view) {
        EngineSerde.Snapshot snap = new EngineSerde.Snapshot(
                spec.energy.energy(),
                spec.temp.celsius(),
                spec.cycle.progress(),
                overheated
        );

        CompoundTag tag = EngineSerde.writeSnapshot(snap);

        // Stirling-specific state
        tag.putDouble("accumulator", spec.producer.getAccumulator());
        tag.putInt("fuelTicks", spec.fuel.getFuelTicks());
        tag.putInt("burnTicks", spec.fuel.getBurnTicks());

        view.store(EngineSerde.KEY_ENGINE, CompoundTag.CODEC, tag);
        view.store("Fuel", ItemStack.CODEC, fuelStack);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        view.read(EngineSerde.KEY_ENGINE, CompoundTag.CODEC).ifPresent(tag -> {
            EngineSerde.Snapshot snap = EngineSerde.readSnapshot(tag, StirlingEngineSpec.MIN_TEMP);

            spec.energy.set(snap.energy());
            spec.temp.setCelsius(snap.heatC());
            spec.cycle.setProgress(snap.progress());
            overheated = snap.overheated();

            Double accumulator = tag.getDouble("accumulator").orElse(0.0);
            int fuelTicks = tag.getInt("fuelTicks").orElse(0);
            int burnTicks = tag.getInt("burnTicks").orElse(0);

            spec.producer.setAccumulator(accumulator);
            spec.fuel.setFuelTicks(fuelTicks);
            spec.fuel.setBurnTicks(burnTicks);

            battery.setEnergy(spec.energy.energy());
        });

        view.read("Fuel", ItemStack.CODEC).ifPresent(stack -> {
            fuelStack = stack;
        });
    }

    public static final int PROPERTY_BURN_RATIO = 0;
    public static final int PROPERTY_COUNT = 1;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            if (index == PROPERTY_BURN_RATIO) {
                return (int)(spec.fuel.getRatio() * 1000.0);
            }
            return 0;
        }

        @Override
        public void set(int index, int value) {
            // client-side only; no-op
        }

        @Override
        public int getCount() {
            return PROPERTY_COUNT;
        }
    };

    public ContainerData getData() {
        return data;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.logistics.power.stirling_engine");
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return this.worldPosition;
    }

    @Override
    public @org.jspecify.annotations.Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new StirlingEngineScreenHandler(i, inventory, this, data);
    }
}
