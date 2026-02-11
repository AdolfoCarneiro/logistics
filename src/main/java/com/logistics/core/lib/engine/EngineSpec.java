package com.logistics.core.lib.engine;

import com.logistics.core.lib.engine.state.EngineCycleState;
import com.logistics.core.lib.engine.state.HeatStage;
import com.logistics.core.lib.engine.state.TemperatureState;
import com.logistics.core.lib.power.EnergyBuffer;

/**
 * Specification interface for engine behavior.
 * Provides access to all components that define an engine's operation.
 */
public interface EngineSpec {
    /**
     * Returns the energy buffer for this engine.
     */
    EnergyBuffer getEnergy();

    /**
     * Returns the temperature state for this engine.
     */
    TemperatureState getTemperature();

    /**
     * Returns the piston cycle state for this engine.
     */
    EngineCycleState getCycle();

    /**
     * Returns the energy producer component.
     */
    Producer getProducer();

    /**
     * Returns the energy drain component.
     */
    Drain getDrain();

    /**
     * Returns the thermal model component.
     */
    ThermalModel getThermal();

    /**
     * Returns the output component.
     */
    Output getOutput();

    /**
     * Returns whether this engine can overheat.
     */
    boolean canOverheat();

    /**
     * Returns the current piston speed.
     */
    float pistonSpeed();

    /**
     * Returns the current heat stage.
     */
    HeatStage stage();
}