package se.mickelus.tetra.items.forged;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.RegistryObject;
import se.mickelus.tetra.blocks.forged.ForgedBlockCommon;
import se.mickelus.tetra.items.TetraItem;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class MetalScrapItem extends TetraItem {
    public static final String identifier = "metal_scrap";
    public static RegistryObject<MetalScrapItem> instance;

    public MetalScrapItem() {
        super(new Properties());
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.tetra.metal_scrap.description").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal(" "));
        tooltip.add(ForgedBlockCommon.locationTooltip);
    }
}
