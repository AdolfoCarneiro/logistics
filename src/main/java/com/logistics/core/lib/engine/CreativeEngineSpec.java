package com.logistics.core.lib.engine;

import com.logistics.core.lib.engine.drain.PassiveDrain;
import com.logistics.core.lib.engine.heat.FixedThermalModel;
import com.logistics.core.lib.engine.output.ConstantOutput;
import com.logistics.core.lib.engine.producer.InfiniteProducer;
import com.logistics.core.lib.engine.state.EngineCycleState;
import com.logistics.core.lib.engine.state.HeatStage;
import com.logistics.core.lib.engine.state.PistonSpeedTable;
import com.logistics.core.lib.engine.state.TemperatureState;
import com.logistics.core.lib.power.EnergyBuffer;

public final class CreativeEngineSpec {
    public static final long CAPACITY = 10_000L;
    public static final long DRAIN_RATE = 0L; // Creative engine doesn't drain

    // Output levels that double with each click
    public static final long[] OUTPUT_LEVELS = {20, 40, 80, 160, 320, 640, 1280};
    public static final long DEFAULT_OUTPUT = OUTPUT_LEVELS[0];

    public static final boolean CAN_OVERHEAT = false;

    public static final int MIN_TEMP = 20;
    public static final int MAX_TEMP = 250;
    public static final int FIXED_TEMP = 100; // Fixed at "cool" temperature

    public final EnergyBuffer energy = new EnergyBuffer(CAPACITY);
    public final TemperatureState temp = new TemperatureState(MIN_TEMP, MIN_TEMP, MAX_TEMP);
    public final EngineCycleState cycle = new EngineCycleState();

    // Infinite producer always fills buffer to 100%
    public final InfiniteProducer producer = new InfiniteProducer();
    public final PassiveDrain drain = new PassiveDrain(DRAIN_RATE);
    public final FixedThermalModel thermal = new FixedThermalModel(FIXED_TEMP);
    // Mutable output that updates when output level changes
    public ConstantOutput output;
    public final PistonSpeedTable speeds = new PistonSpeedTable();

    private int outputLevelIndex = 0;

    public CreativeEngineSpec() {
        updateOutputLevel(0);
    }

    public int getOutputLevelIndex() {
        return outputLevelIndex;
    }

    public void setOutputLevelIndex(int index) {
        if (index < 0 || index >= OUTPUT_LEVELS.length) {
            index = 0;
        }
        if (this.outputLevelIndex != index) {
            this.outputLevelIndex = index;
            updateOutputLevel(index);
        }
    }

    private void updateOutputLevel(int index) {
        long level = OUTPUT_LEVELS[index];
        this.output = new ConstantOutput(level);
    }

    public long getCurrentOutputLevel() {
        return OUTPUT_LEVELS[outputLevelIndex];
    }

    public HeatStage stage() {
        return speeds.stage(temp.ratio(), CAN_OVERHEAT);
    }

    public float pistonSpeed() {
        // Speed scales with output level
        return speeds.speed(temp.ratio(), CAN_OVERHEAT) * (outputLevelIndex + 1);
    }

    public boolean canOverheat() {
        return CAN_OVERHEAT;
    }
}