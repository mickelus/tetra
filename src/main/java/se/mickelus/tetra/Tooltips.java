package se.mickelus.tetra;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class Tooltips {
    public static final Component reveal = new TranslatableComponent("item.tetra.tooltip_reveal").withStyle(ChatFormatting.GRAY);


    public static final Component expand = new TranslatableComponent("item.tetra.tooltip_expand");
    public static final Component expanded = new TranslatableComponent("item.tetra.tooltip_expanded");
}
