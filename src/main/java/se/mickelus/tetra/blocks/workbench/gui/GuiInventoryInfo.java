package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.gui.GuiElement;
import se.mickelus.tetra.gui.GuiTexture;
import se.mickelus.tetra.gui.animation.Applier;
import se.mickelus.tetra.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.module.schema.UpgradeSchema;

public class GuiInventoryInfo extends GuiElement {
    private InventoryPlayer inventory;
    private ItemStack targetStack;
    private UpgradeSchema schema;

    private GuiInventoryHighlight[] highlights;

    public GuiInventoryInfo(int x, int y, PlayerEntity player) {
        super(x, y, 224, 72);

        this.inventory = player.inventory;

        highlights = new GuiInventoryHighlight[36];

        // player inventory
        for (int xo = 0; xo < 9; xo++) {
            for (int yo = 0; yo < 3; yo++) {
                GuiInventoryHighlight highlight = new GuiInventoryHighlight(xo * 17, yo * 17 + 2, xo + yo);
                highlight.setVisible(false);
                this.addChild(highlight);
                highlights[9 + yo * 9 + xo] = highlight;
            }
        }

        // player toolbar
        for (int xo = 0; xo < 9; xo++) {
            GuiInventoryHighlight highlight = new GuiInventoryHighlight(xo * 17, 57, 4 + xo);
            highlight.setVisible(false);
            this.addChild(highlight);
            highlights[xo] = highlight;
        }
    }

    public void update() {
        if (schema != null && targetStack != null) {
            // player inventory
            for (int x = 0; x < 9; x++) {
                for (int y = 0; y < 3; y++) {
                    int stackIndex = y * 9 + x + 9;
                    boolean shouldHighlight = false;

                    for (int materialIndex = 0; materialIndex < schema.getNumMaterialSlots(); materialIndex++) {
                        if (schema.acceptsMaterial(targetStack, materialIndex, inventory.getStackInSlot(stackIndex))) {
                            shouldHighlight = true;
                            break;
                        }
                    }

                    highlights[stackIndex].setVisible(shouldHighlight);
                }
            }

            // player toolbar
            for (int stackIndex = 0; stackIndex < 9; stackIndex++) {
                boolean shouldHighlight = false;

                for (int materialIndex = 0; materialIndex < schema.getNumMaterialSlots(); materialIndex++) {
                    if (schema.acceptsMaterial(targetStack, materialIndex, inventory.getStackInSlot(stackIndex))) {
                        shouldHighlight = true;
                        break;
                    }
                }

                highlights[stackIndex].setVisible(shouldHighlight);
            }
        } else {
            for (GuiInventoryHighlight highlight : highlights) {
                highlight.setVisible(false);
            }
        }
    }

    public void update(UpgradeSchema schema, ItemStack targetStack) {
        this.schema = schema;
        this.targetStack = targetStack;
        update();
    }
}
