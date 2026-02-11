package com.logistics.core.lib.engine;

import com.logistics.core.lib.engine.state.TemperatureState;
import com.logistics.core.lib.power.EnergyBuffer;

/**
 * Models the thermal behavior of an engine.
 * Updates temperature based on energy levels.
 */
public interface ThermalModel {
    /**
     * Update the engine temperature based on current energy level.
     *
     * @param energy the current energy buffer state
     * @param temp the temperature state to update
     */
    void update(EnergyBuffer energy, TemperatureState temp);
}