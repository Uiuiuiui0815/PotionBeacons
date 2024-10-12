package net.uiuiuiui0815.potionbeacons;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class PotionBeacons implements ModInitializer {
	public static final String MOD_ID = "potionbeacons";

	public static <T extends BlockEntityType<?>> T register(String path, T blockEntityType) {
		return Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(MOD_ID, path), blockEntityType);
	}
	public static final BlockEntityType<PotionBeaconEntity> POTION_BEACON_ENTITY = register("potion_beacon",
	BlockEntityType.Builder.create(PotionBeaconEntity::new, PotionBeaconBlock.POTION_BEACON_BLOCK).build());
	@Override
	public void onInitialize() {
		Registry.register(Registries.BLOCK, Identifier.of(MOD_ID, "potion_beacon"), PotionBeaconBlock.POTION_BEACON_BLOCK);
		Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "potion_beacon"), new BlockItem(PotionBeaconBlock.POTION_BEACON_BLOCK, new Item.Settings()));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(content -> content.addAfter(Items.BEACON, PotionBeaconBlock.POTION_BEACON_BLOCK));
	}
}