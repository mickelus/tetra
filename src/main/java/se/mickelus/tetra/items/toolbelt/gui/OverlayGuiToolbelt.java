package se.mickelus.tetra.items.toolbelt.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.gui.GuiElement;
import se.mickelus.tetra.gui.GuiRect;
import se.mickelus.tetra.gui.GuiRoot;
import se.mickelus.tetra.gui.GuiTexture;
import se.mickelus.tetra.gui.animation.Applier;
import se.mickelus.tetra.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.items.toolbelt.InventoryToolbelt;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public class OverlayGuiToolbelt extends GuiRoot {

    private static final ResourceLocation toolbeltTexture = new ResourceLocation(TetraMod.MOD_ID, "textures/gui/toolbelt-inventory.png");

    private OverlayGuiItem[] items = new OverlayGuiItem[0];

    private KeyframeAnimation showAnimation;
    private KeyframeAnimation hideAnimation;
    private GuiElement strapList;

    private boolean hasMouseMoved = false;
    private ScaledResolution scaledResolution;

    public OverlayGuiToolbelt(Minecraft mc) {
        super(mc);

        scaledResolution = new ScaledResolution(mc);

        strapList = new GuiElement(37, 0, 0, 0);
        addChild(strapList);

        showAnimation = new KeyframeAnimation(200, strapList)
            .applyTo(new Applier.TranslateX(strapList.getX() + 10), new Applier.Opacity(1))
            .onStop(isFinished -> {
                if (isFinished) {
                    Arrays.stream(items)
                        .filter(Objects::nonNull)
                        .forEach(item -> item.setVisible(true));
                }
            });
        hideAnimation = new KeyframeAnimation(100, strapList)
            .applyTo(new Applier.TranslateX(strapList.getX()), new Applier.Opacity(0))
            .onStop(isFinished -> {
                if (isFinished) {
                    isVisible = false;
                }
            });

    }

    public void setInventoryToolbelt(InventoryToolbelt inventoryToolbelt) {
        strapList.clearChildren();
        int numSlots = inventoryToolbelt.getSizeInventory();
        items = new OverlayGuiItem[numSlots];

        strapList.addChild(new GuiTexture(0, numSlots * -12 - 7, 22, 7, 0, 28, toolbeltTexture));
        strapList.addChild(new GuiTexture(0, numSlots * 12, 22, 7, 0, 35, toolbeltTexture));
        strapList.addChild(new GuiRect(0, numSlots * -12, 22, numSlots * 24, 0xcc000000));

        for (int i = 0; i < numSlots; i++) {
            ItemStack itemStack = inventoryToolbelt.getStackInSlot(i);
            if (!itemStack.isEmpty()) {
                items[i] = new OverlayGuiItem(-35, numSlots * -12 + i * 24 + 3, itemStack, i);
                strapList.addChild(items[i]);
            }
        }
    }

    @Override
    protected void onShow() {
        if (hideAnimation.isActive()) {
            hideAnimation.stop();
        }
        scaledResolution = new ScaledResolution(mc);
        hasMouseMoved = false;
        showAnimation.start();
    }

    @Override
    protected boolean onHide() {
        if (showAnimation.isActive()) {
            showAnimation.stop();
        }
        hideAnimation.start();
        Arrays.stream(items)
            .filter(Objects::nonNull)
            .forEach(item -> item.setVisible(false));
        return false;
    }

    @Override
    public void draw() {
        if (isVisible()) {
            int mouseX, mouseY;
            if (!hasMouseMoved && (Mouse.getDX() > 0 || Mouse.getDY() > 0)) {
                hasMouseMoved = true;
            }
            int width = scaledResolution.getScaledWidth();
            int height = scaledResolution.getScaledHeight();
            if (hasMouseMoved) {
                mouseX = Mouse.getX() * width / mc.displayWidth;
                mouseY = height - Mouse.getY() * height / mc.displayHeight - 1;
            } else {
                mouseX = width / 2;
                mouseY = height / 2;
            }
            drawChildren(width/2, height/2, width, height, mouseX, mouseY, 1);
        }
    }

    public int getFocus() {
        for (int i = 0; i < items.length; i++) {
            OverlayGuiItem element = items[i];
            if (items[i] != null && element.hasFocus()) {
                return element.getSlot();
            }
        }
        return -1;
    }
}
