package se.mickelus.tetra.aspect;


import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ItemAspect {
    private static final Map<String, ItemAspect> map = new ConcurrentHashMap<>();

    public static final ItemAspect armor = get("armor");
    public static final ItemAspect armorFeet = get("armor_feet");
    public static final ItemAspect armorLegs = get("armor_legs");
    public static final ItemAspect armorChest = get("armor_chest");
    public static final ItemAspect armorHead = get("armor_head");
    public static final ItemAspect edgedWeapon = get("edged_weapon");
    public static final ItemAspect bluntWeapon = get("blunt_weapon");
    public static final ItemAspect pointyWeapon = get("pointy_weapon");
    public static final ItemAspect throwable = get("throwable");
    public static final ItemAspect blockBreaker = get("block_breaker");
    public static final ItemAspect fishingRod = get("fishing_rod");
    public static final ItemAspect breakable = get("breakable");
    public static final ItemAspect bow = get("bow");
    public static final ItemAspect wearable = get("wearable");
    public static final ItemAspect crossbow = get("crossbow");
    public static final ItemAspect vanishable = get("vanishable");


    private final String key;

    private ItemAspect(String key) {
        this.key = key;
    }

    public static ItemAspect get(String key) {
        return map.computeIfAbsent(key, k -> new ItemAspect(key));
    }

    public String getKey() {
        return key;
    }

    @OnlyIn(Dist.CLIENT)
    public MutableComponent getLabel() {
        return getAspectLabel(this);
    }

    @OnlyIn(Dist.CLIENT)
    public static MutableComponent getAspectLabel(ItemAspect aspect) {
        return getAspectLabel(aspect.getKey());
    }

    @OnlyIn(Dist.CLIENT)
    public static MutableComponent getAspectLabel(String key) {
        String localizationKey = "tetra.aspect." + key;
        if (I18n.exists(localizationKey)) {
            return Component.translatable(localizationKey);
        }
        return Component.literal(StringUtils.capitalize(key.replace("_", " ")));
    }

    @OnlyIn(Dist.CLIENT)
    public MutableComponent getDescription() {
        return getAspectDescription(this);
    }

    @OnlyIn(Dist.CLIENT)
    public static MutableComponent getAspectDescription(ItemAspect aspect) {
        return getAspectDescription(aspect.getKey());
    }

    @OnlyIn(Dist.CLIENT)
    public static MutableComponent getAspectDescription(String key) {
        String localizationKey = "tetra.aspect." + key + ".description";
        if (I18n.exists(localizationKey)) {
            return Component.translatable(localizationKey);
        }
        return Component.translatable("tetra.modular.aspects.missing_description").withStyle(ChatFormatting.GRAY);
    }

    public static class Deserializer implements JsonDeserializer<ItemAspect> {
        @Override
        public ItemAspect deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return Optional.ofNullable(json)
                    .map(JsonElement::getAsString)
                    .map(ItemAspect::get)
                    .orElse(null);
        }
    }
}
