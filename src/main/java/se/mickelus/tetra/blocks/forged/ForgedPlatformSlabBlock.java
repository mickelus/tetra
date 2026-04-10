package se.mickelus.tetra.blocks.forged;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.SlabBlock;
import se.mickelus.tetra.blocks.InitializableBlock;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class ForgedPlatformSlabBlock extends SlabBlock implements InitializableBlock {
    public static final String identifier = "forged_platform_slab";

    public ForgedPlatformSlabBlock() {
        super(ForgedBlockCommon.propertiesSolid);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag advanced) {
        tooltip.add(ForgedBlockCommon.locationTooltip);
    }
}
