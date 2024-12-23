package net.uiuiuiui0815.potionbeacons;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

import java.util.List;

@Environment(EnvType.CLIENT)
public class PotionBeaconBlockEntityRenderer implements BlockEntityRenderer<PotionBeaconEntity> {
    public static final Identifier BEAM_TEXTURE = Identifier.ofVanilla("textures/entity/beacon_beam.png");
    public static final int MAX_BEAM_HEIGHT = 1024;

    public PotionBeaconBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
    }

    public void render(PotionBeaconEntity potionBeaconEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j) {
        long l = potionBeaconEntity.getWorld().getTime();
        List<PotionBeaconEntity.BeamSegment> list = potionBeaconEntity.getBeamSegments();
        int k = 0;

        for(int m = 0; m < list.size(); ++m) {
            PotionBeaconEntity.BeamSegment beamSegment = list.get(m);
            renderBeam(matrixStack, vertexConsumerProvider, f, l, k, m == list.size() - 1 ? MAX_BEAM_HEIGHT : beamSegment.getHeight(), beamSegment.getColor());
            k += beamSegment.getHeight();
        }

        if(potionBeaconEntity.charges <= 0) {
            return;
        }
        int color = potionBeaconEntity.getColor();
        Sprite sprite = FluidVariantRendering.getSprites(FluidVariant.of(Fluids.WATER))[0];
        RenderLayer potionLayer = RenderLayer.getEntityTranslucent(sprite.getAtlasId());
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(potionLayer);
        float y2 = (float) Math.ceil((double) potionBeaconEntity.charges / 1500)*3/16+9/8f;
        float minU = sprite.getFrameU(2/16f);
        float maxU = sprite.getFrameU(14/16f);
        float minV = sprite.getFrameV(2/16f);
        float maxV = sprite.getFrameV(14/16f);
        MatrixStack.Entry entry = matrixStack.peek();
        vertexConsumer.vertex(entry, 2/16f, y2, 2/16f)
                .color(color)
                .texture(minU, minV)
                .light(i)
                .overlay(j)
                .normal(0,1,0);
        vertexConsumer.vertex(entry, 14/16f, y2, 2/16f)
                .color(color)
                .texture(maxU, minV)
                .light(i)
                .overlay(j)
                .normal(0,1,0);
        vertexConsumer.vertex(entry, 14/16f, y2, 14/16f)
                .color(color)
                .texture(maxU, maxV)
                .light(i)
                .overlay(j)
                .normal(0,1,0);
        vertexConsumer.vertex(entry, 2/16f, y2, 14/16f)
                .color(color)
                .texture(minU, maxV)
                .light(i)
                .overlay(j)
                .normal(0,1,0);
        vertexConsumer.vertex(entry, 2/16f, 1, 2/16f)
                .color(color)
                .texture(minU, minV)
                .light(i)
                .overlay(j)
                .normal(0,1,0);
        vertexConsumer.vertex(entry, 14/16f, 1, 2/16f)
                .color(color)
                .texture(maxU, minV)
                .light(i)
                .overlay(j)
                .normal(0,1,0);
        vertexConsumer.vertex(entry, 14/16f, 1, 14/16f)
                .color(color)
                .texture(maxU, maxV)
                .light(i)
                .overlay(j)
                .normal(0,1,0);
        vertexConsumer.vertex(entry, 2/16f, 1, 14/16f)
                .color(color)
                .texture(minU, maxV)
                .light(i)
                .overlay(j)
                .normal(0,1,0);
    }

    private static void renderBeam(MatrixStack matrices, VertexConsumerProvider vertexConsumers, float tickDelta, long worldTime, int yOffset, int maxY, int color) {
        renderBeam(matrices, vertexConsumers, BEAM_TEXTURE, tickDelta, 1.0F, worldTime, yOffset, maxY, color, 0.2F, 0.25F);
    }

    public static void renderBeam(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Identifier textureId, float tickDelta, float heightScale, long worldTime, int yOffset, int maxY, int color, float innerRadius, float outerRadius) {
        int i = yOffset + maxY;
        matrices.push();
        matrices.translate(0.5, 0.0, 0.5);
        float f = (float)Math.floorMod(worldTime, 40) + tickDelta;
        float g = maxY < 0 ? f : -f;
        float h = MathHelper.fractionalPart(g * 0.2F - (float)MathHelper.floor(g * 0.1F));
        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(f * 2.25F - 45.0F));
        float j;
        float m;
        float n = -innerRadius;
        float q = -innerRadius;
        float t = -1.0F + h;
        float u = (float)maxY * heightScale * (0.5F / innerRadius) + t;
        renderBeamLayer(matrices, vertexConsumers.getBuffer(RenderLayer.getBeaconBeam(textureId, false)), color, yOffset, i, 0.0F, innerRadius, innerRadius, 0.0F, n, 0.0F, 0.0F, q, u, t);
        matrices.pop();
        j = -outerRadius;
        float k = -outerRadius;
        m = -outerRadius;
        n = -outerRadius;
        t = -1.0F + h;
        u = (float)maxY * heightScale + t;
        renderBeamLayer(matrices, vertexConsumers.getBuffer(RenderLayer.getBeaconBeam(textureId, true)), ColorHelper.withAlpha(32, color), yOffset, i, j, k, outerRadius, m, n, outerRadius, outerRadius, outerRadius, u, t);
        matrices.pop();
    }

    private static void renderBeamLayer(MatrixStack matrices, VertexConsumer vertices, int color, int yOffset, int height, float x1, float z1, float x2, float z2, float x3, float z3, float x4, float z4, float v1, float v2) {
        MatrixStack.Entry entry = matrices.peek();
        renderBeamFace(entry, vertices, color, yOffset, height, x1, z1, x2, z2, v1, v2);
        renderBeamFace(entry, vertices, color, yOffset, height, x4, z4, x3, z3, v1, v2);
        renderBeamFace(entry, vertices, color, yOffset, height, x2, z2, x4, z4, v1, v2);
        renderBeamFace(entry, vertices, color, yOffset, height, x3, z3, x1, z1, v1, v2);
    }

    private static void renderBeamFace(MatrixStack.Entry matrix, VertexConsumer vertices, int color, int yOffset, int height, float x1, float z1, float x2, float z2, float v1, float v2) {
        renderBeamVertex(matrix, vertices, color, height, x1, z1, (float) 1.0, v1);
        renderBeamVertex(matrix, vertices, color, yOffset, x1, z1, (float) 1.0, v2);
        renderBeamVertex(matrix, vertices, color, yOffset, x2, z2, (float) 0.0, v2);
        renderBeamVertex(matrix, vertices, color, height, x2, z2, (float) 0.0, v1);
    }

    private static void renderBeamVertex(MatrixStack.Entry matrix, VertexConsumer vertices, int color, int y, float x, float z, float u, float v) {
        vertices.vertex(matrix, x, (float)y, z).color(color).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(matrix, 0.0F, 1.0F, 0.0F);
    }

    public boolean rendersOutsideBoundingBox(PotionBeaconEntity beaconBlockEntity) {
        return true;
    }

    public int getRenderDistance() {
        return 256;
    }

    public boolean isInRenderDistance(PotionBeaconEntity beaconBlockEntity, Vec3d vec3d) {
        return Vec3d.ofCenter(beaconBlockEntity.getPos()).multiply(1.0, 0.0, 1.0).isInRange(vec3d.multiply(1.0, 0.0, 1.0), this.getRenderDistance());
    }
}
