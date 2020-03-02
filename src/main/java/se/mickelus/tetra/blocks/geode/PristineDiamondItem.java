package se.mickelus.tetra.blocks.geode;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.Tooltips;
import se.mickelus.tetra.blocks.forged.ForgedBlockCommon;
import se.mickelus.tetra.items.TetraItem;
import se.mickelus.tetra.items.TetraItemGroup;

import javax.annotation.Nullable;
import java.util.List;

public class PristineDiamondItem extends TetraItem {
    private static final String unlocalizedName = "pristine_diamond";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static PristineDiamondItem instance;

    public PristineDiamondItem() {
        super(new Properties().group(TetraItemGroup.instance));
        setRegistryName(unlocalizedName);
    }

    @Override
    public void addInformation(ItemStack itemStack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag advanced) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Tooltips.expanded);
            tooltip.add(new TranslationTextComponent("item.tetra.pristine_gem.description").setStyle(new Style()
                    .setColor(TextFormatting.GRAY)));
        } else {
            tooltip.add(Tooltips.expand);
        }
    }
}
