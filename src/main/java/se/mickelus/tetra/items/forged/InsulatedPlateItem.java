package se.mickelus.tetra.items.forged;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.forged.ForgedBlockCommon;
import se.mickelus.tetra.items.TetraItem;
import se.mickelus.tetra.items.TetraItemGroup;

import javax.annotation.Nullable;
import java.util.List;

public class InsulatedPlateItem extends TetraItem {
    private static final String unlocalizedName = "vent_plate";
    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static InsulatedPlateItem instance;

    public InsulatedPlateItem() {
        super(new Properties().group(TetraItemGroup.instance));
        setRegistryName(unlocalizedName);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(new TranslationTextComponent("item.tetra.vent_plate.description").mergeStyle(TextFormatting.GRAY));
        tooltip.add(new StringTextComponent(" "));
        tooltip.add(ForgedBlockCommon.locationTooltip);
    }
}
