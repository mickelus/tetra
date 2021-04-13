package se.mickelus.tetra.gui;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiTexture;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.data.SynergyData;
import se.mickelus.tetra.util.CastOptional;

import java.util.*;
import java.util.stream.Collectors;

public class GuiSynergyIndicator extends GuiElement {
    private static final int activeCoord = 176;
    private static final int inactiveCoord = 186;
    private static final int emptyCoord = 196;

    protected List<String> tooltip;

    protected GuiTexture indicator;

    protected boolean alwaysShowStats;

    public GuiSynergyIndicator(int x, int y) {
        super(x, y, 10, 10);

        indicator = new GuiTexture(0, 0, width, height, 176, 0, GuiTextures.workbench);
        addChild(indicator);
    }

    public GuiSynergyIndicator(int x, int y, boolean alwaysShowStats) {
        this(x, y);

        this.alwaysShowStats = alwaysShowStats;
    }

    public void update(ItemStack itemStack, ItemModule module) {
        String moduleKey = module.getUnlocalizedName();
        String moduleVariant = module.getVariantData(itemStack).key;

        boolean hasActive = alwaysShowStats;

        tooltip = new ArrayList<>();
        tooltip.add(TextFormatting.GRAY + I18n.format("item.tetra.modular.synergy_indicator.header"));

        if (itemStack.getItem() instanceof IModularItem) {
            IModularItem item = (IModularItem) itemStack.getItem();

            Set<SynergyData> activeSynergies = Arrays.stream(item.getSynergyData(itemStack))
                    .collect(Collectors.toSet());

            hasActive = hasActive || Arrays.stream(item.getSynergyData(itemStack))
                    .filter(data -> Arrays.asList(data.modules).contains(moduleKey) || Arrays.asList(data.moduleVariants).contains(moduleVariant))
                    .anyMatch(this::providesStats);

            Arrays.stream(item.getAllSynergyData(itemStack))
                    .filter(data -> Arrays.asList(data.modules).contains(moduleKey))
                    .filter(this::providesStats)
                    .flatMap(data -> getModuleLines(activeSynergies.contains(data), data).stream())
                    .collect(Collectors.toCollection(() -> tooltip));

            Arrays.stream(item.getAllSynergyData(itemStack))
                    .filter(data -> Arrays.asList(data.moduleVariants).contains(moduleVariant))
                    .filter(this::providesStats)
                    .flatMap(data -> getVariantLines(activeSynergies.contains(data), data).stream())
                    .collect(Collectors.toCollection(() -> tooltip));
        }


        if (tooltip.size() <= 1) {
            tooltip = Collections.singletonList(TextFormatting.GRAY + I18n.format("item.tetra.modular.synergy_indicator.empty"));
            indicator.setTextureCoordinates(emptyCoord, 0);
        } else if (!hasActive) {
            indicator.setTextureCoordinates(inactiveCoord, 0);
        } else {
            indicator.setTextureCoordinates(activeCoord, 0);
        }
    }

    public void update(ItemStack itemStack, String slot) {
        CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .map(item -> item.getModuleFromSlot(itemStack, slot))
                .ifPresent(module -> update(itemStack, module));
    }

    private boolean providesStats(SynergyData data) {
        return data.attributes != null
                || data.tools != null
                || data.effects != null
                || data.durability != 0 || data.durabilityMultiplier != 1
                || data.integrity != 0 || data.integrityMultiplier != 1
                || data.magicCapacity != 0;
    }

    private List<String> getVariantLines(boolean isActive, SynergyData data) {
        String header = Arrays.stream(data.moduleVariants)
                .map(key -> I18n.format("tetra.variant." + key))
                .collect(Collectors.joining(" + "));

        if (isActive || alwaysShowStats) {
            List<String> result = getDataLines(data);
            result.add(0, TextFormatting.GREEN + "\u00BB " + TextFormatting.WHITE + header);
            return result;
        }
        return Collections.singletonList(TextFormatting.BOLD + "  " + TextFormatting.DARK_GRAY + header);
    }

    private List<String> getModuleLines(boolean isActive, SynergyData data) {
        String header = Arrays.stream(data.modules)
                .map(key -> I18n.format("tetra.module." + key + ".name"))
                .collect(Collectors.joining(" + "));

        if (data.sameVariant) {
            header += " " + TextFormatting.DARK_GRAY + I18n.format("item.tetra.modular.synergy_indicator.variant_same");
        }

        if (isActive || alwaysShowStats) {
            List<String> result = getDataLines(data);
            result.add(0, TextFormatting.GREEN + "\u00BB " + TextFormatting.WHITE + header);
            return result;
        }
        return Collections.singletonList(TextFormatting.BOLD + "  " + TextFormatting.DARK_GRAY + header);
    }

    private List<String> getDataLines(SynergyData data) {
        List<String> result = new ArrayList<>();

        if (data.attributes != null) {
            data.attributes.forEach((attribute, modifier) -> {
                double amount = modifier.getAmount();
                if (modifier.getOperation() == AttributeModifier.Operation.ADDITION) {
                    result.add(getValueDouble(amount, 0) + I18n.format(attribute.getAttributeName()));
                } else {
                    result.add(getValueMultiplier(amount + 1) + I18n.format(attribute.getAttributeName()));
                }
            });
        }

        if (data.durability != 0) {
            result.add(getValueInteger(data.durability, 0) + I18n.format("tetra.stats.durability"));
        }
        if (data.durabilityMultiplier != 1) {
            result.add(getValueMultiplier(data.durabilityMultiplier) + I18n.format("tetra.stats.durability"));
        }

        if (data.integrity != 0) {
            result.add(getValueInteger(data.integrity, 0) + I18n.format("tetra.stats.integrity"));
        }
        if (data.integrityMultiplier != 1) {
            result.add(getValueMultiplier(data.integrityMultiplier) + I18n.format("tetra.stats.integrity"));
        }

        if (data.effects != null) {
            data.effects.getLevelMap().forEach((itemEffect, level) ->
                    result.add(getValueInteger(level, 0) + I18n.format("tetra.stats." + itemEffect.getKey()) + " " + I18n.format("tetra.stats.level_suffix")));

            data.effects.efficiencyMap.forEach((itemEffect, efficiency) ->
                    result.add(getValueDouble(efficiency, 0) + I18n.format("tetra.stats." + itemEffect.getKey()) + " " + I18n.format("tetra.stats.strength_suffix")));
        }

        if (data.tools != null) {
            data.tools.getLevelMap().forEach((tool, level) ->
                    result.add(getValueInteger(level, 0) + I18n.format("tetra.tool." + tool.getName()) + " " + I18n.format("tetra.stats.tier_suffix")));

            data.tools.efficiencyMap.forEach((tool, efficiency) ->
                    result.add(getValueDouble(efficiency, 0) + I18n.format("tetra.tool." + tool.getName()) + " " + I18n.format("tetra.stats.efficiency_suffix")));
        }


        if (data.magicCapacity != 0) {
            result.add(getValueDouble(data.integrity, 0) + I18n.format("tetra.stats.magicCapacity"));
        }

        for (int i = 0; i < result.size(); i++) {
            if (i == result.size() - 1) {
                result.set(i, TextFormatting.GRAY + "  \u2514 " + result.get(i));
            } else {
                result.set(i, TextFormatting.GRAY + "  \u251c " + result.get(i));
            }
        }

        return result;
    }

    public String getValueInteger(int value, int flippingPoint) {
        if (value > flippingPoint) {
            return TextFormatting.GREEN + String.format("%+d ", value) + TextFormatting.RESET;

        } else {
            return TextFormatting.RED + String.format("%+d ", value) + TextFormatting.RESET;
        }
    }

    public String getValueMultiplier(double value) {
        if (value > 1) {
            return TextFormatting.GREEN + String.format("%.02fx ", value) + TextFormatting.RESET;

        } else {
            return TextFormatting.RED + String.format("%.02fx ", value) + TextFormatting.RESET;
        }
    }

    public String getValueDouble(double value, double flippingPoint) {
        if (value > flippingPoint) {
            return TextFormatting.GREEN + String.format("%+.02f ", value) + TextFormatting.RESET;

        } else {
            return TextFormatting.RED + String.format("%+.02f ", value) + TextFormatting.RESET;
        }
    }

    @Override
    public List<String> getTooltipLines() {
        if (hasFocus()) {
            return tooltip;
        }
        return super.getTooltipLines();
    }
}
