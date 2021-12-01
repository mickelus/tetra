package se.mickelus.tetra.blocks.geode;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.Tooltips;
import se.mickelus.tetra.items.TetraItem;
import se.mickelus.tetra.items.TetraItemGroup;

import javax.annotation.Nullable;
import java.util.List;

public class PristineEmeraldItem extends TetraItem {
    private static final String unlocalizedName = "pristine_emerald";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static PristineEmeraldItem instance;

    public PristineEmeraldItem() {
        super(new Properties().tab(TetraItemGroup.instance));
        setRegistryName(unlocalizedName);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level world, List<Component> tooltip, TooltipFlag advanced) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Tooltips.expanded);
            tooltip.add(new TranslatableComponent("item.tetra.pristine_gem.description").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Tooltips.expand);
        }
    }
}
