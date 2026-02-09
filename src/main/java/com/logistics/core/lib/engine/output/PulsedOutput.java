package com.logistics.core.lib.engine.output;

import com.logistics.core.lib.engine.state.EngineCycleState;
import com.logistics.core.lib.power.EnergyBuffer;

public final class PulsedOutput {
    private final long pulseAmount;

    public PulsedOutput(long pulseAmount) {
        this.pulseAmount = pulseAmount;
    }

    /** Returns how much to send this tick (0 if none). */
    public long maxSend(EnergyBuffer energy, EngineCycleState.AdvanceResult cycle) {
        if (!cycle.crossedHalf()) return 0;
        return Math.min(pulseAmount, energy.energy());
    }
}