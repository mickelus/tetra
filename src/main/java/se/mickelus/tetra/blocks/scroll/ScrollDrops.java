package se.mickelus.tetra.blocks.scroll;

import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.TableLootEntry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import se.mickelus.tetra.TetraMod;

import java.util.HashMap;
import java.util.Map;

public class ScrollDrops {
    Map<ResourceLocation, ResourceLocation> basicExtensions;

    public ScrollDrops() {
        basicExtensions = new HashMap<>();
        basicExtensions.put(LootTables.BASTION_BRIDGE, new ResourceLocation(TetraMod.MOD_ID, "bastion_scrolls"));
        basicExtensions.put(LootTables.BASTION_HOGLIN_STABLE, new ResourceLocation(TetraMod.MOD_ID, "bastion_scrolls"));
        basicExtensions.put(LootTables.BASTION_OTHER, new ResourceLocation(TetraMod.MOD_ID, "bastion_scrolls"));
        basicExtensions.put(LootTables.BASTION_TREASURE, new ResourceLocation(TetraMod.MOD_ID, "bastion_scrolls"));
        basicExtensions.put(LootTables.CHESTS_NETHER_BRIDGE, new ResourceLocation(TetraMod.MOD_ID, "chests/nether_bridge_extended"));
        basicExtensions.put(LootTables.CHESTS_SIMPLE_DUNGEON, new ResourceLocation(TetraMod.MOD_ID, "chests/simple_dungeon_extended"));
    }

    @SubscribeEvent
    public void onLootTableLoad(final LootTableLoadEvent event) {
        if (basicExtensions.containsKey(event.getName())) {
            event.getTable().addPool(LootPool.builder()
                    .name(TetraMod.MOD_ID + ":" + event.getName().getPath() + "_extended")
                    .addEntry(TableLootEntry.builder(basicExtensions.get(event.getName()))).build());
        }
    }
}
