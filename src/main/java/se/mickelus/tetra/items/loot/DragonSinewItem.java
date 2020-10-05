package se.mickelus.tetra.items.loot;

import net.minecraft.loot.LootPool;
import net.minecraft.loot.TableLootEntry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.TetraItem;
import se.mickelus.tetra.items.TetraItemGroup;

public class DragonSinewItem extends TetraItem {
    private static final String unlocalizedName = "dragon_sinew";
    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static DragonSinewItem instance;

    private static final ResourceLocation dragonLootTable = new ResourceLocation("entities/ender_dragon");
    private static final ResourceLocation sinewLootTable = new ResourceLocation(TetraMod.MOD_ID, "entities/ender_dragon_extended");

    public DragonSinewItem() {
        super(new Properties().group(TetraItemGroup.instance));
        setRegistryName(unlocalizedName);

        MinecraftForge.EVENT_BUS.register(new LootTableHandler());
    }

    public static class LootTableHandler {
        @SubscribeEvent
        public void onLootTableLoad(final LootTableLoadEvent event) {
            if (event.getName().equals(dragonLootTable)) {
                event.getTable().addPool(LootPool.builder()
                        .name(TetraMod.MOD_ID + ":" + unlocalizedName)
                        .addEntry(TableLootEntry.builder(sinewLootTable)).build());
            }
        }
    }
}
