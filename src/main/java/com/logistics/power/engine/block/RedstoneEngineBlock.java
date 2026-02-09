package com.logistics.power.engine.block;

import com.logistics.LogisticsPower;
import com.logistics.core.lib.block.Probeable;
import com.logistics.core.lib.block.Wrenchable;
import com.logistics.core.lib.support.ProbeResult;
import com.logistics.power.engine.block.entity.RedstoneEngineBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import org.jetbrains.annotations.Nullable;

public final class RedstoneEngineBlock extends BaseEntityBlock implements Wrenchable, Probeable {

    public static final MapCodec<RedstoneEngineBlock> CODEC = simpleCodec(RedstoneEngineBlock::new);

    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public RedstoneEngineBlock(Properties props) {
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
        return new RedstoneEngineBlockEntity(pos, state);
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
                LogisticsPower.ENTITY.REDSTONE_ENGINE_BLOCK_ENTITY,
                RedstoneEngineBlockEntity::serverTick
        );
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public ProbeResult onProbe(Level world, BlockPos pos, Player player) {
        if (world.getBlockEntity(pos) instanceof RedstoneEngineBlockEntity be) {
            return be.getProbeResult();
        }
        return null;
    }
}