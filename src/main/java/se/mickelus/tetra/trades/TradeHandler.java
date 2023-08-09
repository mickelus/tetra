package se.mickelus.tetra.trades;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import se.mickelus.tetra.blocks.forged.chthonic.ChthonicExtractorBlock;
import se.mickelus.tetra.blocks.scroll.ScrollItem;
import se.mickelus.tetra.items.forged.*;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.List;

@ParametersAreNonnullByDefault
public class TradeHandler {
    public static final TagKey<Structure> ruinsTag = TagKey.create(Registries.STRUCTURE, new ResourceLocation("tetra:forged_ruins"));

    private static void add(VillagerTradesEvent event, int level, VillagerTrades.ItemListing... listings) {
        event.getTrades().get(level).addAll(Arrays.asList(listings));
    }

    @SubscribeEvent
    public void setupWandererTrades(WandererTradesEvent event) {
        List<VillagerTrades.ItemListing> generic = event.getGenericTrades();
        List<VillagerTrades.ItemListing> rare = event.getRareTrades();

        generic.add(new ItemsForScrapTrade(InsulatedPlateItem.instance, 1, 24, 1));
        generic.add(new ItemsForEmeraldsAndScrapTrade(LubricantDispenserItem.instance.get(), 1, 8, 16, 1));
        generic.add(new ItemsForEmeraldsAndScrapTrade(QuickLatchItem.instance, 1, 5, 16, 1));
        generic.add(new ItemsForScrapTrade(BoltItem.instance, 1, 32, 2));

        rare.add(new ItemsForEmeraldsAndScrapTrade(StonecutterItem.instance, 1, 32, 16, 1));
        rare.add(new ItemsForEmeraldsAndScrapTrade(EarthpiercerItem.instance, 1, 24, 16, 1));
        rare.add(new ItemsForEmeraldsAndScrapTrade(CombustionChamberItem.instance.get(), 1, 25, 16, 1));
        rare.add(new ItemsForEmeraldsAndScrapTrade(ChthonicExtractorBlock.instance, 1, 8, 16, 5));
    }

    @SubscribeEvent
    public void setupVillagerTrades(VillagerTradesEvent event) {
        VillagerProfession profession = event.getType();

        if (VillagerProfession.TOOLSMITH.equals(profession)) { // 5, 2, 5, 3, 1
            add(event, 2, new ItemsForEmeraldsTrade(ScrollItem.hammerEfficiency, 4, 1, 1, 5));
            add(event, 5, new ItemsForEmeraldsTrade(ScrollItem.metalExpertise, 8, 1, 1, 20));
        }

        if (VillagerProfession.WEAPONSMITH.equals(profession)) { // 3, 2, 1, 2, 1
            add(event, 3, new ItemsForEmeraldsTrade(ScrollItem.hammerEfficiency, 4, 1, 1, 10));
            add(event, 5, new ItemsForEmeraldsTrade(ScrollItem.metalExpertise, 8, 1, 1, 20));
        }

        if (VillagerProfession.ARMORER.equals(profession)) { // 5, 4, 5, 2, 2
            add(event, 4, new ItemsForEmeraldsTrade(ScrollItem.hammerEfficiency, 4, 1, 1, 10));
            add(event, 5, new ItemsForEmeraldsTrade(ScrollItem.metalExpertise, 8, 1, 1, 20));
        }

        if (VillagerProfession.FLETCHER.equals(profession)) { // 3, 2, 2, 2, 3
            add(event, 2, new ItemsForEmeraldsTrade(ScrollItem.axeEfficiency, 4, 1, 1, 5));
            add(event, 3, new ItemsForEmeraldsTrade(ScrollItem.woodExpertise, 8, 1, 1, 15));
            add(event, 4, new ItemsForEmeraldsTrade(ScrollItem.fibreExpertise, 8, 1, 1, 20));
        }

        if (VillagerProfession.LEATHERWORKER.equals(profession)) { // 3, 3, 2, 2, 2
            add(event, 3, new ItemsForEmeraldsTrade(ScrollItem.cutEfficiency, 4, 1, 1, 10));
            add(event, 4, new ItemsForEmeraldsTrade(ScrollItem.scaleExpertise, 8, 1, 1, 20));
            add(event, 5, new ItemsForEmeraldsTrade(ScrollItem.skinExpertise, 8, 1, 1, 20));
        }

        if (VillagerProfession.MASON.equals(profession)) { // 2, 2, 7, 30~, 2
            add(event, 2, new ItemsForEmeraldsTrade(ScrollItem.stoneExpertise, 8, 1, 1, 5));
            add(event, 5, new ItemsForEmeraldsTrade(ScrollItem.gemExpertise, 8, 1, 1, 20));
        }

        if (VillagerProfession.SHEPHERD.equals(profession)) {
            add(event, 5, new ItemsForEmeraldsTrade(ScrollItem.fabricExpertise, 16, 1, 1, 20));
        }

        if (VillagerProfession.BUTCHER.equals(profession)) {
            add(event, 4, new ItemsForEmeraldsTrade(ScrollItem.boneExpertise, 16, 1, 1, 20));
        }

        if (VillagerProfession.CARTOGRAPHER.equals(profession)) {
            add(event, 2, new TreasureMapForEmeralds(16, ruinsTag, "tetra.filled_map.forged_ruins", MapDecoration.Type.RED_X, 1, 5));

//        1: 2
//        2: 4
//        3: 2
//        4: 17
//        5: 1
        }
    }
}
