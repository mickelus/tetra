package se.mickelus.tetra.blocks.forged;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ForgedBlockCommon {
    public static final Block.Properties propertiesSolid = Block.Properties.of()
            .requiresCorrectToolForDrops()
            .sound(SoundType.NETHERITE_BLOCK)
            .strength(12F, 2400.0F);

    public static final Block.Properties propertiesNotSolid = Block.Properties.of()
            .requiresCorrectToolForDrops()
            .noOcclusion()
            .sound(SoundType.NETHERITE_BLOCK)
            .isRedstoneConductor(ForgedBlockCommon::notSolid)
            .isSuffocating(ForgedBlockCommon::notSolid)
            .isViewBlocking(ForgedBlockCommon::notSolid)
            .strength(12F, 600.0F);

    public static final Component locationTooltip = Component.translatable("item.tetra.forged_description")
            .withStyle(ChatFormatting.GRAY);

    public static final Component unsettlingTooltip = Component.translatable("item.tetra.forged_unsettling")
            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);

    private static boolean notSolid(BlockState state, BlockGetter reader, BlockPos pos) {
        return false;
    }
}
