package se.mickelus.tetra.blocks.scroll;

import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import se.mickelus.tetra.TetraMod;

import java.util.HashMap;
import java.util.Map;

public class ScrollDrops {
    Map<ResourceLocation, ResourceLocation> basicExtensions;

    public ScrollDrops() {
        basicExtensions = new HashMap<>();
        basicExtensions.put(BuiltInLootTables.BASTION_BRIDGE, new ResourceLocation(TetraMod.MOD_ID, "bastion_scrolls"));
        basicExtensions.put(BuiltInLootTables.BASTION_HOGLIN_STABLE, new ResourceLocation(TetraMod.MOD_ID, "bastion_scrolls"));
        basicExtensions.put(BuiltInLootTables.BASTION_OTHER, new ResourceLocation(TetraMod.MOD_ID, "bastion_scrolls"));
        basicExtensions.put(BuiltInLootTables.BASTION_TREASURE, new ResourceLocation(TetraMod.MOD_ID, "bastion_scrolls"));
        basicExtensions.put(BuiltInLootTables.NETHER_BRIDGE, new ResourceLocation(TetraMod.MOD_ID, "chests/nether_bridge_extended"));
        basicExtensions.put(BuiltInLootTables.SIMPLE_DUNGEON, new ResourceLocation(TetraMod.MOD_ID, "chests/simple_dungeon_extended"));
    }

    @SubscribeEvent
    public void onLootTableLoad(final LootTableLoadEvent event) {
        if (basicExtensions.containsKey(event.getName())) {
            event.getTable().addPool(LootPool.lootPool()
                    .name(TetraMod.MOD_ID + ":" + event.getName().getPath() + "_extended")
                    .add(LootTableReference.lootTableReference(basicExtensions.get(event.getName()))).build());
        }
    }
}
