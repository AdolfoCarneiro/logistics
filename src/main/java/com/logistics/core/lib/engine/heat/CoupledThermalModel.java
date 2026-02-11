package com.logistics.core.lib.engine.heat;

import com.logistics.core.lib.engine.ThermalModel;
import com.logistics.core.lib.engine.state.TemperatureState;
import com.logistics.core.lib.power.EnergyBuffer;

public final class CoupledThermalModel implements ThermalModel {
    private final int minTemp;
    private final int maxTemp;

    public CoupledThermalModel(int minTemp, int maxTemp) {
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
    }

    @Override
    public void update(EnergyBuffer energy, TemperatureState temp) {
        double ratio = energy.ratio();
        double t = (maxTemp - minTemp) * ratio + minTemp;
        temp.setCelsius(Math.round(t));
    }
}