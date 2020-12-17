package se.mickelus.tetra.trades;

import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.villager.VillagerTrades;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MerchantOffer;
import net.minecraft.util.IItemProvider;
import se.mickelus.tetra.items.forged.ItemMetalScrap;

import javax.annotation.Nullable;
import java.util.Random;

class ItemsForEmeraldsAndScrapTrade implements VillagerTrades.ITrade {
    private final ItemStack sellingItem;
    private final int sellingItemCount;
    private final int emeraldCount;
    private final int scrapCount;
    private final int maxUses;
    private final int xpValue;
    private final float priceMultiplier;

    public ItemsForEmeraldsAndScrapTrade(IItemProvider sellingItem, int sellingItemCount, int emeraldCount, int scrapCount, int maxUses) {
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
