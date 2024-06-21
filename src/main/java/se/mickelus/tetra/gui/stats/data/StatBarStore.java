package se.mickelus.tetra.gui.stats.data;

import com.google.gson.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.GsonHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.mutil.data.deserializer.ResourceLocationDeserializer;
import se.mickelus.mutil.util.JsonOptional;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.workbench.gui.WorkbenchStatsGui;
import se.mickelus.tetra.gui.stats.bar.GuiStatBase;
import se.mickelus.tetra.gui.stats.bar.GuiStatIndicator;
import se.mickelus.tetra.gui.stats.getter.ILabelGetter;
import se.mickelus.tetra.gui.stats.getter.IStatGetter;
import se.mickelus.tetra.gui.stats.getter.ITooltipGetter;
import se.mickelus.tetra.gui.stats.getter.StatFormat;
import se.mickelus.tetra.items.modular.impl.holo.gui.craft.HoloStatsGui;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class StatBarStore implements ResourceManagerReloadListener {
    private static final Logger logger = LogManager.getLogger();
    public static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(GuiStatBase.class, new StatBarStore.StatBarDeserializer())
            .registerTypeAdapter(IStatGetter.class, new StatBarStore.StatGetterDeserializer())
            .registerTypeAdapter(ILabelGetter.class, new StatBarStore.LabelGetterDeserializer())
            .registerTypeAdapter(StatFormat.class, new StatBarStore.StatFormatDeserializer())
            .registerTypeAdapter(ITooltipGetter.class, new StatBarStore.TooltipGetterDeserializer())
            .registerTypeAdapter(GuiStatIndicator.class, new StatBarStore.IndicatorDeserializer())
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocationDeserializer())
            .create();
    public static StatBarStore instance;
    private GuiStatBase[] statBars = new GuiStatBase[0];

    private static final Map<String, Function<JsonElement, GuiStatBase>> statBarDeserializers = new HashMap<>();
    private static final Map<String, Function<JsonElement, IStatGetter>> statGetterDeserializers = new HashMap<>();
    private static final Map<String, Function<JsonElement, ILabelGetter>> labelGetterDeserializers = new HashMap<>();
    private static final Map<String, Function<JsonElement, StatFormat>> statformatDeserializers = new HashMap<>();
    private static final Map<String, Function<JsonElement, ITooltipGetter>> tooltipGetterDeserializers = new HashMap<>();
    private static final Map<String, Function<JsonElement, GuiStatIndicator>> indicatorDeserializers = new HashMap<>();

    public StatBarStore() {
        ((ReloadableResourceManager) Minecraft.getInstance().getResourceManager()).registerReloadListener(this);

        instance = this;

        registerStatBar("tetra:default", StandardStatBarDeserializer::deserialize);

        registerStatGetter("tetra:and", StatGetterDeserializers::andGetter);
        registerStatGetter("tetra:or", StatGetterDeserializers::orGetter);
        registerStatGetter("tetra:multiply", StatGetterDeserializers::multiplyGetter);
        registerStatGetter("tetra:sum", StatGetterDeserializers::sumGetter);
        registerStatGetter("tetra:attribute", StatGetterDeserializers::attributeGetter);
        registerStatGetter("tetra:effect_efficiency", StatGetterDeserializers::effectEfficiencyGetter);
        registerStatGetter("tetra:effect_level", StatGetterDeserializers::effectLevelGetter);
        registerStatGetter("tetra:enchantment", StatGetterDeserializers::enchantmentGetter);

        registerLabelGetter("tetra:basic", LabelGetterDeserializers::basicLabelGetter);
        registerLabelGetter("tetra:none", LabelGetterDeserializers::noLabelGetter);

        registerStatFormat("tetra:basic", StatFormatDeserializers::basicStatformat);
        registerStatFormat("tetra:abbreviate", StatFormatDeserializers::abbreviateStatformat);

        registerTooltipGetter("tetra:default", TooltipGetterDeserializers::defaultGetter);

        registerIndicator("tetra:default", IndicatorDeserializers::standardGetter);

    }

    public static void registerStatBar(String key, Function<JsonElement, GuiStatBase> deserializer) {
        statBarDeserializers.put(key, deserializer);
    }

    public static void registerStatGetter(String key, Function<JsonElement, IStatGetter> deserializer) {
        statGetterDeserializers.put(key, deserializer);
    }

    public static void registerLabelGetter(String key, Function<JsonElement, ILabelGetter> deserializer) {
        labelGetterDeserializers.put(key, deserializer);
    }

    public static void registerStatFormat(String key, Function<JsonElement, StatFormat> deserializer) {
        statformatDeserializers.put(key, deserializer);
    }

    public static void registerTooltipGetter(String key, Function<JsonElement, ITooltipGetter> deserializer) {
        tooltipGetterDeserializers.put(key, deserializer);
    }

    public static void registerIndicator(String key, Function<JsonElement, GuiStatIndicator> deserializer) {
        indicatorDeserializers.put(key, deserializer);
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        statBars = prepareBars();
        System.out.println(statBars);
        HoloStatsGui.setDataBars(Arrays.stream(statBars).filter(bar -> Arrays.asList(bar.getContexts()).contains("tetra:holosphere")).toArray(GuiStatBase[]::new));
        WorkbenchStatsGui.setDataBars(Arrays.stream(statBars).filter(bar -> Arrays.asList(bar.getContexts()).contains("tetra:workbench")).toArray(GuiStatBase[]::new));
    }

    public GuiStatBase[] getBars() {
        return statBars;
    }

    private static GuiStatBase[] prepareBars() {
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        return resourceManager.listResources("stat_bars", rl -> rl.getPath().endsWith(".json")).entrySet().stream()
                .filter(entry -> TetraMod.MOD_ID.equals(entry.getKey().getNamespace()))
                .map(entry -> parseBar(entry.getKey(), entry.getValue()))
                .filter(Objects::nonNull)
                .toArray(GuiStatBase[]::new);
    }

    @Nullable
    private static GuiStatBase parseBar(ResourceLocation resourceLocation, Resource resource) {
        try (BufferedReader reader = resource.openAsReader()) {
            return GsonHelper.fromJson(gson, reader, GuiStatBase.class);
        } catch (IOException | JsonParseException e) {
            logger.warn("Failed to parse statbar data from '{}': {}", resourceLocation, e);
        }

        return null;
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

    public static class StatFormatDeserializer implements JsonDeserializer<StatFormat> {
        @Override
        public StatFormat deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
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
