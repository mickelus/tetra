package se.mickelus.tetra.data.provider;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import se.mickelus.tetra.module.data.EnchantmentMapping;
import se.mickelus.tetra.module.improvement.DestabilizationEffect;

import java.util.*;

public class EnchantmentBuilder {
    private Enchantment enchantment;
    private float destabilizationProbability = 0.04f;
    private int destabilizationInstabilityLimit = 1;

    private boolean apply = true;
    private boolean createImprovements = true;
    private boolean isCurse = false;

    private int bonusLevels = 0;
    private float capacityMultiplier = 1;

    public EnchantmentBuilder(Enchantment enchantment) {
        this.enchantment = enchantment;

        isCurse = enchantment.isCurse();
        if (isCurse) {
            destabilizationInstabilityLimit = 20;
        }

        if (enchantment.getMaxLevel() == 5) {
            bonusLevels = 10;
        } else if (enchantment.getMaxLevel() > 1) {
            bonusLevels = 5;
        }
    }

    public EnchantmentBuilder setDestabilizationProbability(float probability) {
        destabilizationProbability = probability;
        return this;
    }

    public EnchantmentBuilder setDestabilizationInstabilityLimit(int limit) {
        destabilizationInstabilityLimit = limit;
        return this;
    }

    public EnchantmentBuilder setApply(boolean apply) {
        this.apply = apply;
        return this;
    }

    public EnchantmentBuilder setCurse(boolean isCurse) {
        this.isCurse = isCurse;
        return this;
    }

    public EnchantmentBuilder setCreateImprovements(boolean createImprovements) {
        this.createImprovements = createImprovements;
        return this;
    }

    public EnchantmentBuilder setBonusLevels(int bonusLevels) {
        this.bonusLevels = bonusLevels;
        return this;
    }

    public EnchantmentBuilder setCapacityMultiplier(float capacityMultiplier) {
        this.capacityMultiplier = capacityMultiplier;
        return this;
    }

    public String getModId() {
        try {
            return enchantment.delegate.name().getNamespace();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getKey() {
        return getImprovementKey();
    }

    public EnchantmentCategory getEnchantmentType() {
        return enchantment.category;
    }

    public boolean shouldApply() {
        return apply;
    }

    public boolean canDestabilize() {
        return destabilizationProbability > 0 && enchantment.getMaxLevel() > 1 && !isCurse;
    }

    public boolean isDestabilizingCurse() {
        return destabilizationProbability > 0 && isCurse;
    }

    public boolean shouldCreateImprovements() {
        return createImprovements;
    }

    private String getImprovementKey() {
        ResourceLocation location = enchantment.delegate.name();
        if (location.getNamespace().equals("minecraft")) {
            return "enchantment/" + location.getPath();
        }

        return "enchantment/" + location.getNamespace() + "_" + location.getPath();
    }

    private String getDestabilizationKey() {
        ResourceLocation location = enchantment.delegate.name();
        if (location.getNamespace().equals("minecraft")) {
            return "destabilized/" + location.getPath();
        }

        return "destabilized/" + location.getNamespace() + "_" + location.getPath();
    }

    private EnchantmentMapping getEnchantmentMapping(boolean destabilization) {
        EnchantmentMapping result = new EnchantmentMapping();
        result.enchantment = enchantment;
        result.apply = apply;

        if (destabilization) {
            result.improvement = getDestabilizationKey();
            result.extract = false;
            result.multiplier = -1;
        } else {
            result.improvement = getImprovementKey();
        }

        return result;
    }

    private DestabilizationEffect getDestabilizationEffect() {
        if (isCurse) {
            DestabilizationEffect result = new DestabilizationEffect();
            result.minLevel = enchantment.getMinLevel();
            result.maxLevel = enchantment.getMaxLevel();
            result.probability = destabilizationProbability;
            result.instabilityLimit = destabilizationInstabilityLimit;

            result.destabilizationKey = getImprovementKey();

            ResourceLocation location = enchantment.delegate.name();
            if (!location.getNamespace().equals("minecraft")) {
                result.requiredMod = location.getNamespace();
            }

            return result;
        } if (canDestabilize()) {
            DestabilizationEffect result = new DestabilizationEffect();
            result.minLevel = enchantment.getMinLevel();
            result.maxLevel = enchantment.getMaxLevel() - 1;
            result.probability = destabilizationProbability;
            result.instabilityLimit = destabilizationInstabilityLimit;

            result.improvementKey = getImprovementKey();
            result.destabilizationKey = getDestabilizationKey();

            ResourceLocation location = enchantment.delegate.name();
            if (!location.getNamespace().equals("minecraft")) {
                result.requiredMod = location.getNamespace();
            }

            return result;
        }

        return null;
    }

    public JsonArray getImprovementsJson() {
        JsonArray result = new JsonArray();

        for (int i = enchantment.getMinLevel(); i < enchantment.getMaxLevel() + 1 + bonusLevels; i++) {
            JsonObject data = new JsonObject();
            data.addProperty("key", getImprovementKey());
            data.addProperty("level", i);
            data.addProperty("enchantment", true);

            if (isCurse) {
                JsonObject glyph = new JsonObject();
                glyph.addProperty("tint", "ee5599");
                data.add("glyph", glyph);
            } else {
                data.addProperty("magicCapacity",
                        -(enchantment.getMaxCost(i) + enchantment.getMinCost(i)) / 2 * capacityMultiplier);
            }

            result.add(data);
        }

        if (canDestabilize()) {
            for (int i = enchantment.getMinLevel(); i < enchantment.getMaxLevel(); i++) {
                JsonObject data = new JsonObject();
                data.addProperty("key", getDestabilizationKey());
                data.addProperty("level", i);
                data.addProperty("enchantment", true);

                JsonObject glyph = new JsonObject();
                glyph.addProperty("tint", "ee5599");
                data.add("glyph", glyph);

                result.add(data);
            }
        }

        return result;
    }

    public Map<String, String> getLocalizationEntries(JsonObject existingLocalizations) {
        Map<String, String> result = new LinkedHashMap<>();

        String key = enchantment.getDescriptionId();

        String name = null;
        if (existingLocalizations.has(key)) {
            name = existingLocalizations.get(key).getAsString();
            if (isCurse) {
                result.put("tetra.improvement." + getImprovementKey() + ".name", ChatFormatting.DARK_PURPLE + name);
            } else {
                result.put("tetra.improvement." + getImprovementKey() + ".name", name);
            }
        }

        if (existingLocalizations.has(key + ".desc")) {
            result.put("tetra.improvement." + getImprovementKey() + ".description", existingLocalizations.get(key + ".desc").getAsString());
        } else if (existingLocalizations.has(key + ".description")) {
            result.put("tetra.improvement." + getImprovementKey() + ".description", existingLocalizations.get(key + ".description").getAsString());
        } else {
            result.put("tetra.improvement." + getImprovementKey() + ".description", "");
        }

        if (canDestabilize()) {
            if (name != null) {
                result.put("tetra.improvement." + getDestabilizationKey() + ".name", ChatFormatting.DARK_PURPLE + "Destabilized: " + name);
                result.put("tetra.improvement." + getDestabilizationKey() + ".description", "Reduces the effect of the " + name + " enchantment");
            } else {
                result.put("tetra.improvement." + getDestabilizationKey() + ".name", "");
                result.put("tetra.improvement." + getDestabilizationKey() + ".description", "");
            }
        }

        return result;
    }

    public Collection<String> getLocalizationKeys() {
        LinkedList<String> result = new LinkedList<>();

        String key = enchantment.getDescriptionId();

        String name = null;
        if (isCurse) {
            result.add("tetra.improvement." + getImprovementKey() + ".name");
        } else {
            result.add("tetra.improvement." + getImprovementKey() + ".name");
        }

        result.add("tetra.improvement." + getImprovementKey() + ".description");


        if (canDestabilize()) {
            result.add("tetra.improvement." + getDestabilizationKey() + ".name");
            result.add("tetra.improvement." + getDestabilizationKey() + ".description");
        }

        return result;
    }

    public JsonObject getEnchantmentJson() {
        return getEnchantmentMapping(false).toJson();
    }

    public JsonObject getEnchantmentDestabilizationJson() {
        return Optional.ofNullable(getEnchantmentMapping(true))
                .map(EnchantmentMapping::toJson)
                .orElse(null);
    }

    public JsonObject getDestabilizationJson() {
        return Optional.ofNullable(getDestabilizationEffect())
                .map(DestabilizationEffect::toJson)
                .orElse(null);
    }
}
