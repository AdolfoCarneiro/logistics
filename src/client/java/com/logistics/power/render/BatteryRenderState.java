package com.logistics.power.render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

/**
 * Render state for battery block entities.
 * Holds the charge level (0–10) used to select the correct overlay model.
 */
public class BatteryRenderState extends BlockEntityRenderState {
    /** Charge level from 0 (empty) to 10 (full). */
    public int chargeLevel;
}
