package se.mickelus.tetra.gui.stats.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.gui.stats.getter.*;

import javax.annotation.Nullable;

public class StatGetterDeserializers {
    public static IStatGetter andGetter(JsonElement json) {
        AndData data = StatBarStore.gson.fromJson(json, AndData.class);
        return new StatGetterAnd(data.getters);
    }

    record AndData(IStatGetter[] getters) {
    }

    public static IStatGetter addGetter(JsonElement json) {
        AddData data = StatBarStore.gson.fromJson(json, AddData.class);
        return new StatGetterAdd(data.fixed != null ? data.fixed : 0, data.getters);
    }

    record AddData(IStatGetter[] getters, Double fixed) {
    }

    public static IStatGetter multiplyGetter(JsonElement json) {
        MultiplyData data = StatBarStore.gson.fromJson(json, MultiplyData.class);
        return new StatGetterMultiply(data.fixed != null ? data.fixed : 1, data.getters);
    }

    record MultiplyData(IStatGetter[] getters, Double fixed) {
    }

    public static IStatGetter attributeGetter(JsonElement json) {
        AttributeData data = StatBarStore.gson.fromJson(json, AttributeData.class);
        Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(data.attribute));
        if (attribute == null) {
            throw new JsonParseException("Failed to parse attribute stat getter, unknown attribute: " + data.attribute);
        }

        return new StatGetterAttribute(
                attribute,
                data.ignoreBase != null ? data.ignoreBase : false,
                data.ignoreBonuses != null ? data.ignoreBonuses : false);
    }

    record AttributeData(String attribute, @Nullable Boolean ignoreBase, @Nullable Boolean ignoreBonuses) {
    }

    public static IStatGetter effectEfficiencyGetter(JsonElement json) {
        EffectEfficiencyData data = StatBarStore.gson.fromJson(json, EffectEfficiencyData.class);
        return new StatGetterEffectEfficiency(ItemEffect.get(data.effect));
    }

    record EffectEfficiencyData(String effect) {
    }

    public static IStatGetter effectLevelGetter(JsonElement json) {
        EffectLevelData data = StatBarStore.gson.fromJson(json, EffectLevelData.class);
        return new StatGetterEffectLevel(ItemEffect.get(data.effect));
    }

    record EffectLevelData(String effect) {
    }

    public static IStatGetter enchantmentGetter(JsonElement json) {
        EnchantmentData data = StatBarStore.gson.fromJson(json, EnchantmentData.class);
        Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(data.enchantment));
        if (enchantment == null) {
            throw new JsonParseException("Failed to parse enchantment stat getter, unknown enchantment: " + data.enchantment);
        }

        return new StatGetterEnchantmentLevel(enchantment);
    }

    record EnchantmentData(String enchantment) {
    }
}
