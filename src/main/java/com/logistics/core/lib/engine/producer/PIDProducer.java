package com.logistics.core.lib.engine.producer;

import com.logistics.core.lib.power.EnergyBuffer;
import com.logistics.power.engine.PIDController;

public final class PIDProducer {
    // PID controller settings (tuned via run/pid_simulator.py)
    private static final double PID_KP = 25.0;
    private static final double PID_KI = 0.03;
    private static final double PID_KD = 15.0;

    private final PIDController pidController = new PIDController(PID_KP, PID_KI, PID_KD);

    private final double targetRatio;
    private final long minGeneration;
    private final long maxGeneration;

    private double accumulator = 0;

    public PIDProducer(double targetRatio, long minGeneration, long maxGeneration) {
        if (!(targetRatio > 0.0 && targetRatio <= 1.0)) {
            throw new IllegalArgumentException("targetRatio must be in (0, 1]");
        }
        if (minGeneration < 0) {
            throw new IllegalArgumentException("minGeneration must be >= 0");
        }
        if (maxGeneration <= minGeneration) {
            throw new IllegalArgumentException("maxGeneration must be > minProduced");
        }

        this.targetRatio = targetRatio;
        this.minGeneration = minGeneration;
        this.maxGeneration = maxGeneration;
    }

    public long tick(boolean powered, EnergyBuffer energy) {
        if (!powered) return 0;

        accumulator += pidController.compute(targetRatio, energy.ratio(), minGeneration, maxGeneration);

        long whole = (long) Math.floor(accumulator);
        long added = energy.add(whole);

        if(energy.ratio() >= 1) {
            accumulator = 0;
        } else {
            accumulator -= added;
        }

        return added;
    }

    public double getAccumulator() {
        return accumulator;
    }

    public void setAccumulator(double accumulator) {
        this.accumulator = accumulator;
    }

    public void reset() {
        this.accumulator = 0;
        this.pidController.reset();
    }
}