package com.logistics.power.engine.block.entity;

import com.logistics.LogisticsPower;
import com.logistics.core.lib.engine.state.EngineCycleState;
import com.logistics.core.lib.engine.state.HeatStage;
import com.logistics.core.lib.engine.storage.EngineSerde;
import com.logistics.core.lib.engine.RedstoneEngineSpec;
import com.logistics.core.lib.power.SidedEnergyProvider;
import com.logistics.core.lib.support.ProbeResult;
import com.logistics.power.engine.block.RedstoneEngineBlock;
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
 * Redstone Engine BE (thin adapter):
 * - delegates behavior to {@link RedstoneEngineSpec}
 * - handles Minecraft/TR energy IO + persistence + output direction
 */
public final class RedstoneEngineBlockEntity extends BlockEntity {

    private final RedstoneEngineSpec spec = new RedstoneEngineSpec();

    // TR energy container (authoritative for external IO).
    // We mirror spec.energy <-> battery each tick and on load.
    public final SidedEnergyProvider battery = new SidedEnergyProvider() {
        @Override
        public long getCapacity() {
            return RedstoneEngineSpec.CAPACITY;
        }

        @Override
        public long getMaxInsert(@Nullable Direction side) {
            return 0; // Redstone engine cannot accept energy externally
        }

        @Override
        public long getMaxExtract(@Nullable Direction side) {
            // Only allow extracting on the output face; pulse amount is gated by cycle, not this limit.
            return (side != null && side == getOutputDirection())
                    ? RedstoneEngineSpec.RF_ON_OUTPUT
                    : 0L;
        }

        @Override
        protected void onFinalCommit() {
            setChanged();
        }
    };

    public RedstoneEngineBlockEntity(BlockPos pos, BlockState state) {
        super(LogisticsPower.ENTITY.REDSTONE_ENGINE_BLOCK_ENTITY, pos, state);
    }

    // =========================
    // Tick (server side)
    // =========================

    public static void serverTick(Level level, BlockPos pos, BlockState state, RedstoneEngineBlockEntity be) {
        if (level.isClientSide()) return;
        be.tickServer(level, state);
    }

    private void tickServer(Level level, BlockState state) {
        // 0) Mirror TR energy into pure state
        spec.energy.set(battery.getEnergy());

        boolean powered = isRedstonePowered(level, state);

        // 1) Producer: +10 RF every 16 ticks while powered
        spec.producer.tick(powered, spec.energy);

        // 2) Drain: -10 RF every tick when unpowered
        spec.drain.tick(powered, spec.energy);

        // 3) Thermal: temp proportional to stored energy ratio
        spec.thermal.update(spec.energy, spec.temp);

        // 4) If not powered: reset cycle (your requirement) and reset cadence
        if (!powered) {
            spec.cycle.reset();
            spec.producer.reset();

            // Mirror energy back to TR container (in case producer/thermal touched it)
            battery.setEnergy(spec.energy.energy());
            syncRenderToClient();
            setChanged();
            return;
        }

        // 5) Advance piston cycle based on temp ratio -> speed
        EngineCycleState.AdvanceResult res = spec.cycle.advance(spec.pistonSpeed());

        // 6) Output: only on full-extension event (crossing 0.5)
        if (res.crossedHalf()) {
            long maxSend = Math.min(RedstoneEngineSpec.RF_ON_OUTPUT, spec.energy.energy());
            if (maxSend > 0) {
                long sent = sendEnergy(level, state, maxSend);
                if (sent > 0) {
                    spec.energy.remove(sent);
                }
            }
        }

        // 7) Mirror pure energy back into TR container
        battery.setEnergy(spec.energy.energy());

        // 8) sync visual stage
        syncRenderToClient();
        setChanged();
    }

    /**
     * Moves up to maxSend RF out of the engine to the neighbor on the output face.
     * Returns the amount actually moved.
     */
    private long sendEnergy(Level level, BlockState state, long maxSend) {
        Direction out = getOutputDirection();
        BlockPos targetPos = worldPosition.relative(out);

        EnergyStorage target = EnergyStorage.SIDED.find(level, targetPos, out.getOpposite());
        if (target == null) return 0L;

        EnergyStorage source = battery.getSideStorage(out);

        // EnergyStorageUtil.move returns void in some versions; if yours returns moved, use it.
        // If it returns void, measure moved by sampling before/after.
        long before = battery.getEnergy();
        EnergyStorageUtil.move(source, target, maxSend, null);
        long after = battery.getEnergy();
        long moved = Math.max(0L, before - after);

        return moved;
    }

    // =========================
    // Client rendering accessors
    // =========================

    /** Client can use this for smooth interpolation. */
    public float getPistonProgress01() {
        return spec.cycle.progress();
    }

    public float getPistonSpeed() {
        return spec.pistonSpeed();
    }

    public HeatStage getHeatStage() {
        return spec.stage();
    }

    public boolean isOverheated() {
        return false;
    }

    public long getTemperatureC() {
        return spec.temp.celsius();
    }

    public boolean isRunning() {
        if (level == null) return false;
        return isRedstonePowered(level, getBlockState());
    }

    // =========================
    // Probe support for debugging
    // =========================

    public ProbeResult getProbeResult() {
        ProbeResult.Builder builder = ProbeResult.builder("Redstone Engine");

        // Power state
        builder.entry("Powered", level != null && isRedstonePowered(level, getBlockState()) ? "Yes" : "No");
        builder.entry("Running", isRunning() ? "Yes" : "No");

        // Energy state
        builder.entry("Energy", String.format("%d / %d RF", spec.energy.energy(), RedstoneEngineSpec.CAPACITY));
        builder.entry("Energy %", String.format("%.1f%%", spec.energy.ratio() * 100));

        // Temperature
        builder.entry("Temperature", String.format("%d°C", spec.temp.celsius()));
        builder.entry("Temp Ratio", String.format("%.1f%%", spec.temp.ratio() * 100));

        // Motion
        builder.entry("Piston Speed", String.format("%.3f", spec.pistonSpeed()));
        builder.entry("Cycle Progress", String.format("%.1f%%", spec.cycle.progress() * 100));

        return builder.build();
    }

    // =========================
    // BE update packets for render data
    // =========================
    private int renderSyncCooldown = 0;

    private void syncRenderToClient() {
        if (level == null || level.isClientSide()) return;

        // throttle: e.g. 5x/sec
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
        // Use the same structure as disk persistence so the client can apply it via the normal load path.
        EngineSerde.Snapshot snap = new EngineSerde.Snapshot(
                spec.energy.energy(),
                spec.temp.celsius(),
                spec.cycle.progress(),
                false
        );

        CompoundTag root = new CompoundTag();
        CompoundTag engine = EngineSerde.writeSnapshot(snap);

        // Redstone-specific cadence is not strictly needed for rendering, but harmless if you want it client-side.
        engine.putInt("genCounter", spec.producer.getCounter());

        root.put(EngineSerde.KEY_ENGINE, engine);
        return root;
    }

    // =========================
    // Redstone + Facing
    // =========================

    private static boolean isRedstonePowered(Level level, BlockState state) {
        return state.getValue(RedstoneEngineBlock.POWERED);
    }

    private Direction getOutputDirection() {
        return getBlockState().getValue(RedstoneEngineBlock.FACING);
    }

    public boolean isOutputDirection(@Nullable Direction direction) {
        return direction == getOutputDirection();
    }

    // =========================
    // Persistence
    // =========================

    @Override
    protected void saveAdditional(ValueOutput view) {
        // Common engine snapshot
        EngineSerde.Snapshot snap = new EngineSerde.Snapshot(
                spec.energy.energy(),
                spec.temp.celsius(),
                spec.cycle.progress(),
                /*overheated*/ false
        );

        CompoundTag tag = EngineSerde.writeSnapshot(snap);

        // Redstone-specific cadence state
        tag.putInt("genCounter", spec.producer.getCounter());

        view.store(EngineSerde.KEY_ENGINE, CompoundTag.CODEC, tag);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        view.read(EngineSerde.KEY_ENGINE, CompoundTag.CODEC).ifPresent(tag -> {
            EngineSerde.Snapshot snap = EngineSerde.readSnapshot(tag, RedstoneEngineSpec.MIN_TEMP);

            spec.energy.set(snap.energy());
            spec.temp.setCelsius(snap.heatC());
            spec.cycle.setProgress(snap.progress());

            int ctr = tag.getInt("genCounter").orElse(0);
            spec.producer.setCounter(ctr);

            // Mirror into TR container so external IO sees correct state immediately
            battery.setEnergy(spec.energy.energy());
        });
    }
}
