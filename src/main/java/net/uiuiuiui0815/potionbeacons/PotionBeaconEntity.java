package net.uiuiuiui0815.potionbeacons;

import com.google.common.collect.ImmutableList;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Stainable;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import org.apache.commons.compress.utils.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class PotionBeaconEntity extends BlockEntity {
    List<BeamSegment> beamSegments = Lists.newArrayList();
    private List<BeamSegment> field_19178 = Lists.newArrayList();
    int level;
    private int minY;
    List<StatusEffect> effects;
    List<Integer> amplifiers;
    int charges;

    public PotionBeaconEntity(BlockPos pos, BlockState state) {
        super(PotionBeacons.POTION_BEACON_ENTITY, pos, state);
        effects = new ArrayList<StatusEffect>();
        amplifiers = new ArrayList<Integer>();
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
                PotionBeaconEntity.applyPlayerEffects(world, pos, blockEntity.effects, blockEntity.amplifiers, blockEntity.level, blockEntity.charges);
                BeaconBlockEntity.playSound(world, pos, SoundEvents.BLOCK_BEACON_AMBIENT);
            }
            if (blockEntity.level >= 4 && !blockEntity.beamSegments.isEmpty()) {
                blockEntity.charges = PotionBeaconEntity.updateCharges(world, pos, blockEntity.charges);
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
        assert this.world != null;
        BeaconBlockEntity.playSound(this.world, this.pos, SoundEvents.BLOCK_BEACON_DEACTIVATE);
        super.markRemoved();
    }

    public void addEffects(List<StatusEffect> effectList, List<Integer> amplifiers){
        effects = new ArrayList<>();
        this.amplifiers = new ArrayList<>();
        effects.addAll(effectList);
        this.amplifiers.addAll(amplifiers);
        charges = 1500;
        this.markDirty();
    }

    private static void applyPlayerEffects(World world, BlockPos pos, List<StatusEffect> effects, List<Integer> amplifiers, int level, int charges){
        if (charges <=0 || level < 4 || world.isClient) {
            return;
        }
        Box box = new Box(pos).expand(50).stretch(0.0, world.getHeight(), 0.0);
        List<PlayerEntity> list = world.getNonSpectatingEntities(PlayerEntity.class, box);
        for (PlayerEntity playerEntity : list) {
            playerEntity.sendMessage(Text.of(String.valueOf(charges)));
            for (int i=0; i < effects.size(); i++){
                playerEntity.addStatusEffect(new StatusEffectInstance(effects.get(i), 340, amplifiers.get(i), true, true));
            }
        }
    }

    private static int updateCharges(World world, BlockPos pos, int charges){
        Box box = new Box(pos).expand(50).stretch(0.0, world.getHeight(), 0.0);
        List<PlayerEntity> list = world.getNonSpectatingEntities(PlayerEntity.class, box);
        return charges - list.size();
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
        charges = nbt.getInt("Charges");
        NbtList nbtList = nbt.getList("Effects", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < nbtList.size(); i++){
            NbtCompound compound = nbtList.getCompound(i);
            String s = compound.getString("Effect" + i);
            StatusEffect statusEffect = Registries.STATUS_EFFECT.get(Identifier.tryParse(s));
            effects.add(i, statusEffect);
        }
        List<Integer> integerList = Arrays.stream(nbt.getIntArray("Amplifiers")).boxed().toList();
        amplifiers.addAll(integerList);
    }
    @Override
    protected void writeNbt(NbtCompound nbt){
        nbt.putInt("Levels", level);
        nbt.putInt("Charges", charges);
        NbtList nbtList = new NbtList();
        for (int i = 0; i < effects.size(); i++){
            String s = (Objects.requireNonNull(Registries.STATUS_EFFECT.getId(effects.get(i)))).toString();
            if (s != null){
                NbtCompound compound = new NbtCompound();
                compound.putString("Effect" + i, s);
                nbtList.add(compound);
            }
        }
        nbt.put("Effects", nbtList);
        nbt.putIntArray("Amplifiers", amplifiers);
        super.writeNbt(nbt);
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
