package se.mickelus.tetra.blocks.forged;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import se.mickelus.tetra.ToolTypes;

public class ForgedBlockCommon {
    public static Material material = new Material.Builder(MaterialColor.IRON).build();

    public static final Block.Properties properties = Block.Properties.create(material)
            .sound(SoundType.METAL)
            .harvestTool(ToolTypes.hammer)
            .harvestLevel(4)
            .hardnessAndResistance(10F, 25);

    public static final ITextComponent hintTooltip = new TranslationTextComponent("forged_description")
            .setStyle(new Style().setColor(TextFormatting.GRAY));
}
