package se.mickelus.tetra;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class Tooltips {
    public static final ITextComponent reveal = new TranslationTextComponent("item.tetra.tooltip_reveal")
            .setStyle(new Style().setColor(TextFormatting.GRAY));


    public static final ITextComponent expand = new TranslationTextComponent("item.tetra.tooltip_expand");
    public static final ITextComponent expanded = new TranslationTextComponent("item.tetra.tooltip_expanded");
}
