package se.mickelus.tetra.blocks.geode;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.TetraItem;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class GeodeItem extends TetraItem {
    public static final String identifier = "geode";

    @ObjectHolder(registryName = "item", value = TetraMod.MOD_ID + ":" + identifier)
    public static GeodeItem instance;

    public GeodeItem() {
        super(new Properties());
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(Component.translatable("item.tetra.geode.tooltip").withStyle(ChatFormatting.GRAY));
    }
}
