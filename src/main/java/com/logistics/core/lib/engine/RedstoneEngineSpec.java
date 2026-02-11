package com.logistics.core.lib.engine;

import com.logistics.core.lib.engine.drain.PassiveDrain;
import com.logistics.core.lib.engine.output.PulsedOutput;
import com.logistics.core.lib.engine.producer.TimedPulseProducer;
import com.logistics.core.lib.engine.state.EngineCycleState;
import com.logistics.core.lib.engine.state.HeatStage;
import com.logistics.core.lib.engine.state.PistonSpeedTable;
import com.logistics.core.lib.engine.state.TemperatureState;
import com.logistics.core.lib.engine.heat.CoupledThermalModel;
import com.logistics.core.lib.power.EnergyBuffer;

public final class RedstoneEngineSpec implements EngineSpec {
    public static final long CAPACITY = 1000L;
    public static final long DRAIN_RATE = 10L;

    public static final long GEN_RF_QTY = 10L;
    public static final int GEN_RF_PERIOD = 16;

    public static final long RF_ON_OUTPUT = 10L;
    public static final boolean CAN_OVERHEAT = false;

    public static final int MIN_TEMP = 20;
    public static final int MAX_TEMP = 250;

    public final EnergyBuffer energy = new EnergyBuffer(CAPACITY);
    public final TemperatureState temp = new TemperatureState(MIN_TEMP, MIN_TEMP, MAX_TEMP);
    public final EngineCycleState cycle = new EngineCycleState();

    public final TimedPulseProducer producer = new TimedPulseProducer(GEN_RF_QTY, GEN_RF_PERIOD);
    public final PassiveDrain drain = new PassiveDrain(DRAIN_RATE);
    public final CoupledThermalModel thermal = new CoupledThermalModel(MIN_TEMP, MAX_TEMP);
    public final PulsedOutput output = new PulsedOutput(RF_ON_OUTPUT);
    public final PistonSpeedTable speeds = new PistonSpeedTable();

    @Override
    public EnergyBuffer getEnergy() {
        return energy;
    }

    @Override
    public TemperatureState getTemperature() {
        return temp;
    }

    @Override
    public EngineCycleState getCycle() {
        return cycle;
    }

    @Override
    public Producer getProducer() {
        return producer;
    }

    @Override
    public Drain getDrain() {
        return drain;
    }

    @Override
    public ThermalModel getThermal() {
        return thermal;
    }

    @Override
    public Output getOutput() {
        return output;
    }

    @Override
    public HeatStage stage() {
        return speeds.stage(temp.ratio(), CAN_OVERHEAT);
    }

    @Override
    public float pistonSpeed() {
        return speeds.speed(temp.ratio(), CAN_OVERHEAT);
    }

    @Override
    public boolean canOverheat() {
        return CAN_OVERHEAT;
    }
}