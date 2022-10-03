package se.mickelus.tetra.blocks.geode;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.TetraItem;
import se.mickelus.tetra.TetraItemGroup;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class GeodeItem extends TetraItem {
    public static final String identifier = "geode";

    @ObjectHolder(TetraMod.MOD_ID + ":" + identifier)
    public static GeodeItem instance;

    public GeodeItem() {
        super(new Properties().tab(TetraItemGroup.instance));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(new TranslatableComponent("item.tetra.geode.tooltip").withStyle(ChatFormatting.GRAY));
    }
}
