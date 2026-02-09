package com.logistics.core.lib.engine.output;

import com.logistics.core.lib.engine.state.EngineCycleState;
import com.logistics.core.lib.power.EnergyBuffer;

public final class ConstantOutput {
    private final long output;

    public ConstantOutput(long output) {
        this.output = output;
    }

    /** Returns how much to send this tick (0 if none). */
    public long maxSend(EnergyBuffer energy, EngineCycleState.AdvanceResult cycle) {
        return Math.min(output, energy.energy());
    }
}