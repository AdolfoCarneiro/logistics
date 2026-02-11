package com.logistics.core.lib.engine;

import com.logistics.core.lib.engine.state.EngineCycleState;
import com.logistics.core.lib.power.EnergyBuffer;

/**
 * Determines how much energy an engine can output.
 * Called each tick to calculate maximum output amount.
 */
public interface Output {
    /**
     * Calculate the maximum amount of energy that can be sent this tick.
     *
     * @param energy the current energy buffer state
     * @param cycleResult the result of advancing the piston cycle
     * @return the maximum energy to send this tick (0 if no output)
     */
    long maxSend(EnergyBuffer energy, EngineCycleState.AdvanceResult cycleResult);
}