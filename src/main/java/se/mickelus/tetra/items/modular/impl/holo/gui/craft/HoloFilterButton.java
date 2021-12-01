package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.glfw.GLFW;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiString;
import se.mickelus.mgui.gui.GuiTexture;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiTextures;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class HoloFilterButton extends GuiElement {
    private final List<String> tooltip = Collections.singletonList(I18n.get("tetra.holo.craft.variants_filter"));
    private boolean inputFocused = false;
    private String filter = "";
    private Consumer<String> onChange;

    private GuiTexture icon;
    private final GuiString label;

    public HoloFilterButton(int x, int y, Consumer<String> onChange) {
        super(x, y, 11, 9);

        this.onChange = onChange;

        icon = new GuiTexture(0, 0, 9, 9, 206, 0, GuiTextures.workbench);
        icon.setColor(GuiColors.muted);
        addChild(icon);

        label = new GuiString(11, 0, "");
        addChild(label);
    }

    @Override
    public boolean onMouseClick(int x, int y, int button) {
        if (hasFocus()) {
            setInputFocused(true);
            return true;
        }

        return false;
    }

    @Override
    public List<String> getTooltipLines() {
        if (hasFocus()) {
            return tooltip;
        }
        return null;
    }

    private void setInputFocused(boolean focused) {
        this.inputFocused = focused;

        if (inputFocused) {
            icon.setColor(GuiColors.hover);
        } else if (filter.length() > 0) {
            icon.setColor(GuiColors.normal);
        } else {
            icon.setColor(GuiColors.muted);
        }
    }

    @Override
    public boolean onKeyPress(int keyCode, int scanCode, int modifiers) {
        switch (keyCode) {
            case GLFW.GLFW_KEY_BACKSPACE:
                if (Screen.hasControlDown()) {
                    updateFilter("");
                }
                if (filter.length() > 0) {
                    updateFilter(StringUtils.chop(filter));
                }
                return true;
            case GLFW.GLFW_KEY_ENTER:
                setInputFocused(!inputFocused);
                return true;
            case GLFW.GLFW_KEY_ESCAPE:
                if (inputFocused) {
                    setInputFocused(false);
                    return true;
                }
                break;

        }

        return false;
    }

    @Override
    public boolean onCharType(char character, int modifiers) {
        if (inputFocused) {
            updateFilter(filter += character);
            return true;
        }

        if (character == 'f') {
            setInputFocused(true);
            return true;
        }

        return false;
    }

    public void updateFilter(String newValue) {
        filter = newValue;
        label.setString(filter);
        onChange.accept(filter);
        setWidth(11 + label.getWidth());
    }

    public void reset() {
        filter = "";
        label.setString(filter);
        setWidth(11);
    }

    @Override
    public void draw(PoseStack matrixStack, int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        super.draw(matrixStack, refX, refY, screenWidth, screenHeight, mouseX, mouseY, opacity);

        if (inputFocused && System.currentTimeMillis() % 800 < 400) {
            drawRect(matrixStack, refX + x + 12 + label.getWidth(), refY + y + 7, refX + x + 17 + label.getWidth(), refY + y + 8, GuiColors.normal, 1f);
        }
    }
}
