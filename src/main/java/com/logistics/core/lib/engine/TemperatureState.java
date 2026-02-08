package com.logistics.core.lib.engine;

public final class TemperatureState {
    private long celsius;

    public TemperatureState(long initialCelsius, long minCelsius, long maxCelsius) {
        this.minCelsius = minCelsius;
        this.maxCelsius = maxCelsius;
        this.celsius = clamp(initialCelsius);
    }

    private final long minCelsius;
    private final long maxCelsius;

    public long celsius() { return celsius; }

    public void setCelsius(long value) { this.celsius = clamp(value); }

    public void increase(long delta) { setCelsius(this.celsius + delta); }

    public void decrease(long delta) { setCelsius(this.celsius - delta); }

    public double ratio() {
        if (maxCelsius == minCelsius) return 0.0;
        return (double)(celsius - minCelsius) / (double)(maxCelsius - minCelsius);
    }

    private long clamp(long v) {
        return Math.max(minCelsius, Math.min(maxCelsius, v));
    }
}