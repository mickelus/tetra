package se.mickelus.tetra.blocks.geode;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.items.TetraCreativeTabs;
import se.mickelus.tetra.items.TetraItem;
import se.mickelus.tetra.network.PacketHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ItemGeode extends TetraItem {

    public static ItemGeode instance;
    private final String unlocalizedName = "geode";

    private GeodeContent[] geodeContents = new GeodeContent[0];
    private int combinedWeight = 0;

    public ItemGeode() {
        setRegistryName(unlocalizedName);
        setUnlocalizedName(unlocalizedName);
        setCreativeTab(TetraCreativeTabs.getInstance());

        instance = this;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(I18n.format("item.geode.tooltip"));
    }

    public ItemStack getRandomContent() {
        long offset = Math.round(Math.random() * combinedWeight);
        for (int i = 0; i < geodeContents.length; i++) {
            offset -= geodeContents[i].weight;
            if (offset <= 0) {
                return new ItemStack(
                        geodeContents[i].item,
                        1 + (int) (Math.random() * geodeContents[i].maxCount));
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void init(PacketHandler packetHandler) {
        initContent();
    }

    public void initContent() {
        String[] geodeConfig = ConfigHandler.geode_contents;
        ArrayList<GeodeContent> geodeContentList = new ArrayList<>(geodeConfig.length/3);
        combinedWeight = 0;

        if (geodeConfig.length % 3 != 0) {
            System.err.format("Unexpected length of geode content list: %d, bailing out... ", geodeConfig.length);
        }

        for (int i = 0; i < geodeConfig.length / 3; i++) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(geodeConfig[i*3]));
            if (item == null) {
                System.out.format("Could not find item: %s from geode content config, skipping...", geodeConfig[i*3]);
            } else {
                int weight = Integer.parseInt(geodeConfig[i*3+2]);
                geodeContentList.add(new GeodeContent(item, Integer.parseInt(geodeConfig[i*3+1]), weight));
                combinedWeight += weight;
            }
        }

        geodeContents = geodeContentList.toArray(new GeodeContent[geodeContentList.size()]);
    }
}
