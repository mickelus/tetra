package se.mickelus.tetra.blocks.forged;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.material.PushReaction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import se.mickelus.tetra.ToolTypes;

public class ForgedBlockCommon {

    public static final Material forgedMaterial = new Material(MaterialColor.IRON, false, true, true, true, false, false, PushReaction.BLOCK);

    public static final Block.Properties propertiesSolid = Block.Properties.create(forgedMaterial)
            .sound(SoundType.METAL)
            .harvestTool(ToolTypes.hammer)
            .harvestLevel(5)
            .setRequiresTool()
            .sound(SoundType.NETHERITE)
            .hardnessAndResistance(12F, 2400.0F);

    public static final Block.Properties propertiesNotSolid = Block.Properties.create(forgedMaterial)
            .sound(SoundType.METAL)
            .harvestTool(ToolTypes.hammer)
            .harvestLevel(5)
            .setRequiresTool()
            .notSolid()
            .sound(SoundType.NETHERITE)
            .setOpaque(ForgedBlockCommon::notSolid)
            .setSuffocates(ForgedBlockCommon::notSolid)
            .setBlocksVision(ForgedBlockCommon::notSolid)
            .hardnessAndResistance(12F, 600.0F);

    public static final ITextComponent locationTooltip = new TranslationTextComponent("item.tetra.forged_description")
            .mergeStyle(TextFormatting.GRAY);

    public static final ITextComponent unsettlingTooltip = new TranslationTextComponent("item.tetra.forged_unsettling")
            .mergeStyle(TextFormatting.GRAY, TextFormatting.ITALIC);

    private static boolean notSolid(BlockState state, IBlockReader reader, BlockPos pos) {
        return false;
    }
}
