package se.mickelus.tetra.gui.stats.data;

import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import se.mickelus.mutil.data.deserializer.ResourceLocationDeserializer;
import se.mickelus.mutil.util.JsonOptional;
import se.mickelus.tetra.data.deserializer.ConditionDeserializer;
import se.mickelus.tetra.gui.stats.bar.GuiStatBase;
import se.mickelus.tetra.gui.stats.bar.GuiStatIndicator;
import se.mickelus.tetra.gui.stats.getter.ILabelGetter;
import se.mickelus.tetra.gui.stats.getter.IStatFormat;
import se.mickelus.tetra.gui.stats.getter.IStatGetter;
import se.mickelus.tetra.gui.stats.getter.ITooltipGetter;
import se.mickelus.tetra.gui.stats.sorting.IStatSorter;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class StatRegistry {
    public static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(GuiStatBase.class, new StatBarDeserializer())
            .registerTypeAdapter(IStatSorter.class, new StatSorterDeserializer())
            .registerTypeAdapter(IStatGetter.class, new StatGetterDeserializer())
            .registerTypeAdapter(ILabelGetter.class, new LabelGetterDeserializer())
            .registerTypeAdapter(IStatFormat.class, new StatFormatDeserializer())
            .registerTypeAdapter(ITooltipGetter.class, new TooltipGetterDeserializer())
            .registerTypeAdapter(GuiStatIndicator.class, new IndicatorDeserializer())
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocationDeserializer())
            .registerTypeAdapter(ICondition.class, new ConditionDeserializer())
            .create();
    private static final Map<String, Function<JsonElement, GuiStatBase>> statBarDeserializers = new HashMap<>();
    private static final Map<String, Function<JsonElement, IStatSorter>> statSorterDeserializers = new HashMap<>();
    private static final Map<String, Function<JsonElement, IStatGetter>> statGetterDeserializers = new HashMap<>();
    private static final Map<String, Function<JsonElement, ILabelGetter>> labelGetterDeserializers = new HashMap<>();
    private static final Map<String, Function<JsonElement, IStatFormat>> statformatDeserializers = new HashMap<>();
    private static final Map<String, Function<JsonElement, ITooltipGetter>> tooltipGetterDeserializers = new HashMap<>();
    private static final Map<String, Function<JsonElement, GuiStatIndicator>> indicatorDeserializers = new HashMap<>();

    public static void init() {
        StatRegistry.registerStatBar("tetra:default", StandardStatBarDeserializer::deserialize);
        StatRegistry.registerStatSorter("tetra:default", BasicStatSorterDeserializer::deserialize);

        StatRegistry.registerStatGetter("tetra:and", StatGetterDeserializers::andGetter);
        StatRegistry.registerStatGetter("tetra:or", StatGetterDeserializers::orGetter);
        StatRegistry.registerStatGetter("tetra:multiply", StatGetterDeserializers::multiplyGetter);
        StatRegistry.registerStatGetter("tetra:sum", StatGetterDeserializers::sumGetter);
        StatRegistry.registerStatGetter("tetra:clamp", StatGetterDeserializers::clampGetter);
        StatRegistry.registerStatGetter("tetra:attribute", StatGetterDeserializers::attributeGetter);
        StatRegistry.registerStatGetter("tetra:effect_efficiency", StatGetterDeserializers::effectEfficiencyGetter);
        StatRegistry.registerStatGetter("tetra:effect_level", StatGetterDeserializers::effectLevelGetter);
        StatRegistry.registerStatGetter("tetra:enchantment", StatGetterDeserializers::enchantmentGetter);

        StatRegistry.registerLabelGetter("tetra:basic", LabelGetterDeserializers::basicLabelGetter);
        StatRegistry.registerLabelGetter("tetra:none", LabelGetterDeserializers::noLabelGetter);

        StatRegistry.registerStatFormat("tetra:basic", StatFormatDeserializers::basicStatformat);
        StatRegistry.registerStatFormat("tetra:abbreviate", StatFormatDeserializers::abbreviateStatformat);

        StatRegistry.registerTooltipGetter("tetra:default", TooltipGetterDeserializers::defaultGetter);

        StatRegistry.registerIndicator("tetra:default", IndicatorDeserializers::defaultGetter);
    }

    public static void registerStatBar(String key, Function<JsonElement, GuiStatBase> deserializer) {
        statBarDeserializers.put(key, deserializer);
    }

    public static void registerStatSorter(String key, Function<JsonElement, IStatSorter> deserializer) {
        statSorterDeserializers.put(key, deserializer);
    }

    public static void registerStatGetter(String key, Function<JsonElement, IStatGetter> deserializer) {
        statGetterDeserializers.put(key, deserializer);
    }

    public static void registerLabelGetter(String key, Function<JsonElement, ILabelGetter> deserializer) {
        labelGetterDeserializers.put(key, deserializer);
    }

    public static void registerStatFormat(String key, Function<JsonElement, IStatFormat> deserializer) {
        statformatDeserializers.put(key, deserializer);
    }

    public static void registerTooltipGetter(String key, Function<JsonElement, ITooltipGetter> deserializer) {
        tooltipGetterDeserializers.put(key, deserializer);
    }

    public static void registerIndicator(String key, Function<JsonElement, GuiStatIndicator> deserializer) {
        indicatorDeserializers.put(key, deserializer);
    }

    public static class StatBarDeserializer implements JsonDeserializer<GuiStatBase> {
        @Override
        public GuiStatBase deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            String key = JsonOptional.field(jsonObject, "type")
                    .map(JsonElement::getAsString)
                    .orElse("tetra:default");
            if (statBarDeserializers.containsKey(key)) {
                return statBarDeserializers.get(key).apply(json);
            }
            throw new JsonParseException("No deserializer found for statbar type: " + key);
        }
    }

    public static class StatSorterDeserializer implements JsonDeserializer<IStatSorter> {
        @Override
        public IStatSorter deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            String key = JsonOptional.field(jsonObject, "type")
                    .map(JsonElement::getAsString)
                    .orElse("tetra:default");
            if (statSorterDeserializers.containsKey(key)) {
                return statSorterDeserializers.get(key).apply(json);
            }
            throw new JsonParseException("No deserializer found for stat sorter type: " + key);
        }
    }

    public static class StatGetterDeserializer implements JsonDeserializer<IStatGetter> {
        @Override
        public IStatGetter deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            String key = JsonOptional.field(jsonObject, "type")
                    .map(JsonElement::getAsString)
                    .orElseThrow(() -> new JsonParseException("Missing required field 'type'"));
            if (statGetterDeserializers.containsKey(key)) {
                return statGetterDeserializers.get(key).apply(json);
            }
            throw new JsonParseException("No deserializer found for stat getter type: " + key);
        }
    }

    public static class LabelGetterDeserializer implements JsonDeserializer<ILabelGetter> {
        @Override
        public ILabelGetter deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            String key = JsonOptional.field(jsonObject, "type")
                    .map(JsonElement::getAsString)
                    .orElseThrow(() -> new JsonParseException("Missing required field 'type'"));
            if (labelGetterDeserializers.containsKey(key)) {
                return labelGetterDeserializers.get(key).apply(json);
            }
            throw new JsonParseException("No deserializer found for label getter type: " + key);
        }
    }

    public static class StatFormatDeserializer implements JsonDeserializer<IStatFormat> {
        @Override
        public IStatFormat deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            String key = JsonOptional.field(jsonObject, "type")
                    .map(JsonElement::getAsString)
                    .orElseThrow(() -> new JsonParseException("Missing required field 'type'"));
            if (statformatDeserializers.containsKey(key)) {
                return statformatDeserializers.get(key).apply(json);
            }
            throw new JsonParseException("No deserializer found for label getter type: " + key);
        }
    }

    public static class TooltipGetterDeserializer implements JsonDeserializer<ITooltipGetter> {
        @Override
        public ITooltipGetter deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            String key = JsonOptional.field(jsonObject, "type")
                    .map(JsonElement::getAsString)
                    .orElse("tetra:default");
            if (tooltipGetterDeserializers.containsKey(key)) {
                return tooltipGetterDeserializers.get(key).apply(json);
            }
            throw new JsonParseException("No deserializer found for tooltip getter type: " + key);
        }
    }

    public static class IndicatorDeserializer implements JsonDeserializer<GuiStatIndicator> {
        @Override
        public GuiStatIndicator deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            String key = JsonOptional.field(jsonObject, "type")
                    .map(JsonElement::getAsString)
                    .orElse("tetra:default");
            if (indicatorDeserializers.containsKey(key)) {
                return indicatorDeserializers.get(key).apply(json);
            }
            throw new JsonParseException("No deserializer found for indicator type: " + key);
        }
    }
}
