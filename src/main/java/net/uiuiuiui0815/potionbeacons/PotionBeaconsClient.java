package net.uiuiuiui0815.potionbeacons;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.impl.client.rendering.BlockEntityRendererRegistryImpl;
import net.minecraft.client.render.RenderLayer;

@Environment(EnvType.CLIENT)
public class PotionBeaconsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(PotionBeaconBlock.POTION_BEACON_BLOCK, RenderLayer.getCutout());
        BlockEntityRendererRegistryImpl.register(PotionBeacons.POTION_BEACON_ENTITY, PotionBeaconBlockEntityRenderer::new);
    }
}
