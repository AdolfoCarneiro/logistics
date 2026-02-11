package com.logistics.core.lib.power;

import com.logistics.core.lib.engine.EngineSpec;
import com.logistics.core.lib.engine.state.EngineCycleState;
import com.logistics.core.lib.engine.state.HeatStage;
import com.logistics.core.lib.support.ProbeResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.EnergyStorageUtil;

/**
 * Minimal abstract base for engine block entities.
 * Contains only the truly common tick logic and helper methods.
 */
public abstract class AbstractEngineBlockEntity<S extends EngineSpec> extends BlockEntity {

    private boolean overheated = false;
    private long tickGeneration = 0;
    private int renderSyncCooldown = 0;

    public AbstractEngineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // Abstract getters - engines provide their spec and battery
    protected abstract S getSpec();
    public abstract SidedEnergyProvider getBattery();
    protected abstract Direction getOutputDirection();
    protected abstract boolean isRedstonePowered(Level level, BlockState state);

    // Hook methods - engines implement for custom behavior
    protected abstract void burn();
    protected abstract boolean computeRunning(Level level, BlockState state);
    protected abstract boolean shouldStop(boolean running);
    protected abstract void onStop();
    public abstract boolean isRunning();
    public abstract ProbeResult getProbeResult();

    // Static tick entry point
    public static <T extends AbstractEngineBlockEntity<?>> void serverTick(
            Level level, BlockPos pos, BlockState state, T be) {
        if (level.isClientSide()) return;
        be.tickServer(level, state);
    }

    // Unified tick logic - identical across all engines
    protected void tickServer(Level level, BlockState state) {
        S spec = getSpec();
        SidedEnergyProvider battery = getBattery();

        spec.getEnergy().set(battery.getEnergy());
        burn();

        boolean running = computeRunning(level, state);
        tickGeneration = spec.getProducer().tick(running, spec.getEnergy());
        spec.getDrain().tick(running, spec.getEnergy());
        spec.getThermal().update(spec.getEnergy(), spec.getTemperature());

        if (!overheated && spec.canOverheat()) {
            overheated = spec.getEnergy().ratio() >= 1.0;
        }

        if (shouldStop(running)) {
            onStop();
            battery.setEnergy(spec.getEnergy().energy());
            syncRenderToClient();
            setChanged();
            return;
        }

        EngineCycleState.AdvanceResult res = spec.getCycle().advance(spec.pistonSpeed());
        long maxSend = spec.getOutput().maxSend(spec.getEnergy(), res);
        if (maxSend > 0) {
            long sent = sendEnergy(level, maxSend);
            if (sent > 0) {
                spec.getEnergy().remove(sent);
            }
        }

        battery.setEnergy(spec.getEnergy().energy());
        syncRenderToClient();
        setChanged();
    }

    private long sendEnergy(Level level, long maxSend) {
        Direction out = getOutputDirection();
        BlockPos targetPos = worldPosition.relative(out);
        EnergyStorage target = EnergyStorage.SIDED.find(level, targetPos, out.getOpposite());
        if (target == null) return 0L;

        EnergyStorage source = getBattery().getSideStorage(out);
        long before = getBattery().getEnergy();
        EnergyStorageUtil.move(source, target, maxSend, null);
        long after = getBattery().getEnergy();
        return Math.max(0L, before - after);
    }

    // Rendering accessors
    public float getPistonProgress01() { return getSpec().getCycle().progress(); }
    public float getPistonSpeed() { return getSpec().pistonSpeed(); }
    public HeatStage getHeatStage() { return getSpec().stage(); }
    public long getTemperatureC() { return getSpec().getTemperature().celsius(); }
    public boolean isOverheated() { return overheated; }
    public boolean canOverheat() { return getSpec().canOverheat(); }

    public void clearOverheated() {
        if (!overheated) return;
        overheated = false;
        setChanged();
        if (level != null && !level.isClientSide()) {
            BlockState st = getBlockState();
            level.sendBlockUpdated(worldPosition, st, st, Block.UPDATE_CLIENTS);
        }
    }

    private void syncRenderToClient() {
        if (level == null || level.isClientSide()) return;
        if (--renderSyncCooldown > 0) return;
        renderSyncCooldown = 4;
        BlockState st = getBlockState();
        level.sendBlockUpdated(worldPosition, st, st, Block.UPDATE_CLIENTS);
    }

    // Protected accessors for subclasses
    protected boolean getOverheated() { return overheated; }
    protected void setOverheated(boolean overheated) { this.overheated = overheated; }
    protected long getTickGeneration() { return tickGeneration; }
}
