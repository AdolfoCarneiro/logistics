package com.logistics.core.lib.engine;

import com.logistics.core.lib.power.EnergyBuffer;

public final class CoupledThermalModel {
    private final int minTemp;
    private final int maxTemp;

    public CoupledThermalModel(int minTemp, int maxTemp) {
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
    }

    public void update(EnergyBuffer energy, TemperatureState temp) {
        double ratio = energy.ratio();
        double t = (maxTemp - minTemp) * ratio + minTemp;
        temp.setCelsius(Math.round(t));
    }
}