package se.mickelus.tetra.blocks.multischematic;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

public class StackedMultiblockSchematicItem extends BaseMultiblockSchematicItem {

    Block ruinedBlock;

    public StackedMultiblockSchematicItem(MultiblockSchematicBlock block, Block ruinedBlock) {
        super(block, block);
        this.ruinedBlock = ruinedBlock;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.addAll(getTooltip());
    }

    @Nullable
    @Override
    protected BlockState getPlacementState(BlockPlaceContext context) {
        if (context.getPlayer() != null && context.getPlayer().isCreative() && context.getPlayer().isCrouching()) {
            BlockState ruinedState = ruinedBlock.getStateForPlacement(context);
            return ruinedState != null && this.canPlace(context, ruinedState) ? ruinedState : null;
        }

        return super.getPlacementState(context);
    }
}
