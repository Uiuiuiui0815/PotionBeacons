package net.uiuiuiui0815.potionbeacons;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PotionBeacons implements ModInitializer {
	public static final String MOD_ID = "potionbeacons";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		Registry.register(Registries.BLOCK, new Identifier("potionbeacons", "potion_beacon"), PotionBeaconBlock.POTION_BEACON_BLOCK);
		Registry.register(Registries.ITEM, new Identifier("potionbeacons", "potion_beacon"), new BlockItem(PotionBeaconBlock.POTION_BEACON_BLOCK, new FabricItemSettings()));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(content -> {content.addAfter(Items.BEACON, PotionBeaconBlock.POTION_BEACON_BLOCK);});
	}
}