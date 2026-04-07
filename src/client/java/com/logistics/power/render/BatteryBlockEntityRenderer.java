package com.logistics.power.render;

import com.logistics.LogisticsPowerClient;
import com.logistics.power.block.entity.BatteryBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.FabricModelManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders the charge-level overlay on all four sides of a Battery block.
 *
 * <p>Uses 10 pre-baked overlay models (battery_charge_1 through battery_charge_10),
 * each covering a progressively taller strip of the side texture.
 * The strip is rendered at full-bright (emissive) to simulate an LED indicator.
 *
 * <p>Level 0 = nothing rendered. Level 1 shows the bottom glow pixel (V=14) plus
 * the first bar pixel (V=13). Level 10 shows the full bar including the top glow (V=3).
 */
public class BatteryBlockEntityRenderer
        implements BlockEntityRenderer<BatteryBlockEntity, BatteryRenderState> {

    public BatteryBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public BatteryRenderState createRenderState() {
        return new BatteryRenderState();
    }

    @Override
    public void extractRenderState(
            BatteryBlockEntity entity,
            BatteryRenderState state,
            float tickDelta,
            Vec3 cameraPos,
            net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(entity, state, crumblingOverlay);
        long cap = entity.getEnergyCapacity();
        float charge = cap > 0 ? (float) entity.getEnergyStored() / cap : 0f;
        state.chargeLevel = charge <= 0f ? 0 : Math.max(1, Math.round(charge * 10));
    }

    @Override
    public void submit(
            BatteryRenderState state,
            PoseStack matrices,
            SubmitNodeCollector queue,
            CameraRenderState cameraState) {
        int level = state.chargeLevel;
        if (level <= 0) return;

        BlockStateModel model = getChargeModel(level);
        if (model == null) return;

        // Full-bright light: block light = 15, sky light = 15 (emissive glow)
        int light = (15 << 4) | (15 << 20);

        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(RandomSource.create(0), parts);
        queue.submitBlockModel(
                matrices, RenderTypes.translucentMovingBlock(),
                parts, new int[]{0xFFFFFF}, light, OverlayTexture.NO_OVERLAY, 0);
    }

    private BlockStateModel getChargeModel(int level) {
        ExtraModelKey<BlockStateModel> key = switch (level) {
            case 1  -> LogisticsPowerClient.MODEL.BATTERY_CHARGE_1;
            case 2  -> LogisticsPowerClient.MODEL.BATTERY_CHARGE_2;
            case 3  -> LogisticsPowerClient.MODEL.BATTERY_CHARGE_3;
            case 4  -> LogisticsPowerClient.MODEL.BATTERY_CHARGE_4;
            case 5  -> LogisticsPowerClient.MODEL.BATTERY_CHARGE_5;
            case 6  -> LogisticsPowerClient.MODEL.BATTERY_CHARGE_6;
            case 7  -> LogisticsPowerClient.MODEL.BATTERY_CHARGE_7;
            case 8  -> LogisticsPowerClient.MODEL.BATTERY_CHARGE_8;
            case 9  -> LogisticsPowerClient.MODEL.BATTERY_CHARGE_9;
            case 10 -> LogisticsPowerClient.MODEL.BATTERY_CHARGE_10;
            default -> null;
        };
        if (key == null) return null;
        FabricModelManager modelManager = (FabricModelManager) Minecraft.getInstance().getModelManager();
        return modelManager.getModel(key);
    }
}
