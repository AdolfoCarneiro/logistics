package com.logistics.core.lib.power;

public final class EnergyBuffer {
    private final long capacity;
    private long energy;

    public EnergyBuffer(long capacity) {
        this.capacity = Math.max(0L, capacity);
        this.energy = 0L;
    }

    public long capacity() { return capacity; }
    public long energy() { return energy; }

    public double ratio() {
        if (capacity <= 0) return 0.0;
        return Math.max(0.0, Math.min(1.0, (double) energy / (double) capacity));
    }

    public void set(long value) {
        energy = clamp(value);
    }

    public long add(long delta) {
        if (delta <= 0) return 0;
        long before = energy;
        energy = clamp(energy + delta);
        return energy - before;
    }

    public long remove(long delta) {
        if (delta <= 0) return 0;
        long before = energy;
        energy = clamp(energy - delta);
        return before - energy;
    }

    private long clamp(long v) {
        if (v < 0) return 0;
        if (v > capacity) return capacity;
        return v;
    }
}
