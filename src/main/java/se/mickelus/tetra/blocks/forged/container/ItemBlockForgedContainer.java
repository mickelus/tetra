package se.mickelus.tetra.blocks.forged.container;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.world.World;
import se.mickelus.tetra.items.TetraItemGroup;

/**
 * Custom itemblock for the forged container, so that the correct bounding box can be checked on placement.
 */
public class ItemBlockForgedContainer extends BlockItem {

    public ItemBlockForgedContainer(Block block) {
        super(block, new Item.Properties().group(TetraItemGroup.getInstance()));

        setRegistryName(block.getRegistryName());
    }

    /**
     * Straight up copy of the vanilla implementation except that it does not check entity collisions in worldIn.mayPlace.
     */
    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        Hand hand = context.getHand();
        Direction face = context.getFace();

        World world = context.getWorld();
        BlockPos pos = context.getPos();
        BlockState blockState = context.getWorld().getBlockState(pos);
        Block block = blockState.getBlock();

        BlockItemUseContext extendedContext = new BlockItemUseContext(context);

        if (!blockState.isReplaceable(extendedContext)) {
            pos = pos.offset(context.getFace());
        }

        ItemStack itemstack = player.getHeldItem(hand);

        if (!itemstack.isEmpty()
                && player.canPlayerEdit(pos, face, itemstack)
                && world.func_217350_a(blockState, pos, ISelectionContext.forEntity(player))) {

            BlockState newState = block.getStateForPlacement(extendedContext);

            if (placeBlock(extendedContext, newState)) {
                newState = world.getBlockState(pos);
                SoundType soundtype = newState.getBlock().getSoundType(newState, world, pos, player);
                world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                itemstack.shrink(1);
            }

            return ActionResultType.SUCCESS;
        } else {
            return ActionResultType.FAIL;
        }
    }

    /**
     * Based on the vanilla implementation, but also checks that the multiblock part can be placed.
     */
    @Override
    protected boolean canPlace(BlockItemUseContext context, BlockState blockState) {
        Direction adjacentDirection = context.getPlayer().getHorizontalFacing().rotateY();
        BlockPos adjacentPos = context.getPos().offset(context.getFace()).offset(adjacentDirection);

        return super.canPlace(context, blockState)
                && super.canPlace(BlockItemUseContext.func_221536_a(context, adjacentPos, adjacentDirection), blockState);
    }
}
