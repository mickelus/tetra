package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.gui.GuiElement;
import se.mickelus.tetra.gui.GuiTexture;
import se.mickelus.tetra.module.schema.UpgradeSchema;

public class GuiInventoryInfo extends GuiElement {
    private static final String WORKBENCH_TEXTURE = "textures/gui/workbench.png";
    private InventoryPlayer inventory;
    private ItemStack targetStack;
    private UpgradeSchema schema;

    public GuiInventoryInfo(int x, int y, EntityPlayer player) {
        super(x, y, 224, 72);

        this.inventory = player.inventory;
    }

    public void update() {
        clearChildren();

        if (schema != null && targetStack != null) {
            // player inventory
            for (int x = 0; x < 9; x++) {
                for (int y = 0; y < 3; y++) {
                    int stackIndex = y * 9 + x + 9;
                    for (int materialIndex = 0; materialIndex < schema.getNumMaterialSlots(); materialIndex++) {
                        if (schema.acceptsMaterial(targetStack, materialIndex, inventory.getStackInSlot(stackIndex))) {
                            this.addChild(new GuiTexture(x * 17, y * 17 + 2, 16, 16, 80, 16, WORKBENCH_TEXTURE));
                            break;
                        }
                    }
                }
            }

            // player toolbar
            for (int stackIndex = 0; stackIndex < 9; stackIndex++) {
                for (int materialIndex = 0; materialIndex < schema.getNumMaterialSlots(); materialIndex++) {
                    if (schema.acceptsMaterial(targetStack, materialIndex, inventory.getStackInSlot(stackIndex))) {
                        this.addChild(new GuiTexture(stackIndex * 17, 57, 16, 16, 80, 16, WORKBENCH_TEXTURE));
                        break;
                    }
                }
            }
        }
    }

    public void update(UpgradeSchema schema, ItemStack targetStack) {
        this.schema = schema;
        this.targetStack = targetStack;
        update();
    }
}
