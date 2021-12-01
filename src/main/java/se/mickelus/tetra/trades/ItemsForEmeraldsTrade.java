package se.mickelus.tetra.trades;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.Random;

class ItemsForEmeraldsTrade implements VillagerTrades.ItemListing {
    private final ItemStack sellingItem;
    private final int emeraldCount;
    private final int sellingItemCount;
    private final int maxUses;
    private final int xpValue;
    private final float priceMultiplier;

    public ItemsForEmeraldsTrade(ItemStack sellingItem, int emeraldCount, int sellingItemCount, int maxUses, int xpValue) {
        this(sellingItem, emeraldCount, sellingItemCount, maxUses, xpValue, 0.05F);
    }

    public ItemsForEmeraldsTrade(ItemStack sellingItem, int emeraldCount, int sellingItemCount, int maxUses, int xpValue, float priceMultiplier) {
        this.sellingItem = sellingItem;
        this.emeraldCount = emeraldCount;
        this.sellingItemCount = sellingItemCount;
        this.maxUses = maxUses;
        this.xpValue = xpValue;
        this.priceMultiplier = priceMultiplier;
    }

    public MerchantOffer getOffer(Entity trader, Random rand) {
        ItemStack itemCopy = sellingItem.copy();
        itemCopy.setCount(sellingItemCount);
        return new MerchantOffer(new ItemStack(Items.EMERALD, this.emeraldCount), itemCopy, this.maxUses, this.xpValue, this.priceMultiplier);
    }
}
