package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.ToolType;
import se.mickelus.mgui.gui.*;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;
import se.mickelus.mgui.gui.impl.GuiHorizontalLayoutGroup;
import se.mickelus.tetra.blocks.workbench.gui.ToolRequirementGui;
import se.mickelus.tetra.gui.GuiItemRolling;
import se.mickelus.tetra.gui.GuiSynergyIndicator;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.gui.statbar.getter.LabelGetterBasic;
import se.mickelus.tetra.module.data.MaterialData;
import se.mickelus.tetra.module.data.TierData;
import se.mickelus.tetra.properties.PropertyHelper;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HoloMaterialDetailGui extends GuiElement {

    private GuiString label;

    private GuiSynergyIndicator synergyIndicator;

    private GuiString improvementsLabel;
    private GuiElement improvements;

    private GuiElement header;
    private GuiItemRolling icon;

    private HoloStatsGui stats;

    private Map<ToolType, Integer> availableToolLevels;

    private KeyframeAnimation openAnimation;
    private KeyframeAnimation showAnimation;
    private KeyframeAnimation hideAnimation;

    public HoloMaterialDetailGui(int x, int y, int width) {
        super(x, y, width, 100);

        header = new GuiHorizontalLayoutGroup(0, 5, 20, 4);
        header.setAttachmentAnchor(GuiAttachment.topCenter);
        addChild(header);

        addChild(new GuiTexture(0, 0, 29, 29, 97, 0, GuiTextures.workbench).setColor(0x222222).setAttachment(GuiAttachment.topCenter));

        icon = new GuiItemRolling(0, 6).setCountVisibility(GuiItem.CountMode.never);
        icon.setAttachment(GuiAttachment.topCenter);
        addChild(icon);

        label = new GuiStringOutline(0, 10, "");
        label.setAttachment(GuiAttachment.topCenter);
        addChild(label);

        addChild(new HoloMaterialStatGui(-40, 40, "primary", LabelGetterBasic.singleDecimalLabel, data -> data.primary).setAttachment(GuiAttachment.topCenter));
        addChild(new HoloMaterialStatGui(0, 40, "secondary", LabelGetterBasic.singleDecimalLabel, data -> data.secondary).setAttachment(GuiAttachment.topCenter));
        addChild(new HoloMaterialStatGui(40, 40, "tertiary", LabelGetterBasic.singleDecimalLabel, data -> data.tertiary).setAttachment(GuiAttachment.topCenter));
        addChild(new HoloMaterialStatGui(-20, 20, "tool_level", LabelGetterBasic.integerLabel, data -> (float) data.toolLevel).setAttachment(GuiAttachment.topCenter));
        addChild(new HoloMaterialStatGui(20, 20, "tool_efficiency", LabelGetterBasic.singleDecimalLabel, data -> data.toolEfficiency).setAttachment(GuiAttachment.topCenter));
        addChild(new HoloMaterialStatGui(0, 80, "durability", LabelGetterBasic.integerLabel, data -> data.durability).setAttachment(GuiAttachment.topCenter));
        addChild(new HoloMaterialStatGui(-20, 60, "integrity_gain", LabelGetterBasic.integerLabel, data -> data.integrityGain).setAttachment(GuiAttachment.topCenter));
        addChild(new HoloMaterialStatGui(20, 60, "magic_capacity", LabelGetterBasic.integerLabel, data -> (float) data.magicCapacity).setAttachment(GuiAttachment.topCenter));


        PlayerEntity player = Minecraft.getInstance().player;
        availableToolLevels = Stream.of(PropertyHelper.getPlayerToolLevels(player), PropertyHelper.getToolbeltToolLevels(player))
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Math::max));

        // animations
        openAnimation = new KeyframeAnimation(80, this)
                .applyTo(new Applier.Opacity(0, 1), new Applier.TranslateY(y - 4, y))
                .withDelay(120);

        showAnimation = new KeyframeAnimation(60, this)
                .applyTo(new Applier.Opacity(1), new Applier.TranslateY(y));

        hideAnimation = new KeyframeAnimation(60, this)
                .applyTo(new Applier.Opacity(0), new Applier.TranslateY(y - 5))
                .onStop(complete -> {
                    if (complete) {
                        this.isVisible = false;
                    }
                });
    }

    public void update(MaterialData selected, MaterialData hovered) {
        MaterialData current = selected != null ? selected : hovered;
        MaterialData preview = hovered != null ? hovered : current;
        if (current != null) {
            label.setString(I18n.format("tetra.material." + preview.key));
            icon.setItems(preview.material.getApplicableItemStacks());

            header.clearChildren();
            Optional.ofNullable(preview.requiredTools)
                    .map(TierData::getLevelMap)
                    .map(Map::entrySet)
                    .map(Collection::stream)
                    .orElseGet(Stream::empty)
                    .map(entry -> new ToolRequirementGui(0, 0, entry.getKey()).updateRequirement(entry.getValue(), availableToolLevels.get(entry.getKey())))
                    .forEach(header::addChild);

            getChildren(HoloMaterialStatGui.class).forEach(stat -> stat.update(current, preview));

            header.setX(label.getWidth() / 2);

            show();
        } else {
            hide();
        }
    }

    public void animateOpen() {
        stats.realignBars();
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
