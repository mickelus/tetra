package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.ToolAction;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.GuiTexture;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.module.data.EffectData;
import se.mickelus.tetra.module.data.MaterialMultiplier;
import se.mickelus.tetra.module.data.ToolData;
import se.mickelus.tetra.module.schematic.SchematicType;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
public class HoloMaterialTranslation extends GuiElement {
    private final List<Component> emptyTooltipImprovement;
    private final List<Component> emptyTooltip;
    private final GuiTexture icon;
    private List<Component> tooltip;

    public HoloMaterialTranslation(int x, int y) {
        super(x, y, 9, 9);

        icon = new GuiTexture(0, 0, 9, 9, 224, 0, GuiTextures.workbench);
        addChild(icon);

        emptyTooltipImprovement = Collections.singletonList(new TranslatableComponent("tetra.holo.craft.empty_translation_improvement"));
        emptyTooltip = Collections.singletonList(new TranslatableComponent("tetra.holo.craft.empty_translation_module"));
    }

    @Override
    public List<Component> getTooltipLines() {
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

            List<String> result = new LinkedList<>();

            if (schematic.getType() == SchematicType.improvement) {
                result.add(I18n.get("tetra.holo.craft.translation_improvement"));
            } else {
                result.add(I18n.get("tetra.holo.craft.translation_module"));
            }

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
                result.add(ChatFormatting.WHITE + I18n.get("tetra.holo.craft.materials.stat.primary") + ":");
                result.addAll(primary);
            }
            if (!secondary.isEmpty()) {
                result.add(" ");
                result.add(ChatFormatting.WHITE + I18n.get("tetra.holo.craft.materials.stat.secondary") + ":");
                result.addAll(secondary);
            }
            if (!tertiary.isEmpty()) {
                result.add(" ");
                result.add(ChatFormatting.WHITE + I18n.get("tetra.holo.craft.materials.stat.tertiary") + ":");
                result.addAll(tertiary);
            }

            tooltip = result.stream()
                    .map(TextComponent::new)
                    .collect(Collectors.toList());
        } else {
            if (schematic.getType() == SchematicType.improvement) {
                tooltip = emptyTooltipImprovement;
            } else {
                tooltip = emptyTooltip;
            }
        }
    }

    private String getStatLine(String unlocalizedStat, int value) {
        return getStatLine(unlocalizedStat, value, null);
    }

    private String getStatLine(String unlocalizedStat, int value, @Nullable String unlocalizedSuffix) {
        if (I18n.exists(unlocalizedStat)) {
            StringBuilder line = new StringBuilder(ChatFormatting.GRAY.toString());

            line.append(I18n.get(unlocalizedStat));

            if (unlocalizedSuffix != null) {
                line.append(" ");
                line.append(I18n.get(unlocalizedSuffix));
            }

            if (value < 0) {
                line.append(ChatFormatting.RED);
                line.append(" -");
            } else {
                line.append(ChatFormatting.GREEN);
                line.append(" +");
            }

            line.append(I18n.get("enchantment.level." + (Math.abs(value))));

            return line.toString();
        }
        return null;
    }

    private void extractAttributes(Multimap<Attribute, AttributeModifier> attributes, List<String> result) {
        if (attributes != null) {
            attributes.entries().stream()
                    .map(entry -> getStatLine(entry.getKey().getDescriptionId(), (int) entry.getValue().getAmount(),
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
            return getStatLine(I18n.exists(levelKey) ? levelKey : "tetra.stats." + effect.getKey(), level);
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

    private void extractTools(ToolData tools, List<String> result) {
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


    private String extractToolLevel(ToolAction tool, ToolData toolData) {
        int level = toolData.getLevel(tool);
        if (level != 0) {
            return getStatLine("tetra.tool." + tool.name(), level, "tetra.stats.tier_suffix");
        }

        return null;
    }

    private String extractToolEfficiency(ToolAction tool, ToolData toolData) {
        int efficiency = (int) toolData.getEfficiency(tool);
        if (efficiency != 0) {
            return getStatLine("tetra.tool." + tool.name(), efficiency, "tetra.stats.efficiency_suffix");
        }

        return null;
    }
}
