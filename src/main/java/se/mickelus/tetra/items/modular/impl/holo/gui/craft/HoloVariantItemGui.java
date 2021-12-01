package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import se.mickelus.mgui.gui.*;
import se.mickelus.tetra.blocks.workbench.gui.GuiModuleGlyph;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiItemRolling;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.module.schematic.OutcomePreview;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;
@ParametersAreNonnullByDefault
public class HoloVariantItemGui extends GuiClickable {
    protected GuiItemRolling material;

    protected GuiTexture backdrop;

    protected OutcomePreview outcome;
    protected Consumer<OutcomePreview> onHover;
    protected Consumer<OutcomePreview> onBlur;

    protected boolean isMuted = false;

    public HoloVariantItemGui(int x, int y, int width, int height, OutcomePreview outcome,
            Consumer<OutcomePreview> onHover, Consumer<OutcomePreview> onBlur, Consumer<OutcomePreview> onSelect) {
        super(x, y, width, height, () -> onSelect.accept(outcome));

        this.outcome = outcome;
        this.onHover = onHover;
        this.onBlur = onBlur;

        material = new GuiItemRolling(-1, -1)
                .setCountVisibility(GuiItem.CountMode.never)
                .setItems(outcome.materials);
    }

    public HoloVariantItemGui(int x, int y, OutcomePreview outcome, @Nullable String label,
            Consumer<OutcomePreview> onHover, Consumer<OutcomePreview> onBlur, Consumer<OutcomePreview> onSelect) {
        this(x, y, 11, 11, outcome, onHover, onBlur, onSelect);

        backdrop = new GuiTexture(0, 0, 11, 11, 68, 0, GuiTextures.workbench);
        addChild(backdrop);

        if (label != null) {
            GuiString labelElement = new GuiStringOutline(1, 1, label);
            labelElement.setColor(outcome.glyph.tint);
            labelElement.setAttachment(GuiAttachment.middleCenter);
            addChild(labelElement);
        } else {
            addChild(new GuiModuleGlyph(2, 2, 8, 8, outcome.glyph).setShift(false));
        }
    }

    public void updateSelection(OutcomePreview outcome) {
        isMuted = outcome != null && !this.outcome.equals(outcome);
        backdrop.setColor(isMuted ? GuiColors.muted : GuiColors.normal);
    }

    @Override
    protected void onFocus() {
        super.onFocus();

        onHover.accept(outcome);

        backdrop.setColor(GuiColors.hover);
    }

    @Override
    protected void onBlur() {
        super.onBlur();

        onBlur.accept(outcome);

        backdrop.setColor(isMuted ? GuiColors.muted : GuiColors.normal);
    }

    @Override
    protected void drawChildren(PoseStack matrixStack, int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        super.drawChildren(matrixStack, refX, refY, screenWidth, screenHeight, mouseX, mouseY, opacity);

        if (Screen.hasShiftDown()) {
            material.draw(matrixStack, refX+ material.getX(), refY + material.getY(), screenWidth, screenHeight, mouseX, mouseY, opacity);
        }
    }
}
