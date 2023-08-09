package se.mickelus.tetra.trades;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import javax.annotation.Nullable;

public class TreasureMapForEmeralds implements VillagerTrades.ItemListing {
    private final int emeraldCost;
    private final TagKey<Structure> destination;
    private final String displayName;
    private final MapDecoration.Type destinationType;
    private final int maxUses;
    private final int villagerXp;

    public TreasureMapForEmeralds(int emeraldCost, TagKey<Structure> destination, String displayName, MapDecoration.Type destinationType, int maxUses, int villagerXp) {
        this.emeraldCost = emeraldCost;
        this.destination = destination;
        this.displayName = displayName;
        this.destinationType = destinationType;
        this.maxUses = maxUses;
        this.villagerXp = villagerXp;
    }

    @Nullable
    public MerchantOffer getOffer(Entity villagerEntity, RandomSource random) {
        if (villagerEntity.level() instanceof ServerLevel serverLevel) {
            BlockPos blockpos = serverLevel.findNearestMapStructure(this.destination, villagerEntity.blockPosition(), 100, true);
            if (blockpos != null) {
                ItemStack itemstack = MapItem.create(serverLevel, blockpos.getX(), blockpos.getZ(), (byte) 2, true, true);
                MapItem.renderBiomePreviewMap(serverLevel, itemstack);
                MapItemSavedData.addTargetDecoration(itemstack, blockpos, "+", this.destinationType);
                itemstack.setHoverName(Component.translatable(this.displayName));
                itemstack.getTag().putString("tetra.advancement_marker", destination.location().toString());

                return new MerchantOffer(new ItemStack(Items.EMERALD, this.emeraldCost), itemstack, this.maxUses, this.villagerXp, 0.2F);
            }
        }
        return null;
    }
}
