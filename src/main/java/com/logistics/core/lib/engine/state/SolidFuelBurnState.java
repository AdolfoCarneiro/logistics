package com.logistics.core.lib.engine.state;

public final class SolidFuelBurnState {
    private int fuelTicks;
    private int burnTicks;

    public boolean isBurning() { return burnTicks > 0; }

    /** Decrement one tick if burning. */
    public void tickDown() {
        if (burnTicks > 0) burnTicks--;
    }

    public void ignite(int newBurnTicks) {
        if (newBurnTicks > 0) burnTicks = fuelTicks = newBurnTicks;
    }

    public double getRatio() {
        if (fuelTicks <= 0) return 0.0d;

        return (double) burnTicks / (double) fuelTicks;
    }

    public void reset() { burnTicks = 0; }

    public int getFuelTicks() {
        return fuelTicks;
    }

    public int getBurnTicks() {
        return burnTicks;
    }

    public void setFuelTicks(int fuelTicks) {
        this.fuelTicks = fuelTicks;
    }

    public void setBurnTicks(int burnTicks) {
        this.burnTicks = burnTicks;
    }
}
