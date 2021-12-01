package se.mickelus.tetra.trades;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.block.Block;

import java.util.Random;

class ItemsForScrapTrade implements VillagerTrades.ItemListing {
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
