package se.mickelus.tetra.blocks.workbench;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.advancements.BlockUseCriterion;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;

public class BasicWorkbenchBlock extends AbstractWorkbenchBlock {
    public static final String unlocalizedName = "basic_workbench";
    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static AbstractWorkbenchBlock instance;

    public BasicWorkbenchBlock() {
        super(Properties.of(Material.WOOD)
                .strength(2.5f)
                .sound(SoundType.WOOD));

        setRegistryName(unlocalizedName);

        hasItem = true;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag advanced) {
        tooltip.add(new TranslatableComponent("block.tetra.basic_workbench.description").withStyle(ChatFormatting.GRAY));
    }

    public static InteractionResult upgradeWorkbench(Player player, Level world, BlockPos pos, InteractionHand hand, Direction facing) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (!player.mayUseItemAt(pos.relative(facing), facing, itemStack)) {
            return InteractionResult.FAIL;
        }

        if (world.getBlockState(pos).getBlock().equals(Blocks.CRAFTING_TABLE)) {

            world.playSound(player, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 0.5F);

            if (!world.isClientSide) {
                world.setBlockAndUpdate(pos, instance.defaultBlockState());

                BlockUseCriterion.trigger((ServerPlayer) player, instance.defaultBlockState(), ItemStack.EMPTY);
            }
            return InteractionResult.sidedSuccess(world.isClientSide);
        }

        return InteractionResult.PASS;
    }
}
