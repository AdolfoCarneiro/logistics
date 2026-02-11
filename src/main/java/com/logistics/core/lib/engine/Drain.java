package com.logistics.core.lib.engine;

import com.logistics.core.lib.power.EnergyBuffer;

/**
 * Drains energy from an engine's buffer.
 * Called each tick to simulate passive energy loss.
 */
public interface Drain {
    /**
     * Tick the drain and remove energy from the buffer.
     *
     * @param running whether the engine is currently running
     * @param energy the energy buffer to drain from
     */
    void tick(boolean running, EnergyBuffer energy);
}