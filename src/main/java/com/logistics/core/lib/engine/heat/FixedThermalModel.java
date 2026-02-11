package com.logistics.core.lib.engine.heat;

import com.logistics.core.lib.engine.state.TemperatureState;
import com.logistics.core.lib.power.EnergyBuffer;

/**
 * Fixed thermal model - maintains a constant temperature regardless of energy level.
 * Used for creative engines that don't simulate realistic heating/cooling.
 */
public final class FixedThermalModel {
    private final int fixedTemp;

    public FixedThermalModel(int fixedTemp) {
        this.fixedTemp = fixedTemp;
    }

    public void update(EnergyBuffer energy, TemperatureState temp) {
        // Always maintain fixed temperature
        temp.setCelsius(fixedTemp);
    }
}