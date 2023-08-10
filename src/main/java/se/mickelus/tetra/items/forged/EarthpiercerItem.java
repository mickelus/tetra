package se.mickelus.tetra.items.forged;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
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

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class EarthpiercerItem extends TetraItem {
    public static final String identifier = "earthpiercer";
    @ObjectHolder(registryName = "item", value = TetraMod.MOD_ID + ":" + identifier)
    public static EarthpiercerItem instance;

    public EarthpiercerItem() {
        super(new Properties());
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(ForgedBlockCommon.unsettlingTooltip);
        tooltip.add(Component.literal(" "));

        if (Screen.hasShiftDown()) {
            tooltip.add(Tooltips.expanded);
            tooltip.add(Component.translatable("item.tetra.earthpiercer.description").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal(" "));
            tooltip.add(ForgedBlockCommon.locationTooltip);
        } else {
            tooltip.add(Tooltips.expand);
        }
    }
}
