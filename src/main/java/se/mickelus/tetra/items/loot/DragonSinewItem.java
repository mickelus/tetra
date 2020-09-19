package se.mickelus.tetra.items.loot;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.TableLootEntry;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.TetraItem;
import se.mickelus.tetra.items.TetraItemGroup;

import javax.annotation.Nullable;
import java.util.List;

public class DragonSinewItem extends TetraItem {
    private static final String unlocalizedName = "dragon_sinew";
    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static DragonSinewItem instance;

    public static final ITextComponent tooltip = new TranslationTextComponent("item.tetra.dragon_sinew.tooltip")
            .mergeStyle(TextFormatting.GRAY);

    private static final ResourceLocation dragonLootTable = new ResourceLocation("entities/ender_dragon");
    private static final ResourceLocation sinewLootTable = new ResourceLocation(TetraMod.MOD_ID, "entities/ender_dragon_extended");

    public DragonSinewItem() {
        super(new Properties().group(TetraItemGroup.instance));
        setRegistryName(unlocalizedName);

        MinecraftForge.EVENT_BUS.register(new LootTableHandler());
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(DragonSinewItem.tooltip);
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
