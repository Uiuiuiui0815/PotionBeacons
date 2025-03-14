package net.uiuiuiui0815.potionbeacons;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PotionBeaconBlock extends BlockWithEntity implements BlockEntityProvider {
    public static final MapCodec<PotionBeaconBlock> CODEC = PotionBeaconBlock.createCodec(PotionBeaconBlock::new);

    public MapCodec<PotionBeaconBlock> getCodec() {
        return CODEC;
    }

    public static final EnumProperty<DoubleBlockHalf> HALF = Properties.DOUBLE_BLOCK_HALF;

    Identifier id = Identifier.of(PotionBeacons.MOD_ID, "potion_beacon");
    RegistryKey<Block> key = RegistryKey.of(RegistryKeys.BLOCK, id);
    public static final PotionBeaconBlock POTION_BEACON_BLOCK = new PotionBeaconBlock(AbstractBlock.Settings.copy(Blocks.BEACON).registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(PotionBeacons.MOD_ID, "potion_beacon"))));

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
                    VoxelShapes.combine(createCuboidShape(0, 0, 0, 16, 12, 2),
                            createCuboidShape(0, 0, 0, 2, 12, 16), BooleanBiFunction.OR),
                    VoxelShapes.combine(createCuboidShape(0, 0, 14, 16, 12, 16),
                            createCuboidShape(14, 0, 0, 16, 12, 16), BooleanBiFunction.OR),
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

    public BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        DoubleBlockHalf doubleBlockHalf = state.get(HALF);
        if (direction.getAxis() == Direction.Axis.Y && doubleBlockHalf == DoubleBlockHalf.LOWER == (direction == Direction.UP)) {
            return neighborState.getBlock() instanceof PotionBeaconBlock && neighborState.get(HALF) != doubleBlockHalf ? neighborState.with(HALF, doubleBlockHalf) : Blocks.AIR.getDefaultState();
        } else {
            return doubleBlockHalf == DoubleBlockHalf.LOWER && direction == Direction.DOWN && !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
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
        if (blockPos.getY() < world.getTopYInclusive() && world.getBlockState(blockPos.up()).canReplace(ctx)) {
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
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (stack.isOf(Items.LINGERING_POTION)) {
            PotionContentsComponent potionContentsComponent = stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
            Iterable<StatusEffectInstance> potionEffects = potionContentsComponent.getEffects();
            BlockPos beaconPos = pos;
            if (state.get(HALF) == DoubleBlockHalf.UPPER) {
                beaconPos = pos.down();
            }
            if (world.getBlockEntity(beaconPos) instanceof PotionBeaconEntity beaconEntity) {
                List<PotionBeaconEffect> effectList = new ArrayList<>();
                for (StatusEffectInstance effectInstance : potionEffects){
                    if (effectInstance.getEffectType().value().isInstant()) return ActionResult.SUCCESS;
                    PotionBeaconEffect effect = new PotionBeaconEffect(effectInstance.getEffectType().value(), effectInstance.getAmplifier());
                    effectList.add(effect);
                }
                if (beaconEntity.charges > 3000 && beaconEntity.effects.equals(effectList)) return ActionResult.SUCCESS;
                beaconEntity.addEffects(effectList);
                if (!player.isCreative()) {
                    player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
                }
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
    }
}
