package se.mickelus.tetra.trades;

import net.minecraft.entity.merchant.villager.VillagerTrades;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import se.mickelus.tetra.blocks.forged.chthonic.ChthonicExtractorBlock;
import se.mickelus.tetra.items.forged.*;

import java.util.List;

public class TradeHandler {
    @SubscribeEvent
    public void setupTrades(WandererTradesEvent event) {
        List<VillagerTrades.ITrade> generic = event.getGenericTrades();
        List<VillagerTrades.ITrade> rare = event.getRareTrades();

        generic.add(new ItemsForScrapTrade(InsulatedPlateItem.instance, 1, 24, 1));
        generic.add(new ItemsForEmeraldsAndScrapTrade(LubricantDispenser.instance, 1, 8, 16, 1));
        generic.add(new ItemsForEmeraldsAndScrapTrade(ItemQuickLatch.instance, 1, 5, 16, 1));
        generic.add(new ItemsForScrapTrade(ItemBolt.instance, 1, 32, 2));

        rare.add(new ItemsForEmeraldsAndScrapTrade(StonecutterItem.instance, 1, 32, 16, 1));
        rare.add(new ItemsForEmeraldsAndScrapTrade(EarthpiercerItem.instance, 1, 24, 16, 1));
        rare.add(new ItemsForEmeraldsAndScrapTrade(CombustionChamberItem.instance, 1, 25, 16, 1));
        rare.add(new ItemsForEmeraldsAndScrapTrade(ChthonicExtractorBlock.instance, 1, 8, 16, 5));

    }
}
