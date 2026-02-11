package com.logistics.power.engine.block.entity;

import com.logistics.LogisticsPower;
import com.logistics.core.lib.engine.RedstoneEngineSpec;
import com.logistics.core.lib.engine.storage.EngineSerde;
import com.logistics.core.lib.power.AbstractEngineBlockEntity;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

/**
 * Redstone Engine BE - minimal implementation.
 * All common logic is in {@link AbstractEngineBlockEntity}.
 */
public final class RedstoneEngineBlockEntity extends AbstractEngineBlockEntity<RedstoneEngineSpec> {

    private final RedstoneEngineSpec spec = new RedstoneEngineSpec();

    public final SidedEnergyProvider battery = new SidedEnergyProvider() {
        @Override
        public long getCapacity() {
            return RedstoneEngineSpec.CAPACITY;
        }

        @Override
        public long getMaxInsert(@Nullable Direction side) {
            return 0;
        }

        @Override
        public long getMaxExtract(@Nullable Direction side) {
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
    // Abstract method implementations
    // =========================

    @Override
    protected RedstoneEngineSpec getSpec() {
        return spec;
    }

    @Override
    public SidedEnergyProvider getBattery() {
        return battery;
    }

    @Override
    protected Direction getOutputDirection() {
        return getBlockState().getValue(RedstoneEngineBlock.FACING);
    }

    @Override
    protected boolean isRedstonePowered(Level level, BlockState state) {
        return state.getValue(RedstoneEngineBlock.POWERED);
    }

    // =========================
    // Hook method implementations
    // =========================

    @Override
    protected void burn() {
        // Redstone engine has no fuel
    }

    @Override
    protected boolean computeRunning(Level level, BlockState state) {
        return isRedstonePowered(level, state);
    }

    @Override
    protected boolean shouldStop(boolean running) {
        return !running;
    }

    @Override
    protected void onStop() {
        spec.getCycle().reset();
        spec.getProducer().reset();
    }

    @Override
    public boolean isRunning() {
        if (level == null) return false;
        return isRedstonePowered(level, getBlockState());
    }

    @Override
    public ProbeResult getProbeResult() {
        ProbeResult.Builder builder = ProbeResult.builder("Redstone Engine");

        builder.entry("Powered", level != null && isRedstonePowered(level, getBlockState()) ? "Yes" : "No");
        builder.entry("Running", isRunning() ? "Yes" : "No");
        builder.entry("Energy", String.format("%d / %d RF", spec.getEnergy().energy(), RedstoneEngineSpec.CAPACITY));
        builder.entry("Energy %", String.format("%.1f%%", spec.getEnergy().ratio() * 100));
        builder.entry("Temperature", String.format("%d°C", spec.getTemperature().celsius()));
        builder.entry("Temp Ratio", String.format("%.1f%%", spec.getTemperature().ratio() * 100));
        builder.entry("Piston Speed", String.format("%.3f", spec.pistonSpeed()));
        builder.entry("Cycle Progress", String.format("%.1f%%", spec.getCycle().progress() * 100));

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
                false
        );

        CompoundTag root = new CompoundTag();
        CompoundTag engine = EngineSerde.writeSnapshot(snap);
        engine.putInt("genCounter", spec.producer.getCounter());
        root.put(EngineSerde.KEY_ENGINE, engine);
        return root;
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
                false
        );

        CompoundTag tag = EngineSerde.writeSnapshot(snap);
        tag.putInt("genCounter", spec.producer.getCounter());
        view.store(EngineSerde.KEY_ENGINE, CompoundTag.CODEC, tag);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        view.read(EngineSerde.KEY_ENGINE, CompoundTag.CODEC).ifPresent(tag -> {
            EngineSerde.Snapshot snap = EngineSerde.readSnapshot(tag, RedstoneEngineSpec.MIN_TEMP);

            spec.getEnergy().set(snap.energy());
            spec.getTemperature().setCelsius(snap.heatC());
            spec.getCycle().setProgress(snap.progress());

            int ctr = tag.getInt("genCounter").orElse(0);
            spec.producer.setCounter(ctr);

            battery.setEnergy(spec.getEnergy().energy());
        });
    }
}