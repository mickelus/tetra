package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.ItemStack;
import se.mickelus.mutil.gui.*;
import se.mickelus.mutil.gui.animation.AnimationChain;
import se.mickelus.mutil.gui.animation.Applier;
import se.mickelus.mutil.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.gui.*;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.RepairRegistry;
import se.mickelus.tetra.module.data.GlyphData;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;

@ParametersAreNonnullByDefault
public class GuiModuleDetails extends GuiElement {

    private final GuiElement glyph;
    private final GuiElement wrapper;
    private final GuiString title;
    private final GuiTextSmall description;
    private final GuiString emptyLabel;

    private final GuiMagicUsage magicBar;
    private final GuiSettleProgress settleBar;

    private final GuiSynergyIndicator synergyIndicator;
    private final AspectIconGui aspectIcon;

    private final RepairInfoGui repairInfo;

    private final AnimationChain flash;

    public GuiModuleDetails(int x, int y) {
        super(x, y, 224, 67);

        addChild(new GuiTexture(-4, -4, 239, 69, 0, 118, GuiTextures.workbench));

        wrapper = new GuiElement(x, y, width, height);
        addChild(wrapper);

        glyph = new GuiElement(3, 3, 16, 16);
        wrapper.addChild(glyph);

        title = new GuiString(20, 7, 106, "");
        wrapper.addChild(title);

        description = new GuiTextSmall(5, 19, 121, "");
        wrapper.addChild(description);

        emptyLabel = new GuiString(-44, -3, ChatFormatting.DARK_GRAY + I18n.get("tetra.workbench.module_detail.empty"));
        emptyLabel.setAttachment(GuiAttachment.middleCenter);
        wrapper.addChild(emptyLabel);

        synergyIndicator = new GuiSynergyIndicator(137, 10);
        wrapper.addChild(synergyIndicator);

        aspectIcon = new AspectIconGui(156, 11);
        wrapper.addChild(aspectIcon);

        repairInfo = new RepairInfoGui(173, 7);
        wrapper.addChild(repairInfo);

        magicBar = new GuiMagicUsage(138, 33, 80);
        wrapper.addChild(magicBar);

        settleBar = new GuiSettleProgress(138, 49, 80);
        wrapper.addChild(settleBar);

        GuiTexture flashOverlay = new GuiTexture(-4, -4, 239, 69, 0, 118, GuiTextures.workbench);
        flashOverlay.setColor(0);
        addChild(flashOverlay);
        flash = new AnimationChain(
                new KeyframeAnimation(40, flashOverlay).applyTo(new Applier.Opacity(0.3f)),
                new KeyframeAnimation(80, flashOverlay).applyTo(new Applier.Opacity(0)));
    }

    public void update(@Nullable ItemModule module, ItemStack itemStack) {
        glyph.clearChildren();
        if (module != null) {
            title.setString(module.getName(itemStack));
            description.setString(ChatFormatting.GRAY + module.getDescription(itemStack).replace(ChatFormatting.RESET.toString(), ChatFormatting.GRAY.toString()));

            GlyphData glyphData = module.getVariantData(itemStack).glyph;

            if (module instanceof ItemModuleMajor majorModule) {
                glyph.addChild(new GuiTexture(0, 0, 15, 15, 52, 0, GuiTextures.workbench));
                glyph.addChild(new GuiModuleGlyph(-1, 0, 16, 16, glyphData).setShift(false));

                settleBar.update(itemStack, majorModule);
            } else {
                glyph.addChild(new GuiTexture(3, 2, 11, 11, 68, 0, GuiTextures.workbench));
                glyph.addChild(new GuiModuleGlyph(5, 4, 8, 8, glyphData).setShift(false));
            }

            magicBar.update(itemStack, ItemStack.EMPTY, module.getSlot());

            synergyIndicator.update(itemStack, module);

            aspectIcon.update(itemStack, module);

            ItemStack[] repairItemStacks = RepairRegistry.instance.getDefinitions(module.getVariantData(itemStack).key).stream()
                    .map(definition -> definition.material.getApplicableItemStacks())
                    .flatMap(Arrays::stream)
                    .toArray(ItemStack[]::new);

            repairInfo.update(repairItemStacks);
        }

        synergyIndicator.setVisible(module != null);
        aspectIcon.setVisible(module != null);
        title.setVisible(module != null);
        description.setVisible(module != null);
        settleBar.setVisible(module instanceof ItemModuleMajor);
        magicBar.setVisible(module instanceof ItemModuleMajor);
        emptyLabel.setVisible(module == null);
        repairInfo.setVisible(module != null);

        flash();
    }

    public void flash() {
        this.flash.stop();
        this.flash.start();
    }
}
