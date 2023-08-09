package se.mickelus.tetra.blocks.multischematic;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import se.mickelus.tetra.Tooltips;

import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;

public class BaseMultiblockSchematicItem extends BlockItem {
    MultiblockSchematicBlock schematicBlock;

    public BaseMultiblockSchematicItem(Block placedBlock, MultiblockSchematicBlock tooltipBlock) {
        super(placedBlock, new Properties());
        this.schematicBlock = tooltipBlock;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("block.tetra.multi_schematic." + schematicBlock.schematic + ".name");
    }

    protected List<Component> getTooltip() {
        List<Component> tooltip = new LinkedList<>();
        tooltip.add(Component.translatable("block.tetra.multi_schematic.piece", schematicBlock.x, schematicBlock.y).withStyle(ChatFormatting.GRAY));

        for (int y = schematicBlock.height - 1; y >= 0; y--) {
            StringJoiner part = new StringJoiner(" ");
            for (int x = 0; x < schematicBlock.width; x++) {
                part.add(x == schematicBlock.x && y == schematicBlock.y
                        ? ChatFormatting.WHITE + "\u25c6"
                        : ChatFormatting.GRAY + "\u25c7");
            }
            tooltip.add(Component.literal(part.toString()));
        }

        if (I18n.exists("block.tetra.multi_schematic." + schematicBlock.schematic + ".description")) {
            tooltip.add(Component.literal(" "));
            tooltip.add(Component.translatable("block.tetra.multi_schematic." + schematicBlock.schematic + ".description").withStyle(ChatFormatting.GRAY));
        }

        tooltip.add(Component.literal(" "));

        if (Screen.hasShiftDown()) {
            tooltip.add(Tooltips.expanded);

            tooltip.add(Component.literal(" "));
            tooltip.add(Component.translatable("block.tetra.multi_schematic.description").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Tooltips.expand);
        }

        return tooltip;
    }
}
