package com.logistics.core.lib.engine;

public final class EngineCycleState {

    public enum Phase { EXPANSION, COMPRESSION }

    public static final float TRANSITION = 0.5f;

    private float progress01;

    public EngineCycleState() {
        this(0.0f);
    }

    public EngineCycleState(float progress01) {
        this.progress01 = normalize(progress01);
    }

    public void setProgress(float progress01) {
        this.progress01 = normalize(progress01);
    }

    public float progress() { return progress01; }

    public Phase phase() {
        return (progress01 < TRANSITION) ? Phase.EXPANSION : Phase.COMPRESSION;
    }

    public void reset() {
        progress01 = 0.0f;
    }

    public AdvanceResult advance(float delta) {
        if (delta <= 0.0f) return AdvanceResult.noop(phase(), progress01);

        float prev = progress01;
        float next = prev + delta;

        boolean wrapped = next >= 1.0f;
        if (wrapped) {
            next = next - (float)Math.floor(next); // supports delta > 1
        }

        boolean crossedHalf = crosses(prev, next, TRANSITION, wrapped);

        progress01 = next;

        return new AdvanceResult(phase(), prev, progress01, crossedHalf, wrapped);
    }

    private static boolean crosses(float prev, float next, float threshold, boolean wrapped) {
        if (!wrapped) {
            return prev < threshold && next >= threshold;
        }
        // Wrapped: we went prev -> 1.0 then 0 -> next
        // We "cross" threshold if it was crossed in either segment.
        // Segment A: prev..1.0 crosses threshold if prev < threshold
        // Segment B: 0..next crosses threshold if next >= threshold
        return prev < threshold || next >= threshold;
    }

    private static float normalize(float p) {
        if (p < 0) return 0.0f;
        if (p >= 1.0f) return p - (float)Math.floor(p); // keep in [0,1)
        return p;
    }

    public record AdvanceResult(
            Phase phaseAfter,
            float prevProgress,
            float newProgress,
            boolean crossedHalf,
            boolean wrapped
    ) {
        static AdvanceResult noop(Phase phase, float progress) {
            return new AdvanceResult(phase, progress, progress, false, false);
        }
    }
}