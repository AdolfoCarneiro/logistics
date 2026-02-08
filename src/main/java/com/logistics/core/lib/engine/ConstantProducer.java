package com.logistics.core.lib.engine;

import com.logistics.core.lib.power.EnergyBuffer;

public final class ConstantProducer {
    private final long rfPerTick;

    public ConstantProducer(long rfPerTick) {
        this.rfPerTick = rfPerTick;
    }

    public long produce(boolean powered, EnergyBuffer energy) {
        if (!powered) return 0;
        return energy.add(rfPerTick);
    }
}