package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import se.mickelus.mutil.gui.*;
import se.mickelus.mutil.gui.animation.Applier;
import se.mickelus.mutil.gui.animation.KeyframeAnimation;
import se.mickelus.mutil.gui.impl.GuiHorizontalLayoutGroup;
import se.mickelus.tetra.blocks.workbench.gui.ToolRequirementGui;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.gui.GuiItemRolling;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.gui.stats.getter.LabelGetterBasic;
import se.mickelus.tetra.module.data.MaterialData;
import se.mickelus.tetra.module.data.TierData;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
@ParametersAreNonnullByDefault
public class HoloMaterialDetailGui extends GuiElement {

    private GuiElement content;

    private GuiString label;
    private GuiTexture labelHighlight;
    private GuiItemRolling icon;

    private GuiElement modifiers;

    private GuiElement requiredTools;

    private KeyframeAnimation openAnimation;
    private KeyframeAnimation showAnimation;
    private KeyframeAnimation hideAnimation;

    private boolean hasSelected = false;

    public HoloMaterialDetailGui(int x, int y, int width) {
        super(x, y, width, 100);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                addChild(new HoloCrossGui(74 + i * 40 + j % 2 * 20, j * 20 + 32, (int) (Math.random() * 800) + i * 1000, 0.3f - i * 0.08f).setAttachment(GuiAttachment.topCenter));
                addChild(new HoloCrossGui(-(74 + i * 40 + j % 2 * 20), j * 20 + 32, (int) (Math.random() * 800) + i * 1000, 0.3f - i * 0.08f).setAttachment(GuiAttachment.topCenter));
            }
        }

        content = new GuiElement(0, 0, width, 100);
        addChild(content);

        requiredTools = new GuiHorizontalLayoutGroup(0, 70, 20, 4);
        requiredTools.setAttachment(GuiAttachment.topCenter);
        content.addChild(requiredTools);

        content.addChild(new HoloMaterialStatGui(30, 20, "primary", LabelGetterBasic.singleDecimalLabel, data -> data.primary).setAttachment(GuiAttachment.topCenter));
        content.addChild(new HoloMaterialStatGui(50, 40, "secondary", LabelGetterBasic.singleDecimalLabel, data -> data.secondary).setAttachment(GuiAttachment.topCenter));
        content.addChild(new HoloMaterialStatGui(30, 60, "tertiary", LabelGetterBasic.singleDecimalLabel, data -> data.tertiary).setAttachment(GuiAttachment.topCenter));

        content.addChild(new HoloMaterialStatGui(70, 20, "tool_level", LabelGetterBasic.integerLabel, data -> (float) data.toolLevel).setAttachment(GuiAttachment.topCenter));
        content.addChild(new HoloMaterialStatGui(70, 60, "tool_efficiency", LabelGetterBasic.singleDecimalLabel, data -> data.toolEfficiency).setAttachment(GuiAttachment.topCenter));

        content.addChild(new HoloMaterialStatGui(-30, 20, "durability", LabelGetterBasic.integerLabel, data -> data.durability).setAttachment(GuiAttachment.topCenter));
        content.addChild(new HoloMaterialIntegrityStatGui(-50, 40).setAttachment(GuiAttachment.topCenter));
        content.addChild(new HoloMaterialStatGui(-30, 60, "magic_capacity", LabelGetterBasic.integerLabel, data -> (float) data.magicCapacity).setAttachment(GuiAttachment.topCenter));

        content.addChild(new GuiTexture(0, 40, 29, 29, 97, 0, GuiTextures.workbench).setColor(0x222222).setAttachment(GuiAttachment.topCenter));
        labelHighlight = new GuiTexture(0, 44, 3, 21, 110, 4, GuiTextures.workbench);
        labelHighlight.setOpacity(0.7f);
        labelHighlight.setAttachment(GuiAttachment.topCenter);
        content.addChild(labelHighlight);

        icon = new GuiItemRolling(0, 46).setCountVisibility(GuiItem.CountMode.never);
        icon.setAttachment(GuiAttachment.topCenter);
        content.addChild(icon);

        label = new GuiStringOutline(0, 50,"");
        label.setAttachment(GuiAttachment.topCenter);
        content.addChild(label);

        modifiers = new GuiElement(0, 0, 0, 0);
        modifiers.setAttachment(GuiAttachment.topCenter);
        content.addChild(modifiers);

        // animations
        openAnimation = new KeyframeAnimation(80, this)
                .applyTo(new Applier.Opacity(0, 1), new Applier.TranslateY(y - 4, y))
                .withDelay(120);

        showAnimation = new KeyframeAnimation(60, content)
                .applyTo(new Applier.Opacity(1), new Applier.TranslateY(0));

        hideAnimation = new KeyframeAnimation(60, content)
                .applyTo(new Applier.Opacity(0), new Applier.TranslateY(-5))
                .onStop(complete -> {
                    if (complete) {
                        this.isVisible = false;
                    }
                });
    }

    public void update(MaterialData selected, MaterialData hovered) {
        MaterialData current = selected != null ? selected : hovered;
        MaterialData preview = hovered != null ? hovered : current;

        if (hasSelected == (selected == null)) {
            if (selected != null) {
                getChildren(HoloCrossGui.class).forEach(HoloCrossGui::animateOpen);
            } else {
                getChildren(HoloCrossGui.class).forEach(element -> {
                    element.stopAnimations();
                    element.setOpacity(0);
                });
            }
        }

        hasSelected = selected != null;

        if (current != null) {
            String labelString = I18n.exists("tetra.material." + preview.key) ? I18n.get("tetra.material." + preview.key) : preview.key;
            label.setString(Minecraft.getInstance().font.plainSubstrByWidth(labelString, 70));
            labelHighlight.setColor(preview.tints.glyph);
            icon.setItems(preview.material.getApplicableItemStacks());

            requiredTools.clearChildren();
            Optional.ofNullable(preview.requiredTools)
                    .map(TierData::getLevelMap)
                    .map(Map::entrySet)
                    .map(Collection::stream)
                    .orElseGet(Stream::empty)
                    .map(entry -> new ToolRequirementGui(0, 0, entry.getKey(), "tetra.tool." + entry.getKey().name() + ".material_requirement")
                            .setTooltipRequirementVisibility(false)
                            .updateRequirement(entry.getValue(), 0))
                    .forEach(requiredTools::addChild);

            content.getChildren(HoloMaterialStatGui.class).forEach(stat -> stat.update(current, preview));

            modifiers.clearChildren();
            Set<ItemEffect> currentEffects = current.effects.getValues();
            Set<ItemEffect> previewEffects = preview.effects.getValues();
            Stream.concat(currentEffects.stream(), previewEffects.stream())
                    .distinct()
                    .map(effect -> new HoloMaterialEffectGui(0, 0, effect.getKey(), currentEffects.contains(effect), previewEffects.contains(effect)))
                    .forEach(modifiers::addChild);

            Set<String> currentImprovements = current.improvements.keySet();
            Set<String> previewImprovements = preview.improvements.keySet();
            Stream.concat(currentImprovements.stream(), previewImprovements.stream())
                    .distinct()
                    .map(improvement -> new HoloMaterialImprovementGui(0, 0, improvement, currentImprovements.contains(improvement), previewImprovements.contains(improvement)))
                    .forEach(modifiers::addChild);

            for (int i = 0; i < modifiers.getNumChildren(); i++) {
                GuiElement element = modifiers.getChild(i);
                int offset = (i > 0) ? i + 3 : i + 1;
                int y = (offset % 3);
                element.setX(60 + (y % 2) * 20 + offset / 3 * 40);
                element.setY(20 + y * 20);
            }

            show();
        } else {
            hide();
        }
    }

    public void animateOpen() {
        openAnimation.start();
    }

    public void show() {
        hideAnimation.stop();
        setVisible(true);
        showAnimation.start();
    }

    public void hide() {
        showAnimation.stop();
        hideAnimation.start();
    }
}
