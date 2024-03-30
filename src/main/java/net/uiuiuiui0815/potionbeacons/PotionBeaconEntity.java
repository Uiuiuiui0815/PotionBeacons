package net.uiuiuiui0815.potionbeacons;

import com.google.common.collect.ImmutableList;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Stainable;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class PotionBeaconEntity extends BlockEntity {
    List<BeamSegment> beamSegments = Lists.newArrayList();
    private List<BeamSegment> field_19178 = Lists.newArrayList();
    int level;
    private int minY;
    @Nullable List<PotionBeaconEffect> effects;

    public PotionBeaconEntity(BlockPos pos, BlockState state) {
        super(PotionBeacons.POTION_BEACON_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, PotionBeaconEntity blockEntity){
        int m;
        BlockPos blockPos;
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        if (blockEntity.minY < j){
            blockPos = pos;
            blockEntity.field_19178 = Lists.newArrayList();
            blockEntity.minY = blockPos.getY() - 1;
        } else {
            blockPos = new BlockPos(i, blockEntity.minY + 1, k);
        }
        BeamSegment beamSegment = blockEntity.field_19178.isEmpty() ? null : blockEntity.field_19178.get(blockEntity.field_19178.size() - 1);
        int l = world.getTopY(Heightmap.Type.WORLD_SURFACE, i, k);
        for (m = 0; m < 10 && blockPos.getY() <= l; ++m){
            block18: {
                BlockState blockState;
                block16: {
                    float[] fs;
                    block17: {
                        blockState = world.getBlockState(blockPos);
                        Block block = blockState.getBlock();
                        if (!(block instanceof Stainable)) break block16;
                        fs = ((Stainable) block).getColor().getColorComponents();
                        if (blockEntity.field_19178.size() > 1) break block17;
                        beamSegment = new BeamSegment(fs);
                        blockEntity.field_19178.add(beamSegment);
                        break block18;
                    }
                    if (beamSegment == null) break block18;
                    if (Arrays.equals(fs, beamSegment.color)) {
                        beamSegment.increaseHeight();
                    } else {
                        beamSegment = new BeamSegment(new float[]{(beamSegment.color[0] + fs[0]) / 2.0f, (beamSegment.color[1] + fs[1]) / 2.0f, (beamSegment.color[2] + fs[2]) / 2.0f});
                        blockEntity.field_19178.add(beamSegment);
                    }
                    break block18;
                }
                if (beamSegment != null && (blockState.getOpacity(world, blockPos) < 15 || blockState.isOf(Blocks.BEDROCK))) {
                    beamSegment.increaseHeight();
                } else {
                    blockEntity.field_19178.clear();
                    blockEntity.minY = l;
                    break;
                }
            }
            blockPos = blockPos.up();
            ++blockEntity.minY;
        }
        m = blockEntity.level;
        if (world.getTime() % 80L == 0L) {
            if (!blockEntity.beamSegments.isEmpty()) {
                blockEntity.level = PotionBeaconEntity.updateLevel(world, i, j, k);
            }
            if (blockEntity.level > 0 && !blockEntity.beamSegments.isEmpty()) {
                PotionBeaconEntity.applyPlayerEffects(world, pos, blockEntity.level, blockEntity.effects);
                BeaconBlockEntity.playSound(world, pos, SoundEvents.BLOCK_BEACON_AMBIENT);
            }
        }
        if (blockEntity.minY >= l) {
            blockEntity.minY = world.getBottomY() - 1;
            boolean bl = m > 0;
            blockEntity.beamSegments = blockEntity.field_19178;
            if (!world.isClient) {
                boolean bl2 = blockEntity.level > 0;
                if (!bl && bl2) {
                    BeaconBlockEntity.playSound(world, pos, SoundEvents.BLOCK_BEACON_ACTIVATE);
                    for (ServerPlayerEntity serverPlayerEntity : world.getNonSpectatingEntities(ServerPlayerEntity.class, new Box(i, j, k, i, j - 4, k).expand(10, 5, 10))) {
                        Criteria.CONSTRUCT_BEACON.trigger(serverPlayerEntity, blockEntity.level);
                    }
                } else if (bl && !bl2){
                    BeaconBlockEntity.playSound(world, pos, SoundEvents.BLOCK_BEACON_DEACTIVATE);
                }
            }
        }
    }

    private static int updateLevel(World world, int x, int y, int z){
        int k;
        int i = 0;
        int j = 1;
        while (j <= 4 && (k = y -j) >= world.getBottomY()) {
            boolean bl = true;
            block1: for (int l = x - j; l <= x + j && bl; ++l){
                for (int m = z - j; m <= z + j; ++m){
                    if (world.getBlockState(new BlockPos(l, k, m)).isIn(BlockTags.BEACON_BASE_BLOCKS)) continue;
                    bl = false;
                    continue block1;
                }
            }
            if (!bl) break;
            i = j++;
        }
        return i;
    }

    @Override
    public void markRemoved(){
        BeaconBlockEntity.playSound(this.world, this.pos, SoundEvents.BLOCK_BEACON_DEACTIVATE);
        super.markRemoved();
    }

    public void addEffect(PotionBeaconEffect effect){
        if (effects != null) {
            effects.add(effect);
        }
    }

    private static void applyPlayerEffects(World world, BlockPos pos, int beaconLevel, @Nullable List<PotionBeaconEffect> effects){
        if (world.isClient || effects == null) {
            return;
        }
        double d = 4 * 10 +10;
        Box box = new Box(pos).expand(d).stretch(0.0, world.getHeight(), 0.0);
        List<PlayerEntity> list = world.getNonSpectatingEntities(PlayerEntity.class, box);
        if (beaconLevel >= 4) {
            for (PlayerEntity playerEntity : list) {
                for (PotionBeaconEffect effect : effects){
                    playerEntity.addStatusEffect(effect.createStatusEffectInstance((9 + 4 * 2) * 20));
                }
            }
        }

    }

    public List<BeamSegment> getBeamSegments() {
        return this.level == 0 ? ImmutableList.of() : this.beamSegments;
    }

    public BlockEntityUpdateS2CPacket toUpdatePacket(){
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(){
        return this.createNbt();
    }

    @Override
    public void readNbt(NbtCompound nbt){
        super.readNbt(nbt);
        this.effects = (List<PotionBeaconEffect>) nbt.get("Effect");
    }
    @Override
    protected void writeNbt(NbtCompound nbt){
        super.writeNbt(nbt);
        nbt.putInt("Levels", this.level);
        if (effects != null) {
            for (PotionBeaconEffect statusEffect : effects) {
                nbt.put("Effects", (NbtElement) statusEffect);
            }
        }
    }

    @Override
    public void setWorld(World world) {
        super.setWorld(world);
        this.minY = world.getBottomY() - 1;
    }

    public static class BeamSegment {
        final float[] color;
        private int height;
        public BeamSegment(float[] color){
            this.color = color;
            this.height = 1;
        }
        protected void increaseHeight(){
            ++this.height;
        }
        public float[] getColor() {
            return this.color;
        }
        public int getHeight() {
            return this.height;
        }
    }
}
