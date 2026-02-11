package com.logistics.power.engine.block.entity;

import com.logistics.LogisticsPower;
import com.logistics.core.lib.engine.state.EngineCycleState;
import com.logistics.core.lib.engine.state.HeatStage;
import com.logistics.core.lib.engine.storage.EngineSerde;
import com.logistics.core.lib.engine.CreativeEngineSpec;
import com.logistics.core.lib.power.SidedEnergyProvider;
import com.logistics.core.lib.support.ProbeResult;
import com.logistics.power.engine.block.CreativeEngineBlock;
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
 * Creative Engine BE (thin adapter):
 * - delegates behavior to {@link CreativeEngineSpec}
 * - handles Minecraft/TR energy IO + persistence + output direction
 */
public final class CreativeEngineBlockEntity extends BlockEntity {

    private final CreativeEngineSpec spec = new CreativeEngineSpec();
    private boolean overheated = false;

    private long tickGeneration = 0;

    // TR energy container (authoritative for external IO).
    // We mirror spec.energy <-> battery each tick and on load.
    public final SidedEnergyProvider battery = new SidedEnergyProvider() {
        @Override
        public long getCapacity() {
            return CreativeEngineSpec.CAPACITY;
        }

        @Override
        public long getMaxInsert(@Nullable Direction side) {
            return 0; // Creative engine cannot accept energy externally
        }

        @Override
        public long getMaxExtract(@Nullable Direction side) {
            return (side != null && side == getOutputDirection())
                    ? spec.getCurrentOutputLevel()
                    : 0L;
        }

        @Override
        protected void onFinalCommit() {
            setChanged();
        }
    };

    public CreativeEngineBlockEntity(BlockPos pos, BlockState state) {
        super(LogisticsPower.ENTITY.CREATIVE_ENGINE_BLOCK_ENTITY, pos, state);
    }

    // =========================
    // Tick (server side)
    // =========================

    public static void serverTick(Level level, BlockPos pos, BlockState state, CreativeEngineBlockEntity be) {
        if (level.isClientSide()) return;
        be.tickServer(level, state);
    }

    private void tickServer(Level level, BlockState state) {
        // 0) Mirror TR energy into pure state
        spec.energy.set(battery.getEnergy());

        // 1) Pre-tick engine-specific logic
        burn();

        // 2) Determine running state
        boolean running = computeRunning(level, state);

        // 3) Producer
        tickGeneration = spec.producer.tick(running, spec.energy);

        // 4) Drain
        spec.drain.tick(running, spec.energy);

        // 5) Thermal
        spec.thermal.update(spec.energy, spec.temp);

        // 5.5) Update overheat status
        if (!overheated && spec.canOverheat()) {
            overheated = spec.energy.ratio() >= 1.0;
        }

        // 6) Check if should stop
        if (shouldStop(running)) {
            onStop();
            battery.setEnergy(spec.energy.energy());
            syncRenderToClient();
            setChanged();
            return;
        }

        // 7) Advance piston cycle
        EngineCycleState.AdvanceResult res = spec.cycle.advance(spec.pistonSpeed());

        // 8) Output
        long maxSend = spec.output.maxSend(spec.energy, res);
        if (maxSend > 0) {
            long sent = sendEnergy(level, maxSend);
            if (sent > 0) {
                spec.energy.remove(sent);
            }
        }

        // 9) Mirror pure energy back into TR container
        battery.setEnergy(spec.energy.energy());

        // 10) Sync render
        syncRenderToClient();
        setChanged();
    }

    // =========================
    // Engine-specific hooks
    // =========================

    /** Pre-tick logic (e.g., fuel management). Called before main tick logic. */
    protected void burn() {
        // Creative engine has no pre-tick logic
    }

    /** Computes whether the engine is running this tick. */
    protected boolean computeRunning(Level level, BlockState state) {
        return isRedstonePowered(level, state);
    }

    /** Checks if the engine should stop and skip cycle/output. */
    protected boolean shouldStop(boolean running) {
        return !running;
    }

    /** Called when the engine stops. Resets state as needed. */
    protected void onStop() {
        spec.cycle.reset();
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
    // Output level control
    // =========================

    /**
     * Cycles to the next output level (doubles the output rate).
     * Wraps around to minimum when maximum is exceeded.
     *
     * @return the new output rate in RF/t
     */
    public long cycleOutputLevel() {
        int currentIndex = spec.getOutputLevelIndex();
        int nextIndex = (currentIndex + 1) % CreativeEngineSpec.OUTPUT_LEVELS.length;
        spec.setOutputLevelIndex(nextIndex);

        setChanged();

        // Sync to clients so renderer can update piston speed
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }

        return spec.getCurrentOutputLevel();
    }

    // =========================
    // Probe support for debugging
    // =========================

    public ProbeResult getProbeResult() {
        ProbeResult.Builder builder = ProbeResult.builder("Creative Engine");

        // Power state
        builder.entry("Powered", level != null && isRedstonePowered(level, getBlockState()) ? "Yes" : "No");
        builder.entry("Running", isRunning() ? "Yes" : "No");

        // Output level
        builder.entry("Output Level", spec.getCurrentOutputLevel() + " RF/t");

        // Energy state
        builder.entry("Energy", String.format("%d / %d RF", spec.energy.energy(), CreativeEngineSpec.CAPACITY));
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

        // Send output level index for rendering
        engine.putInt("outputLevelIndex", spec.getOutputLevelIndex());

        root.put(EngineSerde.KEY_ENGINE, engine);
        return root;
    }

    // =========================
    // Redstone + Facing
    // =========================

    private static boolean isRedstonePowered(Level level, BlockState state) {
        return state.getValue(CreativeEngineBlock.POWERED);
    }

    private Direction getOutputDirection() {
        return getBlockState().getValue(CreativeEngineBlock.FACING);
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

        // Creative-specific state: output level index
        tag.putInt("outputLevelIndex", spec.getOutputLevelIndex());

        view.store(EngineSerde.KEY_ENGINE, CompoundTag.CODEC, tag);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        view.read(EngineSerde.KEY_ENGINE, CompoundTag.CODEC).ifPresent(tag -> {
            EngineSerde.Snapshot snap = EngineSerde.readSnapshot(tag, CreativeEngineSpec.MIN_TEMP);

            spec.energy.set(snap.energy());
            spec.temp.setCelsius(snap.heatC());
            spec.cycle.setProgress(snap.progress());

            int outputLevelIndex = tag.getInt("outputLevelIndex").orElse(0);
            spec.setOutputLevelIndex(outputLevelIndex);

            // Mirror into TR container so external IO sees correct state immediately
            battery.setEnergy(spec.energy.energy());
        });
    }
}
