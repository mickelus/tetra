package se.mickelus.tetra.gui.stats.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.gui.stats.getter.*;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class StatGetterDeserializers {
    public static IStatGetter andGetter(JsonElement json) {
        AndData data = StatRegistry.gson.fromJson(json, AndData.class);
        return new StatGetterAnd(data.stats);
    }

    record AndData(IStatGetter[] stats) {
    }

    public static IStatGetter orGetter(JsonElement json) {
        OrData data = StatRegistry.gson.fromJson(json, OrData.class);
        return new StatGetterOr(data.stats);
    }

    record OrData(IStatGetter[] stats) {
    }

    public static IStatGetter sumGetter(JsonElement json) {
        SumData data = StatRegistry.gson.fromJson(json, SumData.class);
        return new StatGetterAdd(data.offset != null ? data.offset : 0, data.stats);
    }

    record SumData(IStatGetter[] stats, Double offset) {
    }

    public static IStatGetter multiplyGetter(JsonElement json) {
        MultiplyData data = StatRegistry.gson.fromJson(json, MultiplyData.class);
        return new StatGetterMultiply(data.factor != null ? data.factor : 1, data.stats);
    }

    record MultiplyData(IStatGetter[] stats, Double factor) {
    }

    public static IStatGetter clampGetter(JsonElement json) {
        ClampData data = StatRegistry.gson.fromJson(json, ClampData.class);
        return new StatGetterClamp(data.stat, data.min, data.max);
    }

    record ClampData(IStatGetter stat, Double min, Double max) {
    }

    public static IStatGetter attributeGetter(JsonElement json) {
        AttributeData data = StatRegistry.gson.fromJson(json, AttributeData.class);
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

    public static IStatGetter attributeMultiplierGetter(JsonElement json) {
        AttributeMultiplierData data = StatRegistry.gson.fromJson(json, AttributeMultiplierData.class);
        Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(data.attribute));
        if (attribute == null) {
            throw new JsonParseException("Failed to parse attribute multiplier stat getter, unknown attribute: " + data.attribute);
        }

        return new StatGetterAttributeMultiply(attribute);
    }

    record AttributeMultiplierData(String attribute) {
    }

    public static IStatGetter attributeAdditionGetter(JsonElement json) {
        AttributeAdditionData data = StatRegistry.gson.fromJson(json, AttributeAdditionData.class);
        Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(data.attribute));
        if (attribute == null) {
            throw new JsonParseException("Failed to parse attribute addition stat getter, unknown attribute: " + data.attribute);
        }

        return new StatGetterAttributeAddition(attribute);
    }

    record AttributeAdditionData(String attribute) {
    }

    public static IStatGetter effectEfficiencyGetter(JsonElement json) {
        EffectEfficiencyData data = StatRegistry.gson.fromJson(json, EffectEfficiencyData.class);
        if (data.effect == null) {
            throw new JsonParseException("Failed to parse effect efficiency stat getter, missing field 'effect'");
        }
        return new StatGetterEffectEfficiency(ItemEffect.get(data.effect));
    }

    record EffectEfficiencyData(String effect, boolean showNegative) {
    }

    public static IStatGetter effectLevelGetter(JsonElement json) {
        EffectLevelData data = StatRegistry.gson.fromJson(json, EffectLevelData.class);
        if (data.effect == null) {
            throw new JsonParseException("Failed to parse effect level stat getter, missing field 'effect'");
        }
        return new StatGetterEffectLevel(ItemEffect.get(data.effect), data.showNegative);
    }

    record EffectLevelData(String effect, boolean showNegative) {
    }

    public static IStatGetter enchantmentGetter(JsonElement json) {
        EnchantmentData data = StatRegistry.gson.fromJson(json, EnchantmentData.class);
        Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(data.enchantment));
        if (enchantment == null) {
            throw new JsonParseException("Failed to parse enchantment stat getter, unknown enchantment: " + data.enchantment);
        }

        return new StatGetterEnchantmentLevel(enchantment);
    }

    record EnchantmentData(String enchantment) {
    }

    public static IStatGetter isItemGetter(JsonElement json) {
        IsItemData data = StatRegistry.gson.fromJson(json, IsItemData.class);
        List<Item> items = Arrays.stream(data.items).map(ForgeRegistries.ITEMS::getValue).filter(Objects::nonNull).toList();
        return new StatGetterIsItem(items, data.inverted);
    }

    record IsItemData(ResourceLocation[] items, boolean inverted) {
    }
}
