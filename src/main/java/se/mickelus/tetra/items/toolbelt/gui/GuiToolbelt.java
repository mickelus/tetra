package se.mickelus.tetra.items.toolbelt.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.gui.*;
import se.mickelus.tetra.gui.impl.GuiKeybinding;
import se.mickelus.tetra.items.toolbelt.ContainerToolbelt;
import se.mickelus.tetra.items.toolbelt.OverlayToolbelt;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@SideOnly(Side.CLIENT)
public class GuiToolbelt extends GuiContainer {

    private static GuiToolbelt instance;

    private static final ResourceLocation INVENTORY_TEXTURE = new ResourceLocation(TetraMod.MOD_ID, "textures/gui/player-inventory.png");

    private GuiElement defaultGui;

    public GuiToolbelt(ContainerToolbelt container) {
        super(container);
        this.allowUserInput = false;
        this.xSize = 179;
        this.ySize = 176;

        int numQuickslots = container.getQuickslotInventory().getSizeInventory();
        int numStorageSlots = container.getStorageInventory().getSizeInventory();
        int numPotionSlots = container.getPotionInventory().getSizeInventory();
        int numQuiverSlots = container.getQuiverInventory().getSizeInventory();
        int offset = 0;

        defaultGui = new GuiElement(0, 0, xSize, ySize);

        // inventory background
        defaultGui.addChild(new GuiTexture(0, 103, 179, 106, INVENTORY_TEXTURE));

        if (numPotionSlots > 0) {
            defaultGui.addChild(new GuiPotionsBackdrop(0, 55 - 30 * offset, numPotionSlots, container.getPotionInventory().getSlotEffects()));
            offset++;
        }

        if (numQuiverSlots > 0) {
            defaultGui.addChild(new GuiQuiverBackdrop(0, 55 - 30 * offset, numQuiverSlots, container.getQuiverInventory().getSlotEffects()));
            offset++;
        }

        if (numQuickslots > 0 ) {
            defaultGui.addChild(new GuiQuickSlotBackdrop(0, 55 - 30 * offset, numQuickslots, container.getQuickslotInventory().getSlotEffects()));
            offset++;
        }

        if (numStorageSlots > 0) {
            defaultGui.addChild(new GuiStorageBackdrop(0, 55 - 30 * offset, numStorageSlots, container.getStorageInventory().getSlotEffects()));
        }

        defaultGui.addChild(new GuiKeybinding(166, 85, OverlayToolbelt.instance.accessBinding));
        defaultGui.addChild(new GuiKeybinding(166, 100, OverlayToolbelt.instance.restockBinding));

        instance = this;
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
        drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        defaultGui.draw(x, y, width, height, mouseX, mouseY, 1);
    }

    @Override
    protected void renderHoveredToolTip(int mouseX, int mouseY) {
        super.renderHoveredToolTip(mouseX, mouseY);
        List<String> tooltipLines = defaultGui.getTooltipLines();

        if (tooltipLines != null) {
            tooltipLines = tooltipLines.stream()
                    .map(line -> line.replace("\\n", "\n"))
                    .collect(Collectors.toList());

            GuiUtils.drawHoveringText(tooltipLines, mouseX, mouseY, width, height, -1, fontRenderer);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        // todo: clear shadow slot if rightclick and "normal" slot is empty
    }
}
