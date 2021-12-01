package se.mickelus.tetra.blocks.geode;

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.util.text.Style;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.TetraItem;
import se.mickelus.tetra.items.TetraItemGroup;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.world.item.Item.Properties;

public class GeodeItem extends TetraItem {
    private static final String unlocalizedName = "geode";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static GeodeItem instance;

    public GeodeItem() {
        super(new Properties().tab(TetraItemGroup.instance));
        setRegistryName(unlocalizedName);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(new TranslatableComponent("item.tetra.geode.tooltip").withStyle(ChatFormatting.GRAY));
    }
}
