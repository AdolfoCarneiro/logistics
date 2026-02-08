package com.logistics.core.lib.engine;

import com.logistics.core.lib.power.EnergyBuffer;

public final class PulseOnHalfCrossingOutputModel {
    private final long pulseAmount;

    public PulseOnHalfCrossingOutputModel(long pulseAmount) {
        this.pulseAmount = pulseAmount;
    }

    /** Returns how much to send this tick (0 if none). */
    public long maxSend(EnergyBuffer energy, EngineCycleState.AdvanceResult cycle) {
        if (!cycle.crossedHalf()) return 0;
        return Math.min(pulseAmount, energy.energy());
    }
}