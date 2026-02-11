package com.logistics.core.lib.engine.producer;

import com.logistics.core.lib.power.EnergyBuffer;

public final class ConstantProducer {
    private final long rfPerTick;

    public ConstantProducer(long rfPerTick) {
        if (rfPerTick < 0) {
            throw new IllegalArgumentException("rfPerTick must be >= 0");
        }
        this.rfPerTick = rfPerTick;
    }

    public long tick(boolean powered, EnergyBuffer energy) {
        if (!powered) return 0;
        return energy.add(rfPerTick);
    }

    public void reset() {
        // No state to reset
    }
}