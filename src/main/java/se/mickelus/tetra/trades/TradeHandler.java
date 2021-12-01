package se.mickelus.tetra.trades;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import se.mickelus.tetra.blocks.forged.chthonic.ChthonicExtractorBlock;
import se.mickelus.tetra.blocks.scroll.ScrollItem;
import se.mickelus.tetra.items.forged.*;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
@ParametersAreNonnullByDefault
public class TradeHandler {
    @SubscribeEvent
    public void setupWandererTrades(WandererTradesEvent event) {
        List<VillagerTrades.ItemListing> generic = event.getGenericTrades();
        List<VillagerTrades.ItemListing> rare = event.getRareTrades();

        generic.add(new ItemsForScrapTrade(InsulatedPlateItem.instance, 1, 24, 1));
        generic.add(new ItemsForEmeraldsAndScrapTrade(LubricantDispenser.instance, 1, 8, 16, 1));
        generic.add(new ItemsForEmeraldsAndScrapTrade(ItemQuickLatch.instance, 1, 5, 16, 1));
        generic.add(new ItemsForScrapTrade(ItemBolt.instance, 1, 32, 2));

        rare.add(new ItemsForEmeraldsAndScrapTrade(StonecutterItem.instance, 1, 32, 16, 1));
        rare.add(new ItemsForEmeraldsAndScrapTrade(EarthpiercerItem.instance, 1, 24, 16, 1));
        rare.add(new ItemsForEmeraldsAndScrapTrade(CombustionChamberItem.instance, 1, 25, 16, 1));
        rare.add(new ItemsForEmeraldsAndScrapTrade(ChthonicExtractorBlock.instance, 1, 8, 16, 5));
    }

    @SubscribeEvent
    public void setupVillagerTrades(VillagerTradesEvent event) {
        VillagerProfession profession = event.getType();

        if (VillagerProfession.TOOLSMITH.equals(profession)) {
            event.getTrades().get(3).addAll(ImmutableList.of(
                    new ItemsForEmeraldsTrade(ScrollItem.hammerEfficiency, 4, 1, 1, 10)
            ));
            event.getTrades().get(4).addAll(ImmutableList.of(
                    new ItemsForEmeraldsTrade(ScrollItem.metalExpertise, 8, 1, 1, 20),
                    new ItemsForEmeraldsTrade(ScrollItem.hammerEfficiency, 4, 1, 1, 15)
            ));
            event.getTrades().get(5).addAll(ImmutableList.of(
                    new ItemsForEmeraldsTrade(ScrollItem.metalExpertise, 8, 1, 1, 20)
            ));
        }

        if (VillagerProfession.WEAPONSMITH.equals(profession)) {
            event.getTrades().get(3).addAll(ImmutableList.of(
                    new ItemsForEmeraldsTrade(ScrollItem.hammerEfficiency, 4, 1, 1, 10)
            ));
            event.getTrades().get(4).addAll(ImmutableList.of(
                    new ItemsForEmeraldsTrade(ScrollItem.metalExpertise, 8, 1, 1, 20),
                    new ItemsForEmeraldsTrade(ScrollItem.hammerEfficiency, 4, 1, 1, 15)
            ));
            event.getTrades().get(5).addAll(ImmutableList.of(
                    new ItemsForEmeraldsTrade(ScrollItem.metalExpertise, 8, 1, 1, 20)
            ));
        }

        if (VillagerProfession.ARMORER.equals(profession)) {
            event.getTrades().get(3).addAll(ImmutableList.of(
                    new ItemsForEmeraldsTrade(ScrollItem.hammerEfficiency, 4, 1, 1, 10)
            ));
            event.getTrades().get(4).addAll(ImmutableList.of(
                    new ItemsForEmeraldsTrade(ScrollItem.metalExpertise, 8, 1, 1, 20),
                    new ItemsForEmeraldsTrade(ScrollItem.hammerEfficiency, 4, 1, 1, 15)
            ));
            event.getTrades().get(5).addAll(ImmutableList.of(
                    new ItemsForEmeraldsTrade(ScrollItem.metalExpertise, 8, 1, 1, 20)
            ));
        }

        if (VillagerProfession.FLETCHER.equals(profession)) {
            event.getTrades().get(2).addAll(ImmutableList.of(
                    new ItemsForEmeraldsTrade(ScrollItem.axeEfficiency, 4, 1, 1, 5)
            ));
            event.getTrades().get(3).addAll(ImmutableList.of(
                    new ItemsForEmeraldsTrade(ScrollItem.woodExpertise, 8, 1, 1, 15),
                    new ItemsForEmeraldsTrade(ScrollItem.axeEfficiency, 4, 1, 1, 10)
            ));
            event.getTrades().get(4).addAll(ImmutableList.of(
                    new ItemsForEmeraldsTrade(ScrollItem.woodExpertise, 8, 1, 1, 20),
                    new ItemsForEmeraldsTrade(ScrollItem.fibreExpertise, 8, 1, 1, 20)
            ));
            event.getTrades().get(5).addAll(ImmutableList.of(
                    new ItemsForEmeraldsTrade(ScrollItem.fibreExpertise, 8, 1, 1, 20)
            ));
        }

        if (VillagerProfession.LEATHERWORKER.equals(profession)) {
            event.getTrades().get(2).addAll(ImmutableList.of(
                    new ItemsForEmeraldsTrade(ScrollItem.cutEfficiency, 4, 1, 1, 5)
            ));
            event.getTrades().get(3).addAll(ImmutableList.of(
                    new ItemsForEmeraldsTrade(ScrollItem.scaleExpertise, 8, 1, 1, 15),
                    new ItemsForEmeraldsTrade(ScrollItem.cutEfficiency, 4, 1, 1, 10)
            ));
            event.getTrades().get(4).addAll(ImmutableList.of(
                    new ItemsForEmeraldsTrade(ScrollItem.scaleExpertise, 8, 1, 1, 20),
                    new ItemsForEmeraldsTrade(ScrollItem.skinExpertise, 8, 1, 1, 20)
            ));
            event.getTrades().get(5).addAll(ImmutableList.of(
                    new ItemsForEmeraldsTrade(ScrollItem.skinExpertise, 8, 1, 1, 20)
            ));
        }

        if (VillagerProfession.MASON.equals(profession)) {
            event.getTrades().get(2).addAll(ImmutableList.of(
                    new ItemsForEmeraldsTrade(ScrollItem.hammerEfficiency, 4, 1, 1, 5)
            ));
            event.getTrades().get(3).addAll(ImmutableList.of(
                    new ItemsForEmeraldsTrade(ScrollItem.stoneExpertise, 8, 1, 1, 15),
                    new ItemsForEmeraldsTrade(ScrollItem.hammerEfficiency, 4, 1, 1, 10)
            ));
            event.getTrades().get(4).addAll(ImmutableList.of(
                    new ItemsForEmeraldsTrade(ScrollItem.stoneExpertise, 8, 1, 1, 20),
                    new ItemsForEmeraldsTrade(ScrollItem.gemExpertise, 8, 1, 1, 20)
            ));
            event.getTrades().get(5).addAll(ImmutableList.of(
                    new ItemsForEmeraldsTrade(ScrollItem.gemExpertise, 8, 1, 1, 20)
            ));
        }

        if (VillagerProfession.LIBRARIAN.equals(profession)) {
            event.getTrades().get(3).addAll(ImmutableList.of(
                    new ItemsForEmeraldsTrade(ScrollItem.hammerEfficiency, 8, 1, 1, 20),
                    new ItemsForEmeraldsTrade(ScrollItem.axeEfficiency, 8, 1, 1, 20),
                    new ItemsForEmeraldsTrade(ScrollItem.cutEfficiency, 8, 1, 1, 20)
            ));
            event.getTrades().get(4).addAll(ImmutableList.of(
                    new ItemsForEmeraldsTrade(ScrollItem.sturdyGuard, 16, 1, 1, 20),
                    new ItemsForEmeraldsTrade(ScrollItem.throwingKnife, 16, 1, 1, 20),
                    new ItemsForEmeraldsTrade(ScrollItem.howlingBlade, 16, 1, 1, 20)
            ));
        }

        if (VillagerProfession.SHEPHERD.equals(profession)) {
            event.getTrades().get(3).addAll(ImmutableList.of(
                    new ItemsForEmeraldsTrade(ScrollItem.fabricExpertise, 16, 1, 1, 15)
            ));
            event.getTrades().get(4).addAll(ImmutableList.of(
                    new ItemsForEmeraldsTrade(ScrollItem.fabricExpertise, 16, 1, 1, 20)
            ));
        }

        if (VillagerProfession.BUTCHER.equals(profession)) {
            event.getTrades().get(3).addAll(ImmutableList.of(
                    new ItemsForEmeraldsTrade(ScrollItem.boneExpertise, 16, 1, 1, 15)
            ));
            event.getTrades().get(4).addAll(ImmutableList.of(
                    new ItemsForEmeraldsTrade(ScrollItem.boneExpertise, 16, 1, 1, 20)
            ));
        }
    }
}
