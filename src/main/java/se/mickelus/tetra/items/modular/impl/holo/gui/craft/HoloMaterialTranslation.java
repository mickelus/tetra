package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.ToolType;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiTexture;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.module.data.EffectData;
import se.mickelus.tetra.module.data.MaterialMultiplier;
import se.mickelus.tetra.module.data.ToolData;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import java.util.*;

public class HoloMaterialTranslation extends GuiElement {
    private final List<String> emptyTooltip = Collections.singletonList(I18n.format("tetra.holo.craft.empty_translation"));
    private List<String> tooltip;

    private GuiTexture icon;

    public HoloMaterialTranslation(int x, int y) {
        super(x, y, 9, 9);

        icon = new GuiTexture(0, 0, 9, 9, 224, 0, GuiTextures.workbench);
        addChild(icon);
    }

    @Override
    public List<String> getTooltipLines() {
        if (hasFocus()) {
            return tooltip;
        }
        return null;
    }

    public void update(UpgradeSchematic schematic) {
        MaterialMultiplier translation = schematic.getMaterialTranslation();
        if (translation != null) {

            LinkedList<String> primary = new LinkedList<>();
            LinkedList<String> secondary = new LinkedList<>();
            LinkedList<String> tertiary = new LinkedList<>();

            extractAttributes(translation.primaryAttributes, primary);
            extractAttributes(translation.secondaryAttributes, secondary);
            extractAttributes(translation.tertiaryAttributes, tertiary);

            extractEffects(translation.primaryEffects, primary);
            extractEffects(translation.secondaryEffects, secondary);
            extractEffects(translation.tertiaryEffects, tertiary);

            ImmutableList.Builder<String> result = new ImmutableList.Builder<>();
            result.add(I18n.format("tetra.holo.craft.translation"));

            if (translation.durability != null || translation.integrity != null) {
                result.add(" ");
            }

            if (translation.durability != null) {
                result.add(getStatLine("tetra.stats.durability", translation.durability.intValue()));
            }

            if (translation.integrity != null) {
                result.add(getStatLine("tetra.stats.integrity", translation.integrity.intValue()));
            }

            extractTools(translation.tools, result);

            if (!primary.isEmpty()) {
                result.add(" ");
                result.add(TextFormatting.WHITE + I18n.format("tetra.holo.craft.materials.stat.primary") + ":");
                result.addAll(primary);
            }
            if (!secondary.isEmpty()) {
                result.add(" ");
                result.add(TextFormatting.WHITE + I18n.format("tetra.holo.craft.materials.stat.secondary") + ":");
                result.addAll(secondary);
            }
            if (!tertiary.isEmpty()) {
                result.add(" ");
                result.add(TextFormatting.WHITE + I18n.format("tetra.holo.craft.materials.stat.tertiary") + ":");
                result.addAll(tertiary);
            }

            tooltip = result.build();
        } else {
            tooltip = emptyTooltip;
        }
    }

    private String getStatLine(String unlocalizedStat, int value) {
        return getStatLine(unlocalizedStat, value, null);
    }

    private String getStatLine(String unlocalizedStat, int value, String unlocalizedSuffix) {
        if (I18n.hasKey(unlocalizedStat)) {
            StringBuilder line = new StringBuilder(TextFormatting.GRAY.toString());

            line.append(I18n.format(unlocalizedStat));

            if (unlocalizedSuffix != null) {
                line.append(" ");
                line.append(I18n.format(unlocalizedSuffix));
            }

            if (value < 0) {
                line.append(TextFormatting.RED);
                line.append(" -");
            } else {
                line.append(TextFormatting.GREEN);
                line.append(" +");
            }

            line.append(I18n.format("enchantment.level." + (Math.abs(value))));

            return line.toString();
        }
        return null;
    }

    private void extractAttributes(Multimap<Attribute, AttributeModifier> attributes, List<String> result) {
        if (attributes != null) {
            attributes.entries().stream()
                    .map(entry -> getStatLine(entry.getKey().getAttributeName(), (int) entry.getValue().getAmount(),
                            entry.getValue().getOperation() != AttributeModifier.Operation.ADDITION ? "tetra.attribute.multiplier" : null))
                    .filter(Objects::nonNull)
                    .map(line -> "  " + line)
                    .forEach(result::add);
        }
    }

    private void extractEffects(EffectData effects, List<String> result) {
        if (effects != null) {
            effects.getValues().stream()
                .map(effect -> extractEffectLevel(effect, effects))
                .filter(Objects::nonNull)
                .map(line -> "  " + line)
                .forEach(result::add);

            effects.getValues().stream()
                .map(effect -> extractEffectEfficiency(effect, effects))
                .filter(Objects::nonNull)
                .map(line -> "  " + line)
                .forEach(result::add);
        }
    }

    private String extractEffectLevel(ItemEffect effect, EffectData effects) {
        int level = effects.getLevel(effect);
        if (level != 0) {
            String levelKey = "tetra.stats." + effect.getKey() + ".level";
            return getStatLine(I18n.hasKey(levelKey) ? levelKey : "tetra.stats." + effect.getKey(), level);
        }

        return null;
    }

    private String extractEffectEfficiency(ItemEffect effect, EffectData effects) {
        int efficiency = (int) effects.getEfficiency(effect);
        if (efficiency != 0) {
            return getStatLine("tetra.stats." + effect.getKey() + ".efficiency", efficiency);
        }

        return null;
    }

    private void extractTools(ToolData tools, ImmutableList.Builder<String> result) {
        if (tools != null) {
            result.add("");
            tools.getValues().stream()
                    .map(tool -> extractToolLevel(tool, tools))
                    .filter(Objects::nonNull)
                    .forEach(result::add);

            tools.getValues().stream()
                    .map(tool -> extractToolEfficiency(tool, tools))
                    .filter(Objects::nonNull)
                    .forEach(result::add);
        }
    }


    private String extractToolLevel(ToolType tool, ToolData toolData) {
        int level = toolData.getLevel(tool);
        if (level != 0) {
            return getStatLine("tetra.tool." + tool.getName(), level, "tetra.stats.tier_suffix");
        }

        return null;
    }

    private String extractToolEfficiency(ToolType tool, ToolData toolData) {
        int efficiency = (int) toolData.getEfficiency(tool);
        if (efficiency != 0) {
            return getStatLine("tetra.tool." + tool.getName(), efficiency, "tetra.stats.efficiency_suffix");
        }

        return null;
    }
}
