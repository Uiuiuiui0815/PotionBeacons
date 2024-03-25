package net.uiuiuiui0815.potionbeacons;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;

public class PotionBeaconsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(PotionBeaconBlock.POTION_BEACON_BLOCK, RenderLayer.getCutout());
    }
}
