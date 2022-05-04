package se.mickelus.tetra.blocks.forged;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RotatedPillarBlock;
import se.mickelus.tetra.blocks.InitializableBlock;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class BlockForgedPillar extends RotatedPillarBlock implements InitializableBlock {
    private static final String identifier = "forged_pillar";

    public BlockForgedPillar() {
        super(ForgedBlockCommon.propertiesSolid);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag advanced) {
        tooltip.add(ForgedBlockCommon.locationTooltip);
    }
}
