package com.logistics.power.engine.block;

import com.logistics.LogisticsPower;
import com.logistics.core.lib.block.Probeable;
import com.logistics.core.lib.block.Wrenchable;
import com.logistics.core.lib.support.ProbeResult;
import com.logistics.power.engine.block.entity.StirlingEngineBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public final class StirlingEngineBlock extends BaseEntityBlock implements Wrenchable, Probeable {

    public static final MapCodec<StirlingEngineBlock> CODEC = simpleCodec(StirlingEngineBlock::new);

    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public StirlingEngineBlock(Properties props) {
        super(props);
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(POWERED, Boolean.FALSE)
        );
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    // --- Placement / facing ---
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction facing = ctx.getNearestLookingDirection().getOpposite();
        boolean powered = hasDirectRedstoneSignal(ctx.getLevel(), ctx.getClickedPos());
        return defaultBlockState()
                .setValue(FACING, facing)
                .setValue(POWERED, powered);
    }

    @Override
    public InteractionResult onWrench(Level world, BlockPos pos, Player player) {
        BlockState state = world.getBlockState(pos);

        if (!world.isClientSide()) {
            // Check if engine is overheated and clear it if so
            if (world.getBlockEntity(pos) instanceof StirlingEngineBlockEntity be && be.isOverheated()) {
                be.clearOverheated();
                return InteractionResult.SUCCESS;
            }

            // Otherwise, rotate the engine
            Direction facing = state.getValue(FACING);
            Direction next = nextFacing(facing);
            world.setBlock(pos, state.setValue(FACING, next), Block.UPDATE_CLIENTS);
        }

        return InteractionResult.SUCCESS;
    }

    private static Direction nextFacing(Direction d) {
        return switch (d) {
            case NORTH -> Direction.EAST;
            case EAST -> Direction.SOUTH;
            case SOUTH -> Direction.WEST;
            case WEST -> Direction.UP;
            case UP -> Direction.DOWN;
            case DOWN -> Direction.NORTH;
        };
    }

    private static boolean hasDirectRedstoneSignal(Level level, BlockPos pos) {
        return level.getDirectSignalTo(pos) > 0;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }
    // --- Redstone updates ---

    @Override
    public void neighborChanged(
            BlockState state,
            Level level,
            BlockPos pos,
            Block block,
            @Nullable Orientation wireOrientation,
            boolean isMoving
    ) {
        if (level.isClientSide()) return;

        boolean powered = hasDirectRedstoneSignal(level, pos);
        if (powered != state.getValue(POWERED)) {
            level.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_CLIENTS);
        }
    }
    // --- Block entity wiring ---

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StirlingEngineBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        if (level.isClientSide()) return null;

        return createTickerHelper(
                type,
                LogisticsPower.ENTITY.STIRLING_ENGINE_BLOCK_ENTITY,
                StirlingEngineBlockEntity::serverTick
        );
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level world,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        return openGui(world, pos, player);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        // Empty hand: open GUI
        return openGui(world, pos, player);
    }

    private InteractionResult openGui(Level world, BlockPos pos, Player player) {
        if (!world.isClientSide()) {
            BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof StirlingEngineBlockEntity stirlingEngine) {
                player.openMenu(stirlingEngine);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public ProbeResult onProbe(Level world, BlockPos pos, Player player) {
        if (world.getBlockEntity(pos) instanceof StirlingEngineBlockEntity be) {
            return be.getProbeResult();
        }
        return null;
    }
}