package se.mickelus.tetra.blocks.forged;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SlabBlock;
import se.mickelus.tetra.blocks.InitializableBlock;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class BlockForgedPlatformSlab extends SlabBlock implements InitializableBlock {
    static final String identifier = "forged_platform_slab";

    public BlockForgedPlatformSlab() {
        super(ForgedBlockCommon.propertiesSolid);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag advanced) {
        tooltip.add(ForgedBlockCommon.locationTooltip);
    }
}
