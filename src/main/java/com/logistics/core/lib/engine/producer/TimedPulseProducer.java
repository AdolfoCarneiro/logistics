package com.logistics.core.lib.engine.producer;

import com.logistics.core.lib.engine.Producer;
import com.logistics.core.lib.power.EnergyBuffer;

public final class TimedPulseProducer implements Producer {

    private final int periodTicks;
    private final long energyPerPulse;

    private int counter = 0;

    public TimedPulseProducer(long energyPerPulse, int periodTicks) {
        this.periodTicks = Math.max(1, periodTicks);
        this.energyPerPulse = Math.max(0L, energyPerPulse);
    }

    @Override
    public long tick(boolean powered, EnergyBuffer energy) {
        if (!powered) {
            counter = 0;
            return 0;
        }

        counter++;

        if (counter >= periodTicks) {
            counter = 0;
            return energy.add(energyPerPulse);
        }

        return 0;
    }

    @Override
    public void reset() {
        counter = 0;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int value) {
        this.counter = Math.max(0, value % periodTicks);
    }
}