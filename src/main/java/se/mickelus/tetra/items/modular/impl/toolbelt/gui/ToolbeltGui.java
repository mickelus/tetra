package se.mickelus.tetra.items.modular.impl.toolbelt.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.gui.GuiUtils;
import se.mickelus.mgui.gui.*;
import se.mickelus.mgui.gui.impl.GuiHorizontalLayoutGroup;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiKeybinding;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.items.modular.impl.toolbelt.OverlayToolbelt;
import se.mickelus.tetra.items.modular.impl.toolbelt.ToolbeltContainer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class ToolbeltGui extends AbstractContainerScreen<ToolbeltContainer> {

    private static ToolbeltGui instance;

    private GuiElement defaultGui;
    private GuiElement keybindGui;

    public ToolbeltGui(ToolbeltContainer container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);

        this.imageWidth = 179;
        this.imageHeight = 240;

        int numQuickslots = container.getQuickslotInventory().getContainerSize();
        int numStorageSlots = container.getStorageInventory().getContainerSize();
        int numPotionSlots = container.getPotionInventory().getContainerSize();
        int numQuiverSlots = container.getQuiverInventory().getContainerSize();
        int offset = 0;

        defaultGui = new GuiElement(0, 0, imageWidth, imageHeight);

        // inventory background
        defaultGui.addChild(new GuiTexture(0, 129, 179, 91, GuiTextures.playerInventory));

        if (numStorageSlots > 0) {
            GuiStorageBackdrop storageBackdrop = new GuiStorageBackdrop(0, 130 - offset, numStorageSlots, container.getStorageInventory().getSlotEffects());
            defaultGui.addChild(storageBackdrop);
            offset += storageBackdrop.getHeight() + 2;
        }

        if (numQuiverSlots > 0) {
            GuiQuiverBackdrop quiverBackdrop = new GuiQuiverBackdrop(0, 130 - offset, numQuiverSlots, container.getQuiverInventory().getSlotEffects());
            defaultGui.addChild(quiverBackdrop);
            offset += quiverBackdrop.getHeight() + 2;
        }

        if (numPotionSlots > 0) {
            GuiPotionsBackdrop potionsBackdrop = new GuiPotionsBackdrop(0, 130 - offset, numPotionSlots, container.getPotionInventory().getSlotEffects());
            defaultGui.addChild(potionsBackdrop);
            offset += potionsBackdrop.getHeight() + 2;
        }

        if (numQuickslots > 0 ) {
            GuiQuickSlotBackdrop quickSlotBackdrop = new GuiQuickSlotBackdrop(0, 130 - offset, numQuickslots, container.getQuickslotInventory().getSlotEffects());
            defaultGui.addChild(quickSlotBackdrop);
            offset += quickSlotBackdrop.getHeight() + 2;
        }


        keybindGui = new GuiElement(0, 0, 3840, 23);
        keybindGui.addChild(new GuiRect(0, 0, 3840, 23, 0xcc000000).setAttachment(GuiAttachment.bottomCenter));
        keybindGui.addChild(new GuiRect(0, -21, 3840, 1, GuiColors.mutedStrong).setAttachment(GuiAttachment.bottomCenter));
        GuiHorizontalLayoutGroup keybindGroup = new GuiHorizontalLayoutGroup(0, -5, 11, 8);
        keybindGroup.setAttachment(GuiAttachment.bottomCenter);
        keybindGui.addChild(keybindGroup);
        keybindGroup.addChild(new GuiKeybinding(0, 0, OverlayToolbelt.instance.accessBinding));
        keybindGroup.addChild(new GuiRect(0, -1, 1, 13, GuiColors.mutedStrong));
        keybindGroup.addChild(new GuiKeybinding(0, 0, OverlayToolbelt.instance.restockBinding));
        keybindGroup.addChild(new GuiRect(0, -1, 1, 13, GuiColors.mutedStrong));
        keybindGroup.addChild(new GuiKeybinding(0, 0, OverlayToolbelt.instance.openBinding));

        instance = this;
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack, 0);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        renderTooltip(matrixStack, mouseX, mouseY);

        renderLabels(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        keybindGui.setWidth(width);
        keybindGui.draw(matrixStack, 0, height - keybindGui.getHeight(), width, height, mouseX, mouseY, 1);
        defaultGui.draw(matrixStack, x, y, width, height, mouseX, mouseY, 1);
    }

    @Override
    protected void renderTooltip(PoseStack matrixStack, int mouseX, int mouseY) {
        super.renderTooltip(matrixStack, mouseX, mouseY);
        List<String> tooltipLines = defaultGui.getTooltipLines();

        if (tooltipLines != null) {
            List<Component> textComponents = tooltipLines.stream()
                    .map(line -> line.replace("\\n", "\n"))
                    .flatMap(line -> Arrays.stream(line.split("\n")))
                    .map(TextComponent::new)
                    .collect(Collectors.toList());

            GuiUtils.drawHoveringText(matrixStack, textComponents, mouseX, mouseY, width, height, 280, font);
        }
    }

    @Override
    protected void renderLabels(PoseStack matrixStack, int x, int y) { }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        return super.mouseClicked(mouseX, mouseY, mouseButton);

        // todo: clear shadow slot if rightclick and "normal" slot is empty
    }
}
