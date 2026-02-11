package com.logistics.core.lib.engine.drain;

import com.logistics.core.lib.engine.Drain;
import com.logistics.core.lib.power.EnergyBuffer;

public final class PassiveDrain implements Drain {
    private final long perTick;
    public PassiveDrain(long perTick) { this.perTick = Math.max(0, perTick); }

    @Override
    public void tick(boolean powered, EnergyBuffer energy) {
        if (!powered) energy.remove(perTick);
    }
}
