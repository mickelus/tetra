package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import se.mickelus.mutil.gui.GuiButton;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.GuiText;
import se.mickelus.mutil.gui.GuiTexture;
import se.mickelus.mutil.gui.animation.AnimationChain;
import se.mickelus.mutil.gui.animation.Applier;
import se.mickelus.mutil.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class GuiSchematicList extends GuiElement {
    private static final int pageLength = 8;
    private final Consumer<UpgradeSchematic> schematicSelectionConsumer;
    private final GuiElement listGroup;
    private final GuiButton buttonBack;
    private final GuiButton buttonForward;
    private final GuiText emptyStateText;
    private final int buttonBackX;
    private final int buttonForwardX;
    private final AnimationChain flash;
    private int page = 0;
    private UpgradeSchematic[] schematics;

    public GuiSchematicList(int x, int y, Consumer<UpgradeSchematic> schematicSelectionConsumer) {
        super(x, y, 224, 67);

        addChild(new GuiTexture(-4, -4, 239, 70, 0, 48, GuiTextures.workbench));

        listGroup = new GuiElement(3, 3, width - 6, height - 6);
        addChild(listGroup);

        String previousLabel = "< " + I18n.get("tetra.workbench.schematic_list.previous");
        int previousButtonWidth = Minecraft.getInstance().font.width(previousLabel) + 10;
        int previousButtonRightEdge = 36;
        buttonBackX = previousButtonRightEdge - previousButtonWidth;
        buttonBack = new GuiButton(buttonBackX, height, previousButtonWidth, 8, previousLabel, () -> setPage(getPage() - 1));
        addChild(buttonBack);
        String nextLabel = I18n.get("tetra.workbench.schematic_list.next") + " >";
        int nextButtonWidth = Minecraft.getInstance().font.width(nextLabel) + 10;
        buttonForwardX = width - 27;
        buttonForward = new GuiButton(buttonForwardX, height, nextButtonWidth, 8, nextLabel, () -> setPage(getPage() + 1));
        addChild(buttonForward);

        emptyStateText = new GuiText(10, 23, 204, ChatFormatting.GRAY + I18n.get("tetra.workbench.schematic_list.empty"));
        addChild(emptyStateText);

        this.schematicSelectionConsumer = schematicSelectionConsumer;

        GuiTexture flashOverlay = new GuiTexture(-4, -4, 239, 70, 0, 48, GuiTextures.workbench);
        flashOverlay.setOpacity(0);
        flashOverlay.setColor(0);
        addChild(flashOverlay);
        flash = new AnimationChain(
                new KeyframeAnimation(40, flashOverlay).applyTo(new Applier.Opacity(0.3f)),
                new KeyframeAnimation(80, flashOverlay).applyTo(new Applier.Opacity(0)));
    }

    public void setSchematics(UpgradeSchematic[] schematics) {
        this.schematics = schematics;
        emptyStateText.setVisible(schematics.length == 0);
        setPage(0);
    }

    private void updateSchematics() {
        int offset = page * pageLength;
        int count = pageLength;

        if (count + offset > schematics.length) {
            count = schematics.length - offset;
        }

        listGroup.clearChildren();
        for (int i = 0; i < count; i++) {
            UpgradeSchematic schematic = schematics[i + offset];
            listGroup.addChild(new GuiSchematicListItem(
                    i / (pageLength / 2) * 109,
                    i % (pageLength / 2) * 14,
                    schematic, () -> schematicSelectionConsumer.accept(schematic)));
        }
    }

    private int getPage() {
        return page;
    }

    private void setPage(int page) {
        this.page = page;

        buttonBack.setX(page > 0 ? buttonBackX : -1000);
        buttonForward.setX(page < getNumPages() - 1 ? buttonForwardX : -1000);
        updateSchematics();

    }

    private int getNumPages() {
        return (int) Math.ceil(1f * schematics.length / pageLength);
    }

    public void flash() {
        this.flash.stop();
        this.flash.start();
    }
}
