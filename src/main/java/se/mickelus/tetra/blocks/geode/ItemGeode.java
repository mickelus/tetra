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
}
