package se.mickelus.tetra.blocks.workbench.gui;

import com.google.common.collect.ImmutableList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ItemAbility;
import se.mickelus.mutil.gui.*;
import se.mickelus.mutil.gui.animation.AnimationChain;
import se.mickelus.mutil.gui.animation.Applier;
import se.mickelus.mutil.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.blocks.workbench.WorkbenchContainer;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiMagicUsage;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.module.data.GlyphData;
import se.mickelus.tetra.module.schematic.SchematicType;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Map;

@ParametersAreNonnullByDefault
public class GuiSchematicDetail extends GuiElement {


    private final GuiElement glyph;
    private final GuiString title;
    private final GuiSources sources;
    private final GuiTextSmall description;
    private final CraftButtonGui craftButton;
    private final SchemaSlotGui[] slots;
    private final GuiElement emptySlotsIndicator;
    private final GuiElement hasSlotsIndicator;
    private final GuiMagicUsage magicCapacity;
    private final ToolRequirementListGui toolRequirementList;
    private final GuiExperience experienceIndicator;
    private final AnimationChain flash;
    private UpgradeSchematic schematic;
    private List<Component> descriptionTooltip;

    public GuiSchematicDetail(int x, int y, Runnable backListener, Runnable craftListener) {
        super(x, y, 224, 67);

        addChild(new GuiTexture(-4, -4, 239, 69, 0, 187, GuiTextures.workbench));

        addChild(new GuiButton(-4, height - 2, 40, 8, "< " + I18n.get("tetra.workbench.schematic_detail.back"), backListener));

        glyph = new GuiElement(3, 3, 16, 16);
        addChild(glyph);

        title = new GuiString(19, 6, 100, "");
        addChild(title);

        sources = new GuiSources(19, 15, 81);
        addChild(sources);

        description = new GuiTextSmall(5, 22, 125, "");
        addChild(description);

        slots = new SchemaSlotGui[WorkbenchTile.maxMaterialSlots];
        for (int i = 0; i < WorkbenchTile.maxMaterialSlots; i++) {
            slots[i] = new SchemaSlotGui(125, 5, 82, i);
            addChild(slots[i]);
        }

        emptySlotsIndicator = new GuiTexture(146, 6, 64, 16, 48, 32, GuiTextures.workbench);
        addChild(emptySlotsIndicator);

        hasSlotsIndicator = new GuiElement(0, 0, 0, 0);
        hasSlotsIndicator.addChild(new GuiTexture(132, 3, 4, 22, 240, 192, GuiTextures.workbench));
        hasSlotsIndicator.addChild(new GuiTexture(220, 3, 5, 22, 244, 192, GuiTextures.workbench));
        addChild(hasSlotsIndicator);

        magicCapacity = new GuiMagicUsage(138, 30, 80);
        addChild(magicCapacity);

        experienceIndicator = new GuiExperience(205, 41, "tetra.workbench.schematic_detail.experience");
        addChild(experienceIndicator);

        craftButton = new CraftButtonGui(155, 41, craftListener);
        addChild(craftButton);

        toolRequirementList = new ToolRequirementListGui(143, 40);
        addChild(toolRequirementList);

        GuiTexture flashOverlay = new GuiTexture(-4, -4, 239, 69, 0, 187, GuiTextures.workbench);
        flashOverlay.setOpacity(0);
        flashOverlay.setColor(0);
        addChild(flashOverlay);
        flash = new AnimationChain(
                new KeyframeAnimation(60, flashOverlay).applyTo(new Applier.Opacity(0.3f)),
                new KeyframeAnimation(120, flashOverlay).applyTo(new Applier.Opacity(0)));
    }

    public void update(Level level, BlockPos pos, WorkbenchTile blockEntity, UpgradeSchematic schematic, ItemStack itemStack, String slot, ItemStack[] materials, Map<ItemAbility, Integer> availableTools,
            Player player) {
        this.schematic = schematic;

        title.setString(schematic.getName());
        title.setColor(schematic.getRarity().tint);

        sources.update(schematic);

        String descriptionString = schematic.getDescription(itemStack);
        description.setString(ChatFormatting.GRAY + descriptionString
                .replace(ChatFormatting.RESET.toString(), ChatFormatting.RESET.toString() + ChatFormatting.GRAY));
        descriptionTooltip = ImmutableList.of(Component.literal(descriptionString));

        glyph.clearChildren();
        GlyphData glyphData = schematic.getGlyph();
        GuiTexture border = null;
        GuiTexture glyphTexture;
        if (schematic.getType() == SchematicType.major) {
            border = new GuiTexture(0, 2, 16, 9, 52, 3, GuiTextures.workbench);
            glyphTexture = new GuiTexture(-1, -1, 16, 16, glyphData.textureX, glyphData.textureY, glyphData.textureLocation);
        } else if (schematic.getType() == SchematicType.minor) {
            border = new GuiTexture(2, 1, 11, 11, 68, 0, GuiTextures.workbench);
            glyphTexture = new GuiTexture(4, 3, 8, 8, glyphData.textureX, glyphData.textureY, glyphData.textureLocation);
        } else if (schematic.getType() == SchematicType.improvement) {
            border = new GuiTexture(0, 2, 16, 9, 52, 3, GuiTextures.workbench);
            glyphTexture = new GuiTexture(-1, -1, 16, 16, glyphData.textureX, glyphData.textureY, glyphData.textureLocation);
        } else {
            glyphTexture = new GuiTexture(-1, -1, 16, 16, glyphData.textureX, glyphData.textureY, glyphData.textureLocation);
        }

        if (border != null) {
            border.setOpacity(0.3f);
            border.setColor(schematic.getRarity().tint);
            glyph.addChild(border);
        }
        glyphTexture.setColor(schematic.getRarity().tint);
        glyph.addChild(glyphTexture);

        if (schematic.getType() == SchematicType.improvement) {
            glyph.addChild(new GuiTexture(7, 7, 7, 7, 68, 16, GuiTextures.workbench).setColor(GuiColors.muted));
        }


        int numMaterialSlots = schematic.getNumMaterialSlots();
        for (int i = 0; i < WorkbenchTile.maxMaterialSlots; i++) {
            slots[i].update(schematic, player, level, pos, blockEntity, itemStack, slot, materials);
            slots[i].setX(WorkbenchContainer.getMaterialSlotGuiX(i, numMaterialSlots));
        }
        toolRequirementList.update(schematic, itemStack, slot, materials, availableTools);
        emptySlotsIndicator.setVisible(numMaterialSlots == 0);
        hasSlotsIndicator.setVisible(numMaterialSlots != 0);

        int xpCost = schematic.getExperienceCost(itemStack, materials, slot);
        experienceIndicator.setVisible(xpCost > 0);
        if (xpCost > 0) {
            if (!player.isCreative()) {
                experienceIndicator.update(xpCost, xpCost <= player.experienceLevel);
            } else {
                experienceIndicator.update(xpCost, true);
            }
        }

        flash();
    }

    public void updateMagicCapacity(UpgradeSchematic schematic, String slot, ItemStack itemStack, ItemStack previewStack) {
        if (slot != null && (schematic != null && SchematicType.major.equals(schematic.getType()) && magicCapacity.providesCapacity(itemStack, previewStack, slot)
                || magicCapacity.hasChanged(itemStack, previewStack, slot))) {
            magicCapacity.update(itemStack, previewStack, slot);
            magicCapacity.setVisible(true);
        } else {
            magicCapacity.setVisible(false);
        }
    }

    public void updateAvailableTools(Map<ItemAbility, Integer> availableTools) {
        toolRequirementList.updateAvailableTools(availableTools);
    }

    public void updateButton(UpgradeSchematic schematic, Player player, ItemStack itemStack, ItemStack previewStack, ItemStack[] materials, String slot,
            Map<ItemAbility, Integer> availableTools) {
        craftButton.update(schematic, player, itemStack, previewStack, materials, slot, availableTools);
    }

    @Override
    public List<Component> getTooltipLines() {
        if (description.hasFocus()) {
            return descriptionTooltip;
        }

        return super.getTooltipLines();
    }

    public void flash() {
        this.flash.stop();
        this.flash.start();
    }
}
