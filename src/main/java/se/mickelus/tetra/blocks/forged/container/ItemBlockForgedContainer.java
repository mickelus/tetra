package se.mickelus.tetra.blocks.forged.container;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Custom itemblock for the forged container, so that the correct bounding box can be checked on placement.
 */
public class ItemBlockForgedContainer extends ItemBlock {

    public ItemBlockForgedContainer(Block block) {
        super(block);
    }

    /**
     * Straight up copy of the vanilla implementation except that it does not check entity collisions in worldIn.mayPlace.
     */
    @Override
    public EnumActionResult onItemUse(PlayerEntity player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        IBlockState iblockstate = worldIn.getBlockState(pos);
        Block block = iblockstate.getBlock();

        if (!block.isReplaceable(worldIn, pos)) {
            pos = pos.offset(facing);
        }

        ItemStack itemstack = player.getHeldItem(hand);

        if (!itemstack.isEmpty() && player.canPlayerEdit(pos, facing, itemstack) && worldIn.mayPlace(this.block, pos, true, facing, (Entity)null)) {
            int i = this.getMetadata(itemstack.getMetadata());
            IBlockState iblockstate1 = this.block.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, i, player, hand);

            if (placeBlockAt(itemstack, player, worldIn, pos, facing, hitX, hitY, hitZ, iblockstate1)) {
                iblockstate1 = worldIn.getBlockState(pos);
                SoundType soundtype = iblockstate1.getBlock().getSoundType(iblockstate1, worldIn, pos, player);
                worldIn.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                itemstack.shrink(1);
            }

            return EnumActionResult.SUCCESS;
        } else {
            return EnumActionResult.FAIL;
        }
    }

    /**
     * Based on the vanilla implementation, but also checks that the multiblock part can be placed.
     */
    @Override
    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side, PlayerEntity player, ItemStack stack) {
        if (worldIn.getBlockState(pos.offset(side)).getBlock().isReplaceable(worldIn, pos.offset(side))) {
            BlockPos adjacentPos = pos.offset(side).offset(player.getHorizontalFacing().rotateY());
            return worldIn.getBlockState(adjacentPos).getBlock().isReplaceable(worldIn, adjacentPos);
        }

        return false;
    }
}
