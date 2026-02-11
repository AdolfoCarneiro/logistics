package com.logistics.core.lib.engine.fuel;

/**
 * Abstraction for a source of fuel that can be consumed by an engine.
 * This allows the engine spec to handle fuel logic without depending on
 * Minecraft-specific inventory or item stack classes.
 */
public interface FuelSource {
    /**
     * Returns the burn time (in ticks) of the next available fuel item.
     * Returns 0 if no fuel is available or the available item is not fuel.
     */
    int getNextFuelBurnTime();

    /**
     * Consumes one unit of fuel from the source.
     * Should only be called after checking that fuel is available.
     */
    void consumeFuel();
}