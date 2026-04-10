package se.mickelus.tetra.items.forged;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.forged.ForgedBlockCommon;
import se.mickelus.tetra.items.TetraItem;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class PlanarStabilizerItem extends TetraItem {
    public static final String identifier = "planar_stabilizer";
    @ObjectHolder(registryName = "item", value = TetraMod.MOD_ID + ":" + identifier)
    public static PlanarStabilizerItem instance;

    public PlanarStabilizerItem() {
        super(new Properties());
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack itemStack, net.minecraft.world.item.Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.tetra.planar_stabilizer.description").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal(" "));
        tooltip.add(ForgedBlockCommon.locationTooltip);
    }
}
