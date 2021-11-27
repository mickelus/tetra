package se.mickelus.tetra.items.forged;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
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
        super(new Properties().group(TetraItemGroup.instance));
        setRegistryName(unlocalizedName);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(ForgedBlockCommon.unsettlingTooltip);
        tooltip.add(new StringTextComponent(" "));

        if (Screen.hasShiftDown()) {
            tooltip.add(Tooltips.expanded);
            tooltip.add(new TranslationTextComponent("item.tetra.earthpiercer.description").mergeStyle(TextFormatting.GRAY));
            tooltip.add(new StringTextComponent(" "));
            tooltip.add(ForgedBlockCommon.locationTooltip);
        } else {
            tooltip.add(Tooltips.expand);
        }
    }
}
