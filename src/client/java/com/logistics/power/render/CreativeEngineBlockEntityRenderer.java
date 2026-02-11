package com.logistics.power.render;

import com.logistics.LogisticsMod;
import com.logistics.core.lib.engine.CreativeEngineSpec;
import com.logistics.core.lib.engine.state.HeatStage;
import com.logistics.core.render.ModelRegistry;
import com.logistics.power.engine.block.entity.CreativeEngineBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public final class CreativeEngineBlockEntityRenderer
        implements BlockEntityRenderer<CreativeEngineBlockEntity, CreativeEngineBlockEntityRenderer.State> {

    // Shared model identifiers
    private static final Identifier TRUNK_BASE_MODEL =
            Identifier.fromNamespaceAndPath(LogisticsMod.MOD_ID, "block/power/engine_trunk_base");
    private static final Identifier TRUNK_OVERLAY_MODEL =
            Identifier.fromNamespaceAndPath(LogisticsMod.MOD_ID, "block/power/engine_trunk_overlay");
    private static final Identifier CHAMBER_MODEL =
            Identifier.fromNamespaceAndPath(LogisticsMod.MOD_ID, "block/power/engine_chamber");

    // Creative-specific base models
    private static final Identifier CREATIVE_BASE_STATIC =
            Identifier.fromNamespaceAndPath(LogisticsMod.MOD_ID, "block/power/creative_engine_base_static");
    private static final Identifier CREATIVE_BASE_MOVING =
            Identifier.fromNamespaceAndPath(LogisticsMod.MOD_ID, "block/power/creative_engine_base_moving");

    // Stage colors (RGB 0-1 range) - creative uses purple/cyan theme
    private static final float[] COLOR_BLUE = {0.2f, 0.4f, 0.8f};
    private static final float[] COLOR_CYAN = {0.2f, 0.8f, 0.8f};
    private static final float[] COLOR_PURPLE = {0.8f, 0.2f, 0.8f};
    private static final float[] COLOR_MAGENTA = {1.0f, 0.2f, 1.0f};

    // Animation cache (client-side smoothing)
    private static final Map<BlockPos, AnimCache> CACHE = new ConcurrentHashMap<>();

    private static final class AnimCache {
        float progress01 = 0f;
        long lastGameTick = -1;
    }

    public static void clearAnimationCache(BlockPos pos) {
        CACHE.remove(pos);
    }

    public static void clearAllAnimationCache() {
        CACHE.clear();
    }

    public CreativeEngineBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {}

    // -------- Render state --------

    public static final class State extends BlockEntityRenderState {
        public BlockPos pos;
        public Direction facing = Direction.UP;

        public boolean running;
        public float pistonSpeed;
        public float progress01;

        public HeatStage stage = HeatStage.COLD;

        public float pistonOffset() {
            // Map progress [0..1) to physical offset [0..0.5] blocks.
            float p = progress01;
            float t = (p < 0.5f) ? (p / 0.5f) : (1.0f - ((p - 0.5f) / 0.5f));
            return 0.5f * t;
        }
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(
            CreativeEngineBlockEntity be,
            State out,
            float tickDelta,
            Vec3 cameraPos,
            net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {

        net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState.extractBase(be, out, crumblingOverlay);

        out.pos = be.getBlockPos();

        BlockState state = be.getBlockState();
        out.facing = state.getValue(BlockStateProperties.FACING);

        // Delegate running logic to BE
        out.running = be.isRunning();

        // Compute stage from temperature ratio
        out.stage = be.isOverheated() ? HeatStage.OVERHEAT : be.getHeatStage();

        // Speed from BE (already derived from output level in spec)
        out.pistonSpeed = be.getPistonSpeed();

        // Smooth progress from cache (client-side)
        AnimCache cache = CACHE.computeIfAbsent(out.pos, k -> new AnimCache());
        out.progress01 = updateProgress(cache, out.pistonSpeed, out.running);
    }

    private static final float DEFAULT_PISTON_SPEED = 0.02f;

    private float updateProgress(AnimCache cache, float pistonSpeed, boolean running) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return 0f;

        long now = client.level.getGameTime();

        if (cache.lastGameTick < 0) {
            cache.lastGameTick = now;
            return cache.progress01;
        }

        long dt = now - cache.lastGameTick;
        if (dt <= 0) return cache.progress01;

        float speed = pistonSpeed > 0 ? pistonSpeed : DEFAULT_PISTON_SPEED;

        if (running) {
            cache.progress01 += speed * dt;
            cache.progress01 = wrap01(cache.progress01);
        } else {
            // when powered off: finish the cycle back to zero
            if (cache.progress01 > 0.001f) {
                cache.progress01 += speed * dt;
                if (cache.progress01 >= 1.0f) cache.progress01 = 0f;
            }
        }

        cache.lastGameTick = now;
        return cache.progress01;
    }

    private static float wrap01(float v) {
        if (v >= 1.0f) v = v - (float) Math.floor(v);
        if (v < 0.0f) v = 0.0f;
        return v;
    }

    // -------- Submit --------

    @Override
    public void submit(State s, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {

        BlockStateModel baseStaticModel = ModelRegistry.getModel(CREATIVE_BASE_STATIC);
        BlockStateModel baseMovingModel = ModelRegistry.getModel(CREATIVE_BASE_MOVING);
        BlockStateModel trunkBaseModel = ModelRegistry.getModel(TRUNK_BASE_MODEL);
        BlockStateModel trunkOverlayModel = ModelRegistry.getModel(TRUNK_OVERLAY_MODEL);
        BlockStateModel chamberModel = ModelRegistry.getModel(CHAMBER_MODEL);

        if (baseStaticModel == null
                || baseMovingModel == null
                || trunkBaseModel == null
                || trunkOverlayModel == null
                || chamberModel == null) {
            return;
        }

        RenderType layer = RenderTypes.cutoutMovingBlock();
        int light = s.lightCoords;

        float pistonOffset = s.pistonOffset();
        float[] trunkColor = trunkColor(s);

        matrices.pushPose();

        applyFacingRotation(matrices, s.facing);

        // 1) Static base (Y=0-4)
        matrices.pushPose();
        queue.submitBlockModel(matrices, layer, baseStaticModel,
                1f, 1f, 1f, light, OverlayTexture.NO_OVERLAY, 0);
        matrices.popPose();

        // 2) Moving base (Y=4+offset to Y=8+offset)
        matrices.pushPose();
        matrices.translate(0, 4 / 16f + pistonOffset, 0);
        queue.submitBlockModel(matrices, layer, baseMovingModel,
                1f, 1f, 1f, light, OverlayTexture.NO_OVERLAY, 0);
        matrices.popPose();

        // 3) Trunk base (Y=4-16)
        matrices.pushPose();
        matrices.translate(0, 4 / 16f, 0);
        queue.submitBlockModel(matrices, layer, trunkBaseModel,
                1f, 1f, 1f, light, OverlayTexture.NO_OVERLAY, 0);
        matrices.popPose();

        // 4) Trunk overlay (tinted)
        matrices.pushPose();
        matrices.translate(0, 4 / 16f, 0);
        queue.submitBlockModel(matrices, layer, trunkOverlayModel,
                trunkColor[0], trunkColor[1], trunkColor[2],
                light, OverlayTexture.NO_OVERLAY, 0);
        matrices.popPose();

        // 5) Chamber (scaled)
        if (pistonOffset > 0.01f) {
            matrices.pushPose();
            matrices.translate(0, 4 / 16f, 0);
            float chamberScaleY = pistonOffset / 0.5f; // 0..1
            matrices.scale(1.0f, chamberScaleY, 1.0f);
            queue.submitBlockModel(matrices, layer, chamberModel,
                    1f, 1f, 1f, light, OverlayTexture.NO_OVERLAY, 0);
            matrices.popPose();
        }

        matrices.popPose();
    }

    private static void applyFacingRotation(PoseStack matrices, Direction facing) {
        matrices.translate(0.5, 0.5, 0.5);
        switch (facing) {
            case DOWN -> matrices.mulPose(Axis.XP.rotationDegrees(180));
            case NORTH -> matrices.mulPose(Axis.XP.rotationDegrees(-90));
            case SOUTH -> matrices.mulPose(Axis.XP.rotationDegrees(90));
            case EAST -> matrices.mulPose(Axis.ZP.rotationDegrees(-90));
            case WEST -> matrices.mulPose(Axis.ZP.rotationDegrees(90));
            default -> {} // UP
        }
        matrices.translate(-0.5, -0.5, -0.5);
    }

    private static float[] trunkColor(State s) {
        // Creative engine uses purple/cyan theme
        // Always shows color (no "COLD" state since it's always ready)
        return switch (s.stage) {
            case COLD -> COLOR_CYAN;
            case COOL -> COLOR_CYAN;
            case WARM -> COLOR_PURPLE;
            case HOT -> COLOR_MAGENTA;
            case OVERHEAT -> COLOR_MAGENTA; // Creative can't overheat, but just in case
        };
    }
}