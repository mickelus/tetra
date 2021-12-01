package se.mickelus.tetra.trades;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;
import se.mickelus.tetra.items.forged.ItemMetalScrap;

import javax.annotation.Nullable;
import java.util.Random;

class ItemsForEmeraldsAndScrapTrade implements VillagerTrades.ItemListing {
    private final ItemStack sellingItem;
    private final int sellingItemCount;
    private final int emeraldCount;
    private final int scrapCount;
    private final int maxUses;
    private final int xpValue;
    private final float priceMultiplier;

    public ItemsForEmeraldsAndScrapTrade(ItemLike sellingItem, int sellingItemCount, int emeraldCount, int scrapCount, int maxUses) {
        this.sellingItem = new ItemStack(sellingItem);
        this.sellingItemCount = sellingItemCount;
        this.emeraldCount = emeraldCount;
        this.scrapCount = scrapCount;
        this.maxUses = maxUses;
        this.xpValue = 0;
        this.priceMultiplier = 0.05F;
    }

    @Nullable
    public MerchantOffer getOffer(Entity trader, Random rand) {
        return new MerchantOffer(new ItemStack(Items.EMERALD, this.emeraldCount), new ItemStack(ItemMetalScrap.instance, this.scrapCount),
                new ItemStack(this.sellingItem.getItem(), this.sellingItemCount), this.maxUses, this.xpValue, this.priceMultiplier);
    }
}
