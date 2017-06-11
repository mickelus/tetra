package se.mickelus.tetra.items.hammer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import se.mickelus.tetra.blocks.workbench.BlockWorkbench;
import se.mickelus.tetra.items.TetraCreativeTabs;
import se.mickelus.tetra.items.TetraItem;
import se.mickelus.tetra.network.PacketPipeline;

public class HammerItem extends TetraItem {

    private static final String unlocalizedName = "hammer";

    public HammerItem() {
        setUnlocalizedName(unlocalizedName);
        setRegistryName(unlocalizedName);
        GameRegistry.register(this);
        setCreativeTab(TetraCreativeTabs.getInstance());
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack itemStack = player.getHeldItem(hand);
        if (!player.canPlayerEdit(pos.offset(facing), facing, itemStack)) {
            return EnumActionResult.FAIL;
        }

        if (world.getBlockState(pos).getBlock().equals(Blocks.CRAFTING_TABLE)) {

            world.playSound(player, pos, SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);

            if (!world.isRemote) {
                world.setBlockState(pos, BlockWorkbench.instance.getDefaultState());
            }
            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }

    @Override
    public void init(PacketPipeline packetPipeline) {
        // TODO register hammer recipe
//        IRecipe recipe = new ShapedOreRecipe(new ItemStack(this),
//                "..L",
//                ".S.",
//                "S..",
//                'L', "logWood",
//                'S', Items.STICK);
//
//        GameRegistry.addRecipe(recipe);
    }
}
