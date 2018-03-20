package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.gui.*;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ModuleData;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class GuiModuleMajor extends GuiClickable {

    private int color;

    private GuiModuleBackdrop backdrop;
    private GuiStringSmall slotString;
    private GuiString moduleString;

    private String slotKey;
    private boolean isPreview;
    private boolean isEmpty;

    private GuiModuleImprovement[] improvementElements;

    public GuiModuleMajor(int x, int y, ItemStack itemStack, ItemStack previewStack, String slotKey, String slotName,
                          ItemModuleMajor module, ItemModuleMajor previewModule,
                          Consumer<String> slotClickHandler) {
        super(x, y, 0, 16, () -> slotClickHandler.accept(slotKey));

        ModuleData data = null;
        ModuleData previewData = null;

        this.slotKey = slotKey;

        improvementElements = new GuiModuleImprovement[0];

        slotString = new GuiStringSmall(19, 0, slotName);
        addChild(slotString);

        if (module != null) {
             data = module.getData(itemStack);
        }

        if (previewModule != null) {
            previewData = previewModule.getData(previewStack);
        }

        if (previewData != null && !previewData.equals(data)) {
            color = GuiColors.change;
            isPreview = true;
            setupModule(previewModule, previewData, previewStack, color);
        } else if (data != null) {
            color = GuiColors.normal;
            setupModule(module, data, itemStack, color);
        } else {
            isEmpty = true;
            color = GuiColors.muted;
            setupModule(null, null, itemStack, color);
        }

        if (module != null && previewModule != null) {
            setupImprovements(previewModule, previewStack, module, itemStack);
        }
    }

    public void setFocusSlot(String focusSlotKey) {
        if(slotKey.equals(focusSlotKey)) {
            color = GuiColors.normal;
            Arrays.stream(improvementElements).forEach(element -> element.setOpacity(1));
        } else if (!isEmpty && focusSlotKey == null){
            color = GuiColors.normal;
            Arrays.stream(improvementElements).forEach(element -> element.setOpacity(1));
        } else {
            color = GuiColors.muted;
            Arrays.stream(improvementElements).forEach(element -> element.setOpacity(0.5f));
        }
        setColors(color);
    }

    private void setupModule(ItemModuleMajor module, ModuleData data, ItemStack itemStack, int color) {
        String moduleName = "Empty";
        backdrop = new GuiModuleBackdrop(1, 0, color);
        addChild(backdrop);

        if (module != null) {
            moduleName = module.getName(itemStack);
        }
        moduleString = new GuiString(19, 5, moduleName, color);
        addChild(moduleString);

        width = Minecraft.getMinecraft().fontRenderer.getStringWidth(moduleName) + 19;

        if (data != null) {
            addChild(new GuiModuleGlyph(0, 0, 16, 16,
                    data.glyph.tint, data.glyph.textureX, data.glyph.textureY,
                    data.glyph.textureLocation));
        }
    }

    private void setupImprovements(ItemModuleMajor previewModule, ItemStack previewStack, ItemModuleMajor module, ItemStack itemStack) {
        String[] improvements = getImprovementUnion(module.getImprovements(itemStack), previewModule.getImprovements(previewStack));
        improvementElements = new GuiModuleImprovement[improvements.length];
        for (int i = 0; i < improvements.length; i++) {
            int currentValue = module.getImprovementLevel(improvements[i], itemStack);
            int previewValue = previewModule.getImprovementLevel(improvements[i], previewStack);
            int color = GuiColors.normal;

            if (currentValue == 0) {
                color = GuiColors.add;
            } else if (previewValue == 0) {
                color = GuiColors.remove;
            } else if (currentValue != previewValue) {
                color = GuiColors.change;
            }

            improvementElements[i] = new GuiModuleImprovement(19 + i * 5, 13, improvements[i], previewValue, color);
            addChild(improvementElements[i]);
        }
    }

    public static String[] getImprovementUnion(String[] improvements, String[] previewImprovements) {
        Set<String> result = new HashSet<>(Arrays.asList(improvements));
        result.addAll(Arrays.asList(previewImprovements));
        return result.toArray(new String[result.size()]);
    }

    private void setColors(int color) {
        if (isPreview) {
            backdrop.setColor(GuiColors.change);
            moduleString.setColor(GuiColors.change);
            slotString.setColor(GuiColors.normal);
        } else {
            backdrop.setColor(color);
            moduleString.setColor(color);
            slotString.setColor(color);
        }
    }

    @Override
    protected void onFocus() {
        setColors(GuiColors.hover);
    }

    @Override
    protected void onBlur() {
        setColors(color);
    }
}
