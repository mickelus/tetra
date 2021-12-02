package se.mickelus.tetra.blocks.forged;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.material.PushReaction;
import se.mickelus.tetra.ToolTypes;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
public class ForgedBlockCommon {

    public static final Material forgedMaterial = new Material(MaterialColor.METAL, false, true, true, true, false, false, PushReaction.BLOCK);
    public static final Material forgedMaterialNotSolid = new Material(MaterialColor.METAL, false, false, true, false, false, false, PushReaction.BLOCK);

    public static final BlockBehaviour.Properties propertiesSolid = BlockBehaviour.Properties.of(forgedMaterial, MaterialColor.COLOR_GRAY)
            .harvestTool(ToolTypes.hammer)
            .harvestLevel(5)
            .requiresCorrectToolForDrops()
            .sound(SoundType.NETHERITE_BLOCK)
            .strength(12F, 2400.0F);

    public static final BlockBehaviour.Properties propertiesNotSolid = BlockBehaviour.Properties.of(forgedMaterialNotSolid, MaterialColor.COLOR_GRAY)
            .harvestTool(ToolTypes.hammer)
            .harvestLevel(5)
            .requiresCorrectToolForDrops()
            .noOcclusion()
            .sound(SoundType.NETHERITE_BLOCK)
            .isRedstoneConductor(ForgedBlockCommon::notSolid)
            .isSuffocating(ForgedBlockCommon::notSolid)
            .isViewBlocking(ForgedBlockCommon::notSolid)
            .strength(12F, 600.0F);

    public static final Component locationTooltip = new TranslatableComponent("item.tetra.forged_description")
            .withStyle(ChatFormatting.GRAY);

    public static final Component unsettlingTooltip = new TranslatableComponent("item.tetra.forged_unsettling")
            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);

    private static boolean notSolid(BlockState state, BlockGetter reader, BlockPos pos) {
        return false;
    }
}
