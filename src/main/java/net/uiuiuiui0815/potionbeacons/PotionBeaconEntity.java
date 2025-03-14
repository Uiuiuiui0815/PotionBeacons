package net.uiuiuiui0815.potionbeacons;

import com.google.common.collect.ImmutableList;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Stainable;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BeamEmitter;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.util.*;

public class PotionBeaconEntity extends BlockEntity implements BeamEmitter {
    List<BeamEmitter.BeamSegment> beamSegments = new ArrayList<>();
    private List<BeamEmitter.BeamSegment> beamSegmentList = new ArrayList<>();
    int level;
    private int minY;
    List<PotionBeaconEffect> effects;
    int charges;
    private int color;

    public int getColor() {
        return color;
    }

    public PotionBeaconEntity(BlockPos pos, BlockState state) {
        super(PotionBeacons.POTION_BEACON_ENTITY, pos, state);
        effects = new ArrayList<>();
    }

    public static void tick(World world, BlockPos pos, PotionBeaconEntity blockEntity){
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        BlockPos blockPos;
        if (blockEntity.minY < j){
            blockPos = pos;
            blockEntity.beamSegmentList = new ArrayList<>();
            blockEntity.minY = blockPos.getY() - 1;
        } else {
            blockPos = new BlockPos(i, blockEntity.minY + 1, k);
        }

        BeamEmitter.BeamSegment beamSegment = blockEntity.beamSegmentList.isEmpty() ? null : blockEntity.beamSegmentList.getLast();
        int l = world.getTopY(Heightmap.Type.WORLD_SURFACE, i, k);

        for (int m = 0; m < 10 && blockPos.getY() <= l; ++m){
            BlockState blockState = world.getBlockState(blockPos);
            Block block = blockState.getBlock();
            if (block instanceof Stainable || block == PotionBeaconBlock.POTION_BEACON_BLOCK) {
                int n = blockEntity.color;
                if (blockEntity.beamSegmentList.size() <= 1) {
                    beamSegment = new BeamEmitter.BeamSegment(n);
                    blockEntity.beamSegmentList.add(beamSegment);
                } else if (beamSegment != null) {
                    if (n == beamSegment.getColor()) {
                        beamSegment.increaseHeight();
                    } else {
                        beamSegment = new BeamEmitter.BeamSegment(ColorHelper.average(beamSegment.getColor(), n));
                        blockEntity.beamSegmentList.add(beamSegment);
                    }
                }
            } else {
                if (beamSegment == null || blockState.getOpacity() >= 15 && !blockState.isOf(Blocks.BEDROCK)) {
                    blockEntity.beamSegmentList.clear();
                    blockEntity.minY = l;
                    break;
                }
                beamSegment.increaseHeight();
            }

            blockPos = blockPos.up();
            ++blockEntity.minY;
        }

        int m = blockEntity.level;
        if (world.getTime() % 80L == 0L) {
            if (!blockEntity.beamSegments.isEmpty()) {
                blockEntity.level = PotionBeaconEntity.updateLevel(world, i, j, k);
            }
            if (blockEntity.level > 0 && !blockEntity.beamSegments.isEmpty()) {
                PotionBeaconEntity.applyPlayerEffects(world, pos, blockEntity.effects, blockEntity.level, blockEntity.charges);
                BeaconBlockEntity.playSound(world, pos, SoundEvents.BLOCK_BEACON_AMBIENT);
            }
            if (blockEntity.level == 4 && !blockEntity.beamSegments.isEmpty()) {
                blockEntity.charges = PotionBeaconEntity.updateCharges(world, pos, blockEntity.charges);
                if (blockEntity.charges == 0) {
                    blockEntity.effects.clear();
                    blockEntity.updateColor();
                }
            }
            blockEntity.markDirty();
            world.updateListeners(pos, blockEntity.getCachedState(), blockEntity.getCachedState(), 0);
        }

        if (blockEntity.minY >= l) {
            blockEntity.minY = world.getBottomY() - 1;
            boolean bl = m > 0;
            blockEntity.beamSegments = blockEntity.beamSegmentList;
            if (!world.isClient) {
                boolean bl2 = blockEntity.level > 0;
                if (!bl && bl2) {
                    BeaconBlockEntity.playSound(world, pos, SoundEvents.BLOCK_BEACON_ACTIVATE);

                    for (ServerPlayerEntity serverPlayerEntity : world.getNonSpectatingEntities(ServerPlayerEntity.class, (new Box(i, j, k, i, j - 4, k)).expand(10, 5, 10))) {
                        Criteria.CONSTRUCT_BEACON.trigger(serverPlayerEntity, blockEntity.level);
                    }
                } else if (bl && !bl2){
                    BeaconBlockEntity.playSound(world, pos, SoundEvents.BLOCK_BEACON_DEACTIVATE);
                }
            }
        }
    }

    private void updateColor(){
        OptionalInt optionalColor;
        List<StatusEffectInstance> instances = new ArrayList<>();
        for(PotionBeaconEffect potionBeaconEffect : effects){
            instances.add(potionBeaconEffect.createStatusEffectInstance());
        }
        optionalColor = PotionContentsComponent.mixColors(instances);
        color = optionalColor.orElse(-1);
        this.markDirty();
        world.updateListeners(pos, this.getCachedState(), this.getCachedState(), 0);
    }

    private static int updateLevel(World world, int x, int y, int z) {
        int i = 0;
        for(int j = 1; j <= 4; i = j++) {
            int k = y - j;
            if (k < world.getBottomY()) {
                break;
            }
            boolean bl = true;
            for(int l = x - j; l <= x + j && bl; ++l) {
                for(int m = z - j; m <= z + j; ++m) {
                    if (!world.getBlockState(new BlockPos(l, k, m)).isIn(BlockTags.BEACON_BASE_BLOCKS)) {
                        bl = false;
                        break;
                    }
                }
            }
            if (!bl) {
                break;
            }
        }
        return i;
    }

    @Override
    public void markRemoved(){
        assert this.world != null;
        BeaconBlockEntity.playSound(this.world, this.pos, SoundEvents.BLOCK_BEACON_DEACTIVATE);
        super.markRemoved();
    }
    
    public void addEffects(List<PotionBeaconEffect> list){
        if (!effects.equals(list) || effects.isEmpty()) {
            effects = new ArrayList<>();
            effects.addAll(list);
            charges = 1500;
            if (effects.isEmpty()) charges = 0;
        } else charges += 1500;
        updateColor();
    }

    private static void applyPlayerEffects(World world, BlockPos pos, List<PotionBeaconEffect> effects, int level, int charges){
        if (charges <=0 || level < 4 || world.isClient) {
            return;
        }
        Box box = new Box(pos).expand(50).stretch(0.0, world.getHeight(), 0.0);
        List<PlayerEntity> list = world.getNonSpectatingEntities(PlayerEntity.class, box);
        for (PlayerEntity playerEntity : list) {
            for (PotionBeaconEffect effect : effects) {
                playerEntity.addStatusEffect(effect.createStatusEffectInstance());
            }
        }
    }

    private static int updateCharges(World world, BlockPos pos, int charges){
        Box box = new Box(pos).expand(50).stretch(0.0, world.getHeight(), 0.0);
        List<PlayerEntity> list = world.getNonSpectatingEntities(PlayerEntity.class, box);
        return Math.max(charges - list.size(), 0);
    }

    public List<BeamEmitter.BeamSegment> getBeamSegments() {
        return this.level == 0 ? ImmutableList.of() : this.beamSegments;
    }

    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        return this.createComponentlessNbt(registries);
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapper){
        super.readNbt(nbt, wrapper);
        charges = nbt.getInt("charges", 0);
        NbtList nbtList = nbt.getListOrEmpty("effects");
        for (int i = 0; i < nbtList.size(); i++){
            effects.add(new PotionBeaconEffect(nbtList.getCompoundOrEmpty(i)));
        }
        color = nbt.getInt("color", -1);
        updateColor();
    }
    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapper){
        nbt.putInt("level", level);
        nbt.putInt("charges", charges);
        NbtList nbtList = new NbtList();
        for (PotionBeaconEffect effect : effects) {
            nbtList.add(effect.toNBT());
        }
        nbt.put("effects", nbtList);
        nbt.putInt("color", color);
        super.writeNbt(nbt, wrapper);
    }

    public void setWorld(World world) {
        super.setWorld(world);
        this.minY = world.getBottomY() - 1;
    }
}