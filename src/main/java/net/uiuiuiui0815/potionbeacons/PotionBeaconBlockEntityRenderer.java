package net.uiuiuiui0815.potionbeacons;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.*;

@Environment(EnvType.CLIENT)
public class PotionBeaconBlockEntityRenderer implements BlockEntityRenderer<PotionBeaconEntity> {
    private final BeaconBlockEntityRenderer<PotionBeaconEntity> beaconRenderer;

    public PotionBeaconBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        beaconRenderer = new BeaconBlockEntityRenderer<>(ctx);
    }

    public void render(PotionBeaconEntity entity, float tickProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Vec3d cameraPos) {
        beaconRenderer.render(entity, tickProgress, matrices, vertexConsumers, light, overlay, cameraPos);
        if (entity.charges <= 0) {
            return;
        }

        int color = entity.getColor();
        Sprite sprite = FluidVariantRendering.getSprites(FluidVariant.of(Fluids.WATER))[0];
        RenderLayer potionLayer = RenderLayer.getEntityTranslucent(sprite.getAtlasId());
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(potionLayer);
        float y2 = (float) Math.min(Math.ceil((double) entity.charges / 1500) * 3 / 16 + 9 / 8f, 27f / 16);
        float minU = sprite.getFrameU(2 / 16f);
        float maxU = sprite.getFrameU(14 / 16f);
        float minV = sprite.getFrameV(2 / 16f);
        float maxV = sprite.getFrameV(14 / 16f);
        MatrixStack.Entry entry = matrices.peek();
        vertexConsumer.vertex(entry, 2 / 16f, y2, 2 / 16f)
                .color(color)
                .texture(minU, minV)
                .light(light)
                .overlay(overlay)
                .normal(0, 1, 0);
        vertexConsumer.vertex(entry, 14 / 16f, y2, 2 / 16f)
                .color(color)
                .texture(maxU, minV)
                .light(light)
                .overlay(overlay)
                .normal(0, 1, 0);
        vertexConsumer.vertex(entry, 14 / 16f, y2, 14 / 16f)
                .color(color)
                .texture(maxU, maxV)
                .light(light)
                .overlay(overlay)
                .normal(0, 1, 0);
        vertexConsumer.vertex(entry, 2 / 16f, y2, 14 / 16f)
                .color(color)
                .texture(minU, maxV)
                .light(light)
                .overlay(overlay)
                .normal(0, 1, 0);
        vertexConsumer.vertex(entry, 2 / 16f, 1, 2 / 16f)
                .color(color)
                .texture(minU, minV)
                .light(light)
                .overlay(overlay)
                .normal(0, 1, 0);
        vertexConsumer.vertex(entry, 14 / 16f, 1, 2 / 16f)
                .color(color)
                .texture(maxU, minV)
                .light(light)
                .overlay(overlay)
                .normal(0, 1, 0);
        vertexConsumer.vertex(entry, 14 / 16f, 1, 14 / 16f)
                .color(color)
                .texture(maxU, maxV)
                .light(light)
                .overlay(overlay)
                .normal(0, 1, 0);
        vertexConsumer.vertex(entry, 2 / 16f, 1, 14 / 16f)
                .color(color)
                .texture(minU, maxV)
                .light(light)
                .overlay(overlay)
                .normal(0, 1, 0);
    }

    public boolean rendersOutsideBoundingBox(PotionBeaconEntity blockEntity) {
        return beaconRenderer.rendersOutsideBoundingBox(blockEntity);
    }

    public int getRenderDistance() {
        return beaconRenderer.getRenderDistance();
    }

    public boolean isInRenderDistance(PotionBeaconEntity blockEntity, Vec3d pos) {
        return this.beaconRenderer.isInRenderDistance(blockEntity, pos);
    }
}