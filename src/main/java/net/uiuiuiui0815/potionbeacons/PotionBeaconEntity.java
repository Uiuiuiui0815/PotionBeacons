package net.uiuiuiui0815.potionbeacons;

import com.google.common.collect.ImmutableList;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Stainable;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
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
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

public class PotionBeaconEntity extends BlockEntity {
    List<BeamSegment> beamSegments = Lists.newArrayList();
    private List<BeamSegment> beamSegmentList = Lists.newArrayList();
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
            blockEntity.beamSegmentList = Lists.newArrayList();
            blockEntity.minY = blockPos.getY() - 1;
        } else {
            blockPos = new BlockPos(i, blockEntity.minY + 1, k);
        }

        BeamSegment beamSegment = blockEntity.beamSegmentList.isEmpty() ? null : blockEntity.beamSegmentList.getLast();
        int l = world.getTopY(Heightmap.Type.WORLD_SURFACE, i, k);

        int m;
        for (m = 0; m < 10 && blockPos.getY() <= l; ++m){
            BlockState blockState = world.getBlockState(blockPos);
            Block block = blockState.getBlock();
            if (block instanceof Stainable || block == PotionBeaconBlock.POTION_BEACON_BLOCK) {
                int n = blockEntity.color;
                if (blockEntity.beamSegmentList.size() <= 1) {
                    beamSegment = new BeamSegment(n);
                    blockEntity.beamSegmentList.add(beamSegment);
                } else if (beamSegment != null) {
                    if (n == beamSegment.color) {
                        beamSegment.increaseHeight();
                    } else {
                        beamSegment = new BeamSegment(ColorHelper.average(beamSegment.color, n));
                        blockEntity.beamSegmentList.add(beamSegment);
                    }
                }
            } else {
                if (beamSegment == null || blockState.getOpacity() >= 15 && !blockState.isOf(Blocks.BEDROCK)) {
                    blockEntity.beamSegmentList.clear();
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
                PotionBeaconEntity.applyPlayerEffects(world, pos, blockEntity.effects, blockEntity.level, blockEntity.charges);
                BeaconBlockEntity.playSound(world, pos, SoundEvents.BLOCK_BEACON_AMBIENT);
            }
            if (blockEntity.level == 4 && !blockEntity.beamSegments.isEmpty()) {
                blockEntity.charges = PotionBeaconEntity.updateCharges(world, pos, blockEntity.charges);
                world.updateListeners(pos, blockEntity.getCachedState(), blockEntity.getCachedState(), 0);
                if (blockEntity.charges == 0) {
                    blockEntity.effects.clear();
                    blockEntity.updateColor();
                }
            }
            blockEntity.markDirty();
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
    
    public void addEffects(List<PotionBeaconEffect> list){
        if (effects.equals(list)) {
            charges += 1500;
        }
        if (!effects.equals(list) || effects.isEmpty()) {
            effects = new ArrayList<>();
            effects.addAll(list);
            charges = 1500;
        }
        updateColor();
        this.markDirty();
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

    public List<BeamSegment> getBeamSegments() {
        return this.level == 0 ? ImmutableList.of() : this.beamSegments;
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket(){
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup wrapperLookup){
        return this.createNbt(wrapperLookup);
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapper){
        super.readNbt(nbt, wrapper);
        charges = nbt.getInt("Charges");
        NbtList nbtList = nbt.getList("Effects", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < nbtList.size(); i++){
            effects.add(new PotionBeaconEffect(nbtList.getCompound(i)));
        }
        color = nbt.getInt("Color");
        updateColor();
    }
    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapper){
        nbt.putInt("Level", level);
        nbt.putInt("Charges", charges);
        NbtList nbtList = new NbtList();
        for (PotionBeaconEffect effect : effects) {
            nbtList.add(effect.toNBT());
        }
        nbt.put("Effects", nbtList);
        nbt.putInt("Color", color);
        super.writeNbt(nbt, wrapper);
    }

    @Override
    public void setWorld(World world) {
        super.setWorld(world);
        this.minY = world.getBottomY() - 1;
    }

    public static class BeamSegment {
        final int color;
        private int height;
        public BeamSegment(int color){
            this.color = color;
            this.height = 1;
        }
        protected void increaseHeight(){
            ++this.height;
        }
        public int getColor() {
            return this.color;
        }
        public int getHeight() {
            return this.height;
        }
    }
}
