package com.logistics.power.engine.block.entity;

import com.logistics.LogisticsPower;
import com.logistics.core.lib.engine.StirlingEngineSpec;
import com.logistics.core.lib.engine.fuel.FuelSource;
import com.logistics.core.lib.engine.storage.EngineSerde;
import com.logistics.core.lib.power.AbstractEngineBlockEntity;
import com.logistics.core.lib.power.SidedEnergyProvider;
import com.logistics.core.lib.support.ProbeResult;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

/**
 * Stirling Engine BE - minimal implementation with fuel inventory.
 * All common logic is in {@link AbstractEngineBlockEntity}.
 */
public final class StirlingEngineBlockEntity extends AbstractEngineBlockEntity<StirlingEngineSpec>
        implements Container, ExtendedScreenHandlerFactory<BlockPos> {

    private final StirlingEngineSpec spec = new StirlingEngineSpec();
    private ItemStack fuelStack = ItemStack.EMPTY;

    private final FuelSource inventoryFuelSource = new FuelSource() {
        @Override
        public int getNextFuelBurnTime() {
            if (fuelStack.isEmpty() || level == null) return 0;
            return level.fuelValues().burnDuration(fuelStack);
        }

        @Override
        public void consumeFuel() {
            fuelStack.shrink(1);
            if (fuelStack.isEmpty()) {
                fuelStack = ItemStack.EMPTY;
            }
            setChanged();
        }
    };

    public final SidedEnergyProvider battery = new SidedEnergyProvider() {
        @Override
        public long getCapacity() {
            return StirlingEngineSpec.CAPACITY;
        }

        @Override
        public long getMaxInsert(@Nullable Direction side) {
            return 0;
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
    // Abstract method implementations
    // =========================

    @Override
    protected StirlingEngineSpec getSpec() {
        return spec;
    }

    @Override
    public SidedEnergyProvider getBattery() {
        return battery;
    }

    @Override
    protected Direction getOutputDirection() {
        return getBlockState().getValue(StirlingEngineBlock.FACING);
    }

    @Override
    protected boolean isRedstonePowered(Level level, BlockState state) {
        return state.getValue(StirlingEngineBlock.POWERED);
    }

    // =========================
    // Hook method implementations
    // =========================

    @Override
    protected void burn() {
        boolean shouldIgnite = !getOverheated();
        spec.tickFuel(shouldIgnite, inventoryFuelSource);
    }

    @Override
    protected boolean computeRunning(Level level, BlockState state) {
        boolean powered = isRedstonePowered(level, state);
        return powered && spec.fuel.isBurning() && !getOverheated();
    }

    @Override
    protected boolean shouldStop(boolean running) {
        return getOverheated() || !running;
    }

    @Override
    protected void onStop() {
        spec.getCycle().reset();
        spec.getProducer().reset();
        if (getOverheated()) {
            spec.fuel.reset();
        }
    }

    @Override
    public boolean isRunning() {
        if (level == null) return false;
        boolean powered = isRedstonePowered(level, getBlockState());
        return powered && spec.fuel.isBurning() && !getOverheated();
    }

    @Override
    public ProbeResult getProbeResult() {
        ProbeResult.Builder builder = ProbeResult.builder("Stirling Engine");

        builder.entry("Powered", level != null && isRedstonePowered(level, getBlockState()) ? "Yes" : "No");
        builder.entry("Running", isRunning() ? "Yes" : "No");
        builder.entry("Burning", spec.fuel.isBurning() ? "Yes" : "No");
        if (spec.fuel.isBurning()) {
            builder.entry("Burn Progress", String.format("%.1f%%", spec.fuel.getRatio() * 100));
            builder.entry("Ticks Left", String.valueOf(spec.fuel.getBurnTicks()));
        }
        builder.entry("Energy", String.format("%d / %d (%d) RF",
            spec.getEnergy().energy(), StirlingEngineSpec.CAPACITY, getTickGeneration()));
        builder.entry("Energy %", String.format("%.1f%%", spec.getEnergy().ratio() * 100));

        if (getOverheated()) {
            builder.warning("OVERHEATED!");
        }

        return builder.build();
    }

    // =========================
    // Helpers
    // =========================

    public boolean isOutputDirection(@Nullable Direction direction) {
        return direction == getOutputDirection();
    }

    // =========================
    // Update packets
    // =========================

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        EngineSerde.Snapshot snap = new EngineSerde.Snapshot(
                spec.getEnergy().energy(),
                spec.getTemperature().celsius(),
                spec.getCycle().progress(),
                getOverheated()
        );

        CompoundTag root = new CompoundTag();
        CompoundTag engine = EngineSerde.writeSnapshot(snap);
        engine.putInt("burnTicks", spec.fuel.getBurnTicks());
        root.put(EngineSerde.KEY_ENGINE, engine);
        return root;
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
        return level != null && level.fuelValues().isFuel(stack);
    }

    // =========================
    // Screen handler
    // =========================

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

    // =========================
    // Persistence
    // =========================

    @Override
    protected void saveAdditional(ValueOutput view) {
        EngineSerde.Snapshot snap = new EngineSerde.Snapshot(
                spec.getEnergy().energy(),
                spec.getTemperature().celsius(),
                spec.getCycle().progress(),
                getOverheated()
        );

        CompoundTag tag = EngineSerde.writeSnapshot(snap);
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

            spec.getEnergy().set(snap.energy());
            spec.getTemperature().setCelsius(snap.heatC());
            spec.getCycle().setProgress(snap.progress());
            setOverheated(snap.overheated());

            Double accumulator = tag.getDouble("accumulator").orElse(0.0);
            int fuelTicks = tag.getInt("fuelTicks").orElse(0);
            int burnTicks = tag.getInt("burnTicks").orElse(0);

            spec.producer.setAccumulator(accumulator);
            spec.fuel.setFuelTicks(fuelTicks);
            spec.fuel.setBurnTicks(burnTicks);

            battery.setEnergy(spec.getEnergy().energy());
        });

        view.read("Fuel", ItemStack.CODEC).ifPresent(stack -> {
            fuelStack = stack;
        });
    }
}