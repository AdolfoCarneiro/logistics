package com.logistics.core.lib.engine;

import com.logistics.core.lib.power.EnergyBuffer;

/**
 * Produces energy for an engine.
 * Called each tick when the engine is running.
 */
public interface Producer {
    /**
     * Tick the producer and generate energy.
     *
     * @param running whether the engine is currently running
     * @param energy the energy buffer to add generated energy to
     * @return the amount of energy generated this tick
     */
    long tick(boolean running, EnergyBuffer energy);

    /**
     * Reset the producer state.
     * Called when the engine stops or resets.
     */
    default void reset() {
        // Optional - not all producers need reset
    }
}