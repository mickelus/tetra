package se.mickelus.tetra.blocks.workbench;

import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.advancements.BlockUseCriterion;

import javax.annotation.Nullable;
import java.util.List;

public class BasicWorkbenchBlock extends AbstractWorkbenchBlock {
    public static final String unlocalizedName = "basic_workbench";
    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static AbstractWorkbenchBlock instance;

    public BasicWorkbenchBlock() {
        super(Properties.create(Material.WOOD)
                .hardnessAndResistance(2.5f)
                .sound(SoundType.WOOD));

        setRegistryName(unlocalizedName);

        hasItem = true;
    }

    @Override
    public void addInformation(ItemStack itemStack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced) {
        tooltip.add(new TranslationTextComponent("block.tetra.basic_workbench.description").mergeStyle(TextFormatting.GRAY));
    }

    public static ActionResultType upgradeWorkbench(PlayerEntity player, World world, BlockPos pos, Hand hand, Direction facing) {
        ItemStack itemStack = player.getHeldItem(hand);
        if (!player.canPlayerEdit(pos.offset(facing), facing, itemStack)) {
            return ActionResultType.FAIL;
        }

        if (world.getBlockState(pos).getBlock().equals(Blocks.CRAFTING_TABLE)) {

            world.playSound(player, pos, SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 1.0F, 0.5F);

            if (!world.isRemote) {
                world.setBlockState(pos, instance.getDefaultState());

                BlockUseCriterion.trigger((ServerPlayerEntity) player, instance.getDefaultState(), ItemStack.EMPTY);
            }
            return ActionResultType.func_233537_a_(world.isRemote);
        }

        return ActionResultType.PASS;
    }
}
