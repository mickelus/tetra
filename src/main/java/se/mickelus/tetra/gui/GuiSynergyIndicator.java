package se.mickelus.tetra.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.GuiTexture;
import se.mickelus.mutil.util.CastOptional;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.data.SynergyData;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
public class GuiSynergyIndicator extends GuiElement {
    private static final int activeCoord = 176;
    private static final int inactiveCoord = 186;
    private static final int emptyCoord = 196;

    protected List<Component> tooltip;

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
        tooltip.add(Component.translatable("tetra.modular.synergy_indicator.header").withStyle(ChatFormatting.GRAY));

        if (itemStack.getItem() instanceof IModularItem item) {
            Set<SynergyData> activeSynergies = Arrays.stream(item.getSynergyData(itemStack))
                    .collect(Collectors.toSet());

            hasActive = hasActive || Arrays.stream(item.getSynergyData(itemStack))
                    .filter(data -> shouldShow(data, activeSynergies, moduleKey, moduleVariant))
                    .anyMatch(this::providesStats);

            Arrays.stream(item.getAllSynergyData(itemStack))
                    .filter(data -> shouldShow(data, activeSynergies, moduleKey, moduleVariant))
                    .filter(this::providesStats)
                    .flatMap(data -> getLines(activeSynergies.contains(data), data).stream())
                    .map(Component::literal)
                    .collect(Collectors.toCollection(() -> tooltip));
        }

        if (tooltip.size() <= 1) {
            tooltip = Collections.singletonList(Component.translatable("tetra.modular.synergy_indicator.empty").withStyle(ChatFormatting.GRAY));
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

    private boolean shouldShow(SynergyData data, Set<SynergyData> activeSynergies, String moduleKey, String moduleVariant) {
        if (data.hidden) {
            return false;
        }
        if (data.obscured) {
            return activeSynergies.contains(data);
        }

        if (data.visibilityKey != null && !(data.visibilityKey.equals(moduleKey) || data.visibilityKey.equals(moduleVariant))) {
            return false;
        }

        return matchesModule(data, moduleKey) || matchesVariant(data, moduleVariant);
    }

    private boolean matchesModule(SynergyData data, String moduleKey) {
        return Arrays.asList(data.modules).contains(moduleKey);
    }

    private boolean matchesVariant(SynergyData data, String moduleVariant) {
        return Arrays.asList(data.moduleVariants).contains(moduleVariant);
    }

    private boolean providesStats(SynergyData data) {
        return data.attributes != null
                || data.tools != null
                || data.effects != null
                || data.durability != 0 || data.durabilityMultiplier != 1
                || data.integrity != 0 || data.integrityMultiplier != 1
                || data.magicCapacity != 0;
    }

    private List<String> getLines(boolean isActive, SynergyData data) {
        String header = getHeaderLine(isActive, data);
        if (isActive || alwaysShowStats) {
            List<String> result = getStatLines(data);
            result.add(0, header);
            return result;
        }
        return Collections.singletonList(header);
    }

    private String getHeaderLine(boolean isActive, SynergyData data) {
        return (isActive
                ? ChatFormatting.GREEN + "\u00BB " + ChatFormatting.WHITE
                : ChatFormatting.BOLD + "  " + ChatFormatting.DARK_GRAY)
                + Stream.concat(
                        Arrays.stream(data.moduleVariants)
                                .map(key -> I18n.get("tetra.variant." + key)),
                        Arrays.stream(data.modules)
                                .map(key -> I18n.get("tetra.module." + key + ".name"))
                )
                .collect(Collectors.joining(" + "));
    }

    private List<String> getStatLines(SynergyData data) {
        List<String> result = new ArrayList<>();

        if (data.attributes != null) {
            data.attributes.forEach((attribute, modifier) -> {
                double amount = modifier.getAmount();
                if (modifier.getOperation() == AttributeModifier.Operation.ADDITION) {
                    result.add(getValueDouble(amount, 0) + I18n.get(attribute.getDescriptionId()));
                } else {
                    result.add(getValueMultiplier(amount + 1) + I18n.get(attribute.getDescriptionId()));
                }
            });
        }

        if (data.durability != 0) {
            result.add(getValueInteger(data.durability, 0) + I18n.get("tetra.stats.durability"));
        }
        if (data.durabilityMultiplier != 1) {
            result.add(getValueMultiplier(data.durabilityMultiplier) + I18n.get("tetra.stats.durability"));
        }

        if (data.integrity != 0) {
            result.add(getValueInteger(data.integrity, 0) + I18n.get("tetra.stats.integrity"));
        }
        if (data.integrityMultiplier != 1) {
            result.add(getValueMultiplier(data.integrityMultiplier) + I18n.get("tetra.stats.integrity"));
        }

        if (data.effects != null) {
            data.effects.getLevelMap().forEach((itemEffect, level) ->
                    result.add(getValueInteger(level, 0) + I18n.get("tetra.stats." + itemEffect.getKey()) + " " + I18n.get("tetra.stats.level_suffix")));

            data.effects.efficiencyMap.forEach((itemEffect, efficiency) ->
                    result.add(getValueDouble(efficiency, 0) + I18n.get("tetra.stats." + itemEffect.getKey()) + " " + I18n.get("tetra.stats.strength_suffix")));
        }

        if (data.tools != null) {
            data.tools.getLevelMap().forEach((tool, level) ->
                    result.add(getValueInteger(level, 0) + I18n.get("tetra.tool." + tool.name()) + " " + I18n.get("tetra.stats.tier_suffix")));

            data.tools.efficiencyMap.forEach((tool, efficiency) ->
                    result.add(getValueDouble(efficiency, 0) + I18n.get("tetra.tool." + tool.name()) + " " + I18n.get("tetra.stats.efficiency_suffix")));
        }


        if (data.magicCapacity != 0) {
            result.add(getValueDouble(data.integrity, 0) + I18n.get("tetra.stats.magicCapacity"));
        }

        for (int i = 0; i < result.size(); i++) {
            if (i == result.size() - 1) {
                result.set(i, ChatFormatting.GRAY + "  \u2514 " + result.get(i));
            } else {
                result.set(i, ChatFormatting.GRAY + "  \u251c " + result.get(i));
            }
        }

        return result;
    }

    public String getValueInteger(int value, int flippingPoint) {
        if (value > flippingPoint) {
            return ChatFormatting.GREEN + String.format("%+d ", value) + ChatFormatting.RESET;

        } else {
            return ChatFormatting.RED + String.format("%+d ", value) + ChatFormatting.RESET;
        }
    }

    public String getValueMultiplier(double value) {
        if (value > 1) {
            return ChatFormatting.GREEN + String.format("%.02fx ", value) + ChatFormatting.RESET;

        } else {
            return ChatFormatting.RED + String.format("%.02fx ", value) + ChatFormatting.RESET;
        }
    }

    public String getValueDouble(double value, double flippingPoint) {
        if (value > flippingPoint) {
            return ChatFormatting.GREEN + String.format("%+.02f ", value) + ChatFormatting.RESET;

        } else {
            return ChatFormatting.RED + String.format("%+.02f ", value) + ChatFormatting.RESET;
        }
    }

    @Override
    public List<Component> getTooltipLines() {
        if (hasFocus()) {
            return tooltip;
        }
        return super.getTooltipLines();
    }
}
