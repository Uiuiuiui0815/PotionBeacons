package net.uiuiuiui0815.potionbeacons;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class PotionBeacons implements ModInitializer {
	public static final String MOD_ID = "potionbeacons";

	public static final BlockEntityType<PotionBeaconEntity> POTION_BEACON_ENTITY = Registry.register(
			Registries.BLOCK_ENTITY_TYPE,
			new Identifier(MOD_ID, "potion_beacon_entity"),
			FabricBlockEntityTypeBuilder.create(PotionBeaconEntity::new, PotionBeaconBlock.POTION_BEACON_BLOCK).build()
	);
	@Override
	public void onInitialize() {
		Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "potion_beacon"), PotionBeaconBlock.POTION_BEACON_BLOCK);
		Registry.register(Registries.ITEM, new Identifier(MOD_ID, "potion_beacon"), new BlockItem(PotionBeaconBlock.POTION_BEACON_BLOCK, new FabricItemSettings()));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(content -> content.addAfter(Items.BEACON, PotionBeaconBlock.POTION_BEACON_BLOCK));
	}
}