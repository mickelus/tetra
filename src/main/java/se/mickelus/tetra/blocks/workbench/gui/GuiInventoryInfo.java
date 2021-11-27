package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

public class GuiInventoryInfo extends GuiElement {
    private PlayerInventory inventory;
    private ItemStack targetStack;
    private String slot;
    private UpgradeSchematic schematic;

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
        if (schematic != null && targetStack != null) {
            // player inventory
            for (int x = 0; x < 9; x++) {
                for (int y = 0; y < 3; y++) {
                    int stackIndex = y * 9 + x + 9;
                    boolean shouldHighlight = false;

                    for (int materialIndex = 0; materialIndex < schematic.getNumMaterialSlots(); materialIndex++) {
                        if (schematic.acceptsMaterial(targetStack, slot, materialIndex, inventory.getStackInSlot(stackIndex))) {
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

                for (int materialIndex = 0; materialIndex < schematic.getNumMaterialSlots(); materialIndex++) {
                    if (schematic.acceptsMaterial(targetStack, slot, materialIndex, inventory.getStackInSlot(stackIndex))) {
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

    public void update(UpgradeSchematic schematic, String slot, ItemStack targetStack) {
        this.schematic = schematic;
        this.slot = slot;
        this.targetStack = targetStack;
        update();
    }
}
