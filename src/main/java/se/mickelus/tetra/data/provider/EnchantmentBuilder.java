package se.mickelus.tetra.data.provider;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.module.data.EnchantmentMapping;
import se.mickelus.tetra.module.data.ImprovementData;
import se.mickelus.tetra.module.improvement.DestabilizationEffect;

import java.util.*;

public class EnchantmentBuilder {
    private Enchantment enchantment;
    private float destabilizationProbability = 0.04f;
    private int destabilizationInstabilityLimit = 1;
    private boolean apply = true;

    public EnchantmentBuilder(Enchantment enchantment) {
        this.enchantment = enchantment;
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

    public String getModId() {
        return enchantment.delegate.name().getNamespace();
    }

    public String getKey() {
        return getImprovementKey();
    }

    public boolean shouldApply() {
        return apply;
    }

    public boolean canDestabilize() {
        return destabilizationProbability > 0 && enchantment.getMaxLevel() > 1;
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

        if (destabilization && apply) {
            result.improvement = getDestabilizationKey();
            result.extract = false;
            result.multiplier = -1;
        } else {
            result.improvement = getImprovementKey();
        }

        return result;
    }

    private DestabilizationEffect getDestabilizationEffect() {
        if (canDestabilize()) {
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

        for (int i = enchantment.getMinLevel(); i < enchantment.getMaxLevel() + 1; i++) {
            JsonObject data = new JsonObject();
            data.addProperty("key", getImprovementKey());
            data.addProperty("level", i);
            data.addProperty("enchantment", true);
            data.addProperty("magicCapacity", -(enchantment.getMaxEnchantability(i) + enchantment.getMinEnchantability(i)) / 2);
            result.add(data);
        }

        if (canDestabilize()) {
            for (int i = enchantment.getMinLevel(); i < enchantment.getMaxLevel(); i++) {
                JsonObject data = new JsonObject();
                data.addProperty("key", getDestabilizationKey());
                data.addProperty("level", i);
                data.addProperty("enchantment", true);
                result.add(data);
            }
        }

        return result;
    }

    public Map<String, String> getLocalizationEntries(JsonObject existingLocalizations) {
        Map<String, String> result = new LinkedHashMap<>();

        String key = enchantment.getName();

        String name = null;
        if (existingLocalizations.has(key)) {
            name = existingLocalizations.get(key).getAsString();
            result.put("tetra.improvement." + getImprovementKey() + ".name", name);
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
                result.put("tetra.improvement." + getDestabilizationKey() + ".name", "Destabilized: " + name);
                result.put("tetra.improvement." + getDestabilizationKey() + ".description", "Reduces the effect of the " + name + " enchantment");
            } else {
                result.put("tetra.improvement." + getDestabilizationKey() + ".name", "");
                result.put("tetra.improvement." + getDestabilizationKey() + ".description", "");
            }
            result.put("tetra.improvement." + getImprovementKey() + ".name", existingLocalizations.get(key).getAsString());
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
