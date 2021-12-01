package se.mickelus.tetra.items.forged;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.Tooltips;
import se.mickelus.tetra.blocks.forged.ForgedBlockCommon;
import se.mickelus.tetra.items.TetraItem;
import se.mickelus.tetra.items.TetraItemGroup;

import javax.annotation.Nullable;
import java.util.List;

public class EarthpiercerItem extends TetraItem {
    private static final String unlocalizedName = "earthpiercer";
    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static EarthpiercerItem instance;

    public EarthpiercerItem() {
        super(new Properties().tab(TetraItemGroup.instance));
        setRegistryName(unlocalizedName);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(ForgedBlockCommon.unsettlingTooltip);
        tooltip.add(new TextComponent(" "));

        if (Screen.hasShiftDown()) {
            tooltip.add(Tooltips.expanded);
            tooltip.add(new TranslatableComponent("item.tetra.earthpiercer.description").withStyle(ChatFormatting.GRAY));
            tooltip.add(new TextComponent(" "));
            tooltip.add(ForgedBlockCommon.locationTooltip);
        } else {
            tooltip.add(Tooltips.expand);
        }
    }
}
