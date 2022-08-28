package se.mickelus.tetra;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class Tooltips {
    public static final Component reveal = Component.translatable("item.tetra.tooltip_reveal").withStyle(ChatFormatting.GRAY);


    public static final Component expand = Component.translatable("item.tetra.tooltip_expand");
    public static final Component expanded = Component.translatable("item.tetra.tooltip_expanded");
}
