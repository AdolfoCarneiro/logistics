package com.logistics.core.lib.engine;

import com.logistics.core.lib.storage.NbtCompat;
import net.minecraft.nbt.CompoundTag;

public final class EngineSerde {
    private EngineSerde() {}

    public static final String KEY_ENGINE = "Engine";
    public static final String KEY_ENERGY = "energy";
    public static final String KEY_HEAT = "heat";
    public static final String KEY_PROGRESS = "progress";
    public static final String KEY_OVERHEATED = "overheated";

    /** Engine-specific snapshot of persisted state. */
    public record Snapshot(long energy, long heatC, float progress, boolean overheated) {}

    public static CompoundTag writeSnapshot(Snapshot s) {
        CompoundTag tag = new CompoundTag();
        tag.putLong(KEY_ENERGY, s.energy());
        tag.putLong(KEY_HEAT, s.heatC());
        tag.putFloat(KEY_PROGRESS, s.progress());
        tag.putBoolean(KEY_OVERHEATED, s.overheated());
        return tag;
    }

    public static Snapshot readSnapshot(CompoundTag tag, long defaultHeatC) {
        long energy = NbtCompat.getLongLossy(tag, KEY_ENERGY, 0L);
        long heatC  = NbtCompat.getLongLossy(tag, KEY_HEAT, defaultHeatC);
        float prog  = NbtCompat.getFloat(tag, KEY_PROGRESS, 0f);
        boolean oh  = NbtCompat.getBoolean(tag, KEY_OVERHEATED, false);
        return new Snapshot(energy, heatC, prog, oh);
    }
}