package com.logistics.core.lib.engine;

import com.logistics.core.lib.engine.drain.PassiveDrain;
import com.logistics.core.lib.engine.heat.CoupledThermalModel;
import com.logistics.core.lib.engine.output.ProportionalOutput;
import com.logistics.core.lib.engine.producer.PIDProducer;
import com.logistics.core.lib.engine.state.*;
import com.logistics.core.lib.power.EnergyBuffer;

public final class StirlingEngineSpec {
    public static final long CAPACITY = 10_000L;
    public static final long DRAIN_RATE = 10L;

    private static final double TARGET_RATIO = 0.652;
    private static final long MIN_GENERATION = 3;
    private static final long MAX_GENERATION = 10;

    public static final long MAX_OUTPUT = 10L;
    public static final boolean CAN_OVERHEAT = true;

    public static final int MIN_TEMP = 20;
    public static final int MAX_TEMP = 250;

    public final EnergyBuffer energy = new EnergyBuffer(CAPACITY);
    public final TemperatureState temp = new TemperatureState(MIN_TEMP, MIN_TEMP, MAX_TEMP);
    public final EngineCycleState cycle = new EngineCycleState();
    public final SolidFuelBurnState fuel = new SolidFuelBurnState();

    public final PIDProducer producer = new PIDProducer(TARGET_RATIO, MIN_GENERATION, MAX_GENERATION);
    public final PassiveDrain drain = new PassiveDrain(DRAIN_RATE);
    public final CoupledThermalModel thermal = new CoupledThermalModel(MIN_TEMP, MAX_TEMP);
    public final ProportionalOutput output = new ProportionalOutput(TARGET_RATIO, MAX_OUTPUT);
    public final PistonSpeedTable speeds = new PistonSpeedTable();

    public HeatStage stage() {
        return speeds.stage(temp.ratio(), CAN_OVERHEAT);
    }

    public float pistonSpeed() {
        return speeds.speed(temp.ratio(), CAN_OVERHEAT);
    }
}