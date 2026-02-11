package com.logistics.core.lib.engine.producer;

import com.logistics.core.lib.power.EnergyBuffer;

/**
 * Infinite producer for creative engine - always fills buffer to 100% capacity.
 * Used for creative/testing purposes where infinite energy is needed.
 */
public final class InfiniteProducer {

    public long tick(boolean powered, EnergyBuffer energy) {
        if (!powered) return 0;

        long before = energy.energy();
        long capacity = energy.capacity();
        energy.set(capacity);

        return capacity - before;
    }

    public void reset() {
        // No state to reset
    }
}