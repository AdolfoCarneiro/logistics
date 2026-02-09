package com.logistics.core.lib.engine.state;

public final class PistonSpeedTable {
    public static final float TRANSITION = 0.5f;

    public HeatStage stage(double ratio, boolean canOverheat) {
        if (ratio < 0.25) return HeatStage.COLD;
        if (ratio < 0.50) return HeatStage.COOL;
        if (ratio < 0.75) return HeatStage.WARM;
        if (ratio < 1.0 || !canOverheat) return HeatStage.HOT;
        return HeatStage.OVERHEAT;
    }

    public float speed(double ratio, boolean canOverheat) {
        if (ratio <= 0.25) return 0.01f;
        if (ratio <= 0.50) return 0.02f;
        if (ratio <= 0.75) return 0.04f;
        if (ratio < 1.0 || !canOverheat) return 0.08f;
        return 0.0f;
    }
}
