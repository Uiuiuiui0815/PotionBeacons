package net.uiuiuiui0815.potionbeacons;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

@Environment(EnvType.CLIENT)
public class PotionBeaconsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(PotionBeaconBlock.POTION_BEACON_BLOCK, RenderLayer.getCutout());
        BlockEntityRendererFactories.register(PotionBeacons.POTION_BEACON_ENTITY, PotionBeaconBlockEntityRenderer::new);
    }
}
