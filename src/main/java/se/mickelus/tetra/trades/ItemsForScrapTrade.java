package se.mickelus.tetra.trades;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.villager.VillagerTrades;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MerchantOffer;

import java.util.Random;

class ItemsForScrapTrade implements VillagerTrades.ITrade {
    private final ItemStack sellingItem;
    private final int scrapCount;
    private final int sellingItemCount;
    private final int maxUses;
    private final int xpValue;
    private final float priceMultiplier;

    public ItemsForScrapTrade(Block sellingItem, int sellingItemCount, int scrapCount, int maxUses) {
        this(new ItemStack(sellingItem), sellingItemCount, scrapCount, maxUses);
    }

    public ItemsForScrapTrade(Item sellingItem, int sellingItemCount, int scrapCount) {
        this(new ItemStack(sellingItem), sellingItemCount, scrapCount, 12);
    }

    public ItemsForScrapTrade(Item sellingItem, int sellingItemCount, int scrapCount, int maxUses) {
        this(new ItemStack(sellingItem), sellingItemCount, scrapCount, maxUses);
    }

    public ItemsForScrapTrade(ItemStack sellingItem, int sellingItemCount, int scrapCount, int maxUses) {
        this.sellingItem = sellingItem;
        this.scrapCount = scrapCount;
        this.sellingItemCount = sellingItemCount;
        this.maxUses = maxUses;
        this.xpValue = 0;
        this.priceMultiplier = 0.05F;
    }

    public MerchantOffer getOffer(Entity trader, Random rand) {
        return new MerchantOffer(new ItemStack(Items.EMERALD, this.scrapCount), new ItemStack(this.sellingItem.getItem(), this.sellingItemCount), this.maxUses, this.xpValue, this.priceMultiplier);
    }
}
