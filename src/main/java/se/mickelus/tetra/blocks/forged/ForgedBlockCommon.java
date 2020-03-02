package se.mickelus.tetra.blocks.forged;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.material.PushReaction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import se.mickelus.tetra.ToolTypes;

public class ForgedBlockCommon {

    public static final Material forgedMaterial = new Material(MaterialColor.IRON, false, true, true, true, false, false, false, PushReaction.BLOCK);

    public static final Block.Properties properties = Block.Properties.create(forgedMaterial)
            .sound(SoundType.METAL)
            .harvestTool(ToolTypes.hammer)
            .harvestLevel(3)
            .hardnessAndResistance(12F, 25);

    public static final ITextComponent locationTooltip = new TranslationTextComponent("item.tetra.forged_description")
            .setStyle(new Style().setColor(TextFormatting.GRAY));

    public static final ITextComponent unsettlingTooltip = new TranslationTextComponent("item.tetra.forged_unsettling")
            .setStyle(new Style().setColor(TextFormatting.GRAY).setItalic(true));
}
