package se.mickelus.tetra.blocks.multischematic;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import se.mickelus.mutil.gui.GuiAttachment;
import se.mickelus.mutil.gui.GuiRoot;
import se.mickelus.mutil.gui.GuiString;
import se.mickelus.mutil.gui.impl.GuiVerticalLayoutGroup;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.StringJoiner;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
public class MultiblockSchematicGui extends GuiRoot implements IGuiOverlay {
    private final GuiVerticalLayoutGroup element;
    private int selected = -1;

    public MultiblockSchematicGui(Minecraft mc) {
        super(mc);

        element = new GuiVerticalLayoutGroup(12, 0, 0, 1);
        element.setAttachmentPoint(GuiAttachment.middleLeft);
        element.setAttachmentAnchor(GuiAttachment.middleCenter);
        addChild(element);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (TickEvent.Phase.END == event.phase
                && mc.player != null
                && mc.level != null
                && (mc.level.getGameTime() % 10 == 0 || selected != mc.player.getInventory().selected)) {
            this.selected = mc.player.getInventory().selected;
            element.clearChildren();

            ItemStack itemStack = Stream.of(mc.player.getMainHandItem(), mc.player.getOffhandItem())
                    .filter(stack -> !stack.isEmpty())
                    .filter(stack -> stack.getItem() instanceof StackedMultiblockSchematicItem)
                    .findFirst()
                    .orElse(ItemStack.EMPTY);

            if (!itemStack.isEmpty()) {
                MultiblockSchematicBlock block = ((StackedMultiblockSchematicItem) itemStack.getItem()).schematicBlock;

                for (int y = block.height - 1; y >= 0; y--) {
                    StringJoiner part = new StringJoiner(" ");
                    for (int x = 0; x < block.width; x++) {
                        part.add(x == block.x && y == block.y
                                ? ChatFormatting.WHITE + "\u25c6"
                                : ChatFormatting.GRAY + "\u25c7");
                    }
                    element.addChild(new GuiString(0, 0, part.toString()));
                }
                element.forceLayout();
            }
        }
    }

    @Override
    public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        this.draw(graphics);
    }
}
