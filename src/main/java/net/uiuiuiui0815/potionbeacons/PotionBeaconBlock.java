package net.uiuiuiui0815.potionbeacons;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.PotionUtil;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PotionBeaconBlock extends BlockWithEntity implements BlockEntityProvider, Stainable {
    public static final MapCodec<PotionBeaconBlock> CODEC = PotionBeaconBlock.createCodec(PotionBeaconBlock::new);

    public MapCodec<PotionBeaconBlock> getCodec() {
        return CODEC;
    }

    public static final EnumProperty<DoubleBlockHalf> HALF = Properties.DOUBLE_BLOCK_HALF;
    public static final PotionBeaconBlock POTION_BEACON_BLOCK = new PotionBeaconBlock(FabricBlockSettings.copyOf(Blocks.BEACON).nonOpaque());

    public PotionBeaconBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(HALF, DoubleBlockHalf.LOWER));
    }


    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
        DoubleBlockHalf half = state.get(HALF);
        if (half == DoubleBlockHalf.LOWER) {
            return VoxelShapes.cuboid(0f, 0f, 0f, 1.0f, 1.0f, 1.0f);
        } else {
            return VoxelShapes.combineAndSimplify(
                    VoxelShapes.combine(createCuboidShape(0, 0, 0, 16, 13, 2),
                            createCuboidShape(0, 0, 0, 2, 13, 16), BooleanBiFunction.OR),
                    VoxelShapes.combine(createCuboidShape(0, 0, 14, 16, 13, 16),
                            createCuboidShape(14, 0, 0, 16, 13, 16), BooleanBiFunction.OR),
                    BooleanBiFunction.OR);
        }
    }

    protected static void onBreakInCreative(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        DoubleBlockHalf doubleBlockHalf = state.get(HALF);
        if (doubleBlockHalf == DoubleBlockHalf.UPPER) {
            BlockPos blockPos = pos.down();
            BlockState blockState = world.getBlockState(blockPos);
            if (blockState.isOf(state.getBlock()) && blockState.get(HALF) == DoubleBlockHalf.LOWER) {
                BlockState blockState2 = blockState.getFluidState().isOf(Fluids.WATER) ? Blocks.WATER.getDefaultState() : Blocks.AIR.getDefaultState();
                world.setBlockState(blockPos, blockState2, 35);
                world.syncWorldEvent(player, 2001, blockPos, Block.getRawIdFromState(blockState));
            }
        }
    }

    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        DoubleBlockHalf doubleBlockHalf = state.get(HALF);
        if (direction.getAxis() == Direction.Axis.Y && doubleBlockHalf == DoubleBlockHalf.LOWER == (direction == Direction.UP)) {
            return neighborState.getBlock() instanceof PotionBeaconBlock && neighborState.get(HALF) != doubleBlockHalf ? neighborState.with(HALF, doubleBlockHalf) : Blocks.AIR.getDefaultState();
        } else {
            return doubleBlockHalf == DoubleBlockHalf.LOWER && direction == Direction.DOWN && !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
        }
    }

    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient && (player.isCreative() || !player.canHarvest(state))) {
            onBreakInCreative(world, pos, state, player);
        }

        return super.onBreak(world, pos, state, player);
    }

    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos blockPos = ctx.getBlockPos();
        World world = ctx.getWorld();
        if (blockPos.getY() < world.getTopY() - 1 && world.getBlockState(blockPos.up()).canReplace(ctx)) {
            return this.getDefaultState().with(HALF, DoubleBlockHalf.LOWER);
        } else {
            return null;
        }
    }

    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        world.setBlockState(pos.up(), state.with(HALF, DoubleBlockHalf.UPPER), 3);
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(HALF);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        if (state.get(HALF) == DoubleBlockHalf.LOWER) {
            return new PotionBeaconEntity(pos, state);
        }
        return null;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return PotionBeaconBlock.validateTicker(type, PotionBeacons.POTION_BEACON_ENTITY, (world1, pos, state1, blockEntity) -> PotionBeaconEntity.tick(world1, pos, blockEntity));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack handStack = player.getStackInHand(hand);
        if (handStack.isOf(Items.LINGERING_POTION)) {
            BlockEntity blockEntity;
            List<StatusEffectInstance> list = PotionUtil.getPotionEffects(handStack);
            if (state.get(HALF) == DoubleBlockHalf.LOWER && (blockEntity = world.getBlockEntity(pos)) instanceof PotionBeaconEntity) {
                List<StatusEffect> effectList = new ArrayList<>();
                List<Integer> amplifierList = new ArrayList<>();
                for (StatusEffectInstance effectInstance : list){
                    if (effectInstance.getEffectType().isInstant()) return ActionResult.PASS;
                    effectList.add(effectInstance.getEffectType());
                    amplifierList.add(effectInstance.getAmplifier());
                }
                ((PotionBeaconEntity) blockEntity).addEffects(effectList, amplifierList);
                player.setStackInHand(hand, ItemUsage.exchangeStack(handStack, player, new ItemStack(Items.GLASS_BOTTLE)));
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (random.nextInt(2) != 0){
            return;
        }
        Direction direction = Direction.random(random);
        if (direction == Direction.UP){
            return;
        }
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity == null ||
                ((PotionBeaconEntity) blockEntity).effects.isEmpty() ||
                ((PotionBeaconEntity) blockEntity).beamSegments.isEmpty() ||
                ((PotionBeaconEntity) blockEntity).level < 4 ||
                ((PotionBeaconEntity) blockEntity).charges <= 0) {
            return;
        }
        BlockPos blockPos = pos.offset(direction);
        BlockState blockState = world.getBlockState(blockPos);
        if (state.isOpaque() && blockState.isSideSolidFullSquare(world, blockPos, direction.getOpposite())) {
            return;
        }
        double d = direction.getOffsetX() == 0 ? random.nextDouble() : 0.5 + (double)direction.getOffsetX() * 0.6;
        double e = direction.getOffsetY() == 0 ? random.nextDouble() : 0.5 + (double)direction.getOffsetY() * 0.6;
        double f = direction.getOffsetZ() == 0 ? random.nextDouble() : 0.5 + (double)direction.getOffsetZ() * 0.6;
        world.addParticle(ParticleTypes.EFFECT, (double) pos.getX() + d, (double)pos.getY() + e, (double)pos.getZ() + f, 0,0,0);
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.WHITE;
    }
}
