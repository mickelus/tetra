package se.mickelus.tetra;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class TextHelper {
    public static final ITextComponent forgedBlockTooltip = new TranslationTextComponent("forged_description")
            .setStyle(new Style().setColor(TextFormatting.GRAY));
}
