package com.logistics.core.lib.storage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public final class NbtCompat {
    private NbtCompat() {}

    /** Reads a long from NBT, allowing legacy numeric types (ex: double) and defaulting if missing. */
    public static long getLongLossy(CompoundTag tag, String key, long defaultValue) {
        Tag t = tag.get(key);
        if (t == null) return defaultValue;

        // Mojang mappings in this environment
        if (t instanceof net.minecraft.nbt.LongTag v)   return v.longValue();
        if (t instanceof net.minecraft.nbt.IntTag v)    return v.intValue();
        if (t instanceof net.minecraft.nbt.ShortTag v)  return v.shortValue();
        if (t instanceof net.minecraft.nbt.ByteTag v)   return v.byteValue();

        // Legacy: you used to store heat as a double
        if (t instanceof net.minecraft.nbt.DoubleTag v) return Math.round(v.doubleValue());
        if (t instanceof net.minecraft.nbt.FloatTag v)  return Math.round(v.floatValue());

        return defaultValue;
    }

    public static float getFloat(CompoundTag tag, String key, float defaultValue) {
        Tag t = tag.get(key);
        if (t == null) return defaultValue;

        if (t instanceof net.minecraft.nbt.FloatTag v)  return v.floatValue();
        if (t instanceof net.minecraft.nbt.DoubleTag v) return (float) v.doubleValue();
        if (t instanceof net.minecraft.nbt.IntTag v)    return v.intValue();
        if (t instanceof net.minecraft.nbt.LongTag v)   return v.longValue();

        return defaultValue;
    }

    public static boolean getBoolean(CompoundTag tag, String key, boolean defaultValue) {
        Tag t = tag.get(key);
        if (t == null) return defaultValue;

        if (t instanceof net.minecraft.nbt.ByteTag v) {
            return v.byteValue() != 0;
        }
        return defaultValue;
    }
}