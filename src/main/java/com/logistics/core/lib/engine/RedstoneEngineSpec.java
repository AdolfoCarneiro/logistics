package com.logistics.core.lib.engine;

import com.logistics.core.lib.power.EnergyBuffer;

public final class RedstoneEngineSpec {
    public static final long CAPACITY = 1000L;
    public static final long DRAIN_RATE = 10L;

    public static final long GEN_RF_QTY = 10L;
    public static final int GEN_RF_PERIOD = 16;

    public static final long RF_ON_OUTPUT = 10L;

    public static final int MIN_TEMP = 20;
    public static final int MAX_TEMP = 250;

    public final EnergyBuffer energy = new EnergyBuffer(CAPACITY);
    public final TemperatureState temp = new TemperatureState(MIN_TEMP, MIN_TEMP, MAX_TEMP);
    public final EngineCycleState cycle = new EngineCycleState();

    public final TimedPulseProducer producer = new TimedPulseProducer(GEN_RF_QTY, GEN_RF_PERIOD);
    public final PassiveDrain drain = new PassiveDrain(DRAIN_RATE);
    public final CoupledThermalModel thermal = new CoupledThermalModel(MIN_TEMP, MAX_TEMP);
    public final PulseOnHalfCrossingOutputModel output = new PulseOnHalfCrossingOutputModel(RF_ON_OUTPUT);
    public final PistonSpeedTable speeds = new PistonSpeedTable();

    public HeatStage stage() {
        return speeds.stage(temp.ratio(), /*canOverheat*/ false);
    }

    public float pistonSpeed() {
        return speeds.speed(temp.ratio(), /*canOverheat*/ false);
    }
}