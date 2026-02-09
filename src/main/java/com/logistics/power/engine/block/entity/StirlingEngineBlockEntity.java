package com.logistics.power.engine.block.entity;

import com.logistics.LogisticsPower;
import com.logistics.core.lib.engine.StirlingEngineSpec;
import com.logistics.core.lib.engine.state.EngineCycleState;
import com.logistics.core.lib.engine.storage.EngineSerde;
import com.logistics.core.lib.power.SidedEnergyProvider;
import com.logistics.power.engine.block.StirlingEngineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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
public final class StirlingEngineBlockEntity extends BlockEntity {

    private final StirlingEngineSpec spec = new StirlingEngineSpec();
    private boolean overheated = false;

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
            // TODO: tryIgniteFuelFromInventory();
            // If ignition succeeds, isBurning should become true.
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
        engine.putDouble("burnProgress", spec.fuel.getRatio());
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
    }
}
