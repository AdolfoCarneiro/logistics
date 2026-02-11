package com.logistics.core.lib.engine.producer;

import com.logistics.core.lib.engine.Producer;
import com.logistics.core.lib.power.EnergyBuffer;

public final class ConstantProducer implements Producer {
    private final long rfPerTick;

    public ConstantProducer(long rfPerTick) {
        if (rfPerTick < 0) {
            throw new IllegalArgumentException("rfPerTick must be >= 0");
        }
        this.rfPerTick = rfPerTick;
    }

    @Override
    public long tick(boolean powered, EnergyBuffer energy) {
        if (!powered) return 0;
        return energy.add(rfPerTick);
    }

    @Override
    public void reset() {
        // No state to reset
    }
}