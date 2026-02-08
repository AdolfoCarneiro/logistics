package com.logistics.core.lib.power;

import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.base.SimpleSidedEnergyContainer;

/**
 * A customizable energy storage implementation with direct energy manipulation methods.
 *
 * <p>Extends Team Reborn's {@link SimpleSidedEnergyContainer} with convenient methods
 * for setting, adding, and removing energy with automatic clamping to [0, capacity].
 *
 * <p>This class provides a cleaner alternative to directly manipulating the {@code amount}
 * field, ensuring energy values are always valid.
 *
 * <p>Can be used standalone with fixed capacity/maxExtract, or extended with anonymous
 * classes to override {@link #getCapacity()} and {@link #getMaxExtract(Direction)} for
 * dynamic behavior.
 */
public class SidedEnergyProvider extends SimpleSidedEnergyContainer {

    private final long capacity;
    private final long maxExtract;
    private final Runnable onCommit;

    /**
     * Creates a new energy provider with no commit callback.
     * Useful when extending with an anonymous class.
     */
    protected SidedEnergyProvider() {
        this(0, 0, null);
    }

    /**
     * Creates a new energy provider.
     *
     * @param capacity the maximum energy capacity in RF
     * @param maxExtract the maximum extraction rate in RF/t
     */
    public SidedEnergyProvider(long capacity, long maxExtract) {
        this(capacity, maxExtract, null);
    }

    /**
     * Creates a new energy provider with a commit callback.
     *
     * @param capacity the maximum energy capacity in RF
     * @param maxExtract the maximum extraction rate in RF/t
     * @param onCommit callback invoked when energy changes (typically for marking dirty)
     */
    public SidedEnergyProvider(long capacity, long maxExtract, @Nullable Runnable onCommit) {
        this.capacity = capacity;
        this.maxExtract = maxExtract;
        this.onCommit = onCommit;
    }

    @Override
    public long getCapacity() {
        return capacity;
    }

    @Override
    public long getMaxInsert(@Nullable Direction side) {
        return 0; // No input by default
    }

    @Override
    public long getMaxExtract(@Nullable Direction side) {
        return maxExtract;
    }

    @Override
    protected void onFinalCommit() {
        if (onCommit != null) {
            onCommit.run();
        }
    }

    /**
     * Sets the energy level directly, clamping to [0, capacity].
     *
     * @param energy the new energy level in RF
     */
    public void setEnergy(long energy) {
        this.amount = Math.max(0L, Math.min(energy, getCapacity()));
    }

    /**
     * Adds energy to the storage, clamping to capacity.
     *
     * @param energy the amount of energy to add in RF
     */
    public void addEnergy(long energy) {
        setEnergy(this.amount + energy);
    }

    /**
     * Removes energy from the storage, clamping to zero.
     *
     * @param energy the amount of energy to remove in RF
     */
    public void removeEnergy(long energy) {
        setEnergy(this.amount - energy);
    }

    /**
     * Gets the current energy level.
     *
     * @return the stored energy in RF
     */
    public long getEnergy() {
        return this.amount;
    }

    /**
     * Gets the energy level as a ratio from 0.0 to 1.0.
     *
     * @return the fill ratio
     */
    public double getEnergyRatio() {
        long cap = getCapacity();
        return cap > 0 ? (double) amount / cap : 0.0;
    }
}