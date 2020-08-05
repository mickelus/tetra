package se.mickelus.tetra.items.modular.impl.toolbelt.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.gui.GuiUtils;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiTexture;
import se.mickelus.mgui.gui.impl.GuiKeybinding;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.modular.impl.toolbelt.OverlayToolbelt;
import se.mickelus.tetra.items.modular.impl.toolbelt.ToolbeltContainer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class ToolbeltGui extends ContainerScreen<ToolbeltContainer> {

    private static ToolbeltGui instance;

    private static final ResourceLocation playerInventoryTexture = new ResourceLocation(TetraMod.MOD_ID, "textures/gui/player-inventory.png");

    private GuiElement defaultGui;

    public ToolbeltGui(ToolbeltContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);

        this.xSize = 179;
        this.ySize = 176;

        int numQuickslots = container.getQuickslotInventory().getSizeInventory();
        int numStorageSlots = container.getStorageInventory().getSizeInventory();
        int numPotionSlots = container.getPotionInventory().getSizeInventory();
        int numQuiverSlots = container.getQuiverInventory().getSizeInventory();
        int offset = 0;

        defaultGui = new GuiElement(0, 0, xSize, ySize);

        // inventory background
        defaultGui.addChild(new GuiTexture(0, 103, 179, 106, playerInventoryTexture));

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
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack, 0);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        func_230459_a_(matrixStack, mouseX, mouseY);

        drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;

        defaultGui.draw(matrixStack, x, y, width, height, mouseX, mouseY, 1);
    }

    @Override
    protected void func_230459_a_(MatrixStack matrixStack, int mouseX, int mouseY) {
        super.func_230459_a_(matrixStack, mouseX, mouseY);
        List<String> tooltipLines = defaultGui.getTooltipLines();

        if (tooltipLines != null) {
            List<ITextComponent> textComponents = tooltipLines.stream()
                    .map(line -> line.replace("\\n", "\n"))
                    .flatMap(line -> Arrays.stream(line.split("\n")))
                    .map(StringTextComponent::new)
                    .collect(Collectors.toList());

            GuiUtils.drawHoveringText(matrixStack, textComponents, mouseX, mouseY, width, height, -1, font);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        return super.mouseClicked(mouseX, mouseY, mouseButton);

        // todo: clear shadow slot if rightclick and "normal" slot is empty
    }
}
