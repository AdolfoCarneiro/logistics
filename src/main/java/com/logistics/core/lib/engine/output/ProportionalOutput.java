package com.logistics.core.lib.engine.output;

import com.logistics.core.lib.engine.Output;
import com.logistics.core.lib.engine.state.EngineCycleState;
import com.logistics.core.lib.power.EnergyBuffer;

public final class ProportionalOutput implements Output {
    private final double targetRatio;
    private final long maxOutput;

    public ProportionalOutput(double targetRatio, long maxOutput) {
        if (!(targetRatio > 0.0 && targetRatio <= 1.0)) {
            throw new IllegalArgumentException("targetRatio must be in (0, 1]");
        }
        this.targetRatio = targetRatio;
        this.maxOutput = Math.max(0L, maxOutput);
    }

    @Override
    public long maxSend(EnergyBuffer energy, EngineCycleState.AdvanceResult cycle) {
        long stored = energy.energy();
        if (stored <= 0L || maxOutput <= 0L) return 0L;

        double ratio = energy.ratio();
        if (ratio <= 0.0) return 0L;

        // Scale 0..targetRatio -> 0..maxOutput; clamp above targetRatio.
        double scaled = (ratio >= targetRatio) ? (double) maxOutput : ((ratio / targetRatio) * (double) maxOutput);

        // Floor to avoid outputting more than intended due to rounding.
        long out = (long) Math.floor(scaled + 1e-9);
        if (out < 0L) out = 0L;
        if (out > maxOutput) out = maxOutput;

        return Math.min(out, stored);
    }
}