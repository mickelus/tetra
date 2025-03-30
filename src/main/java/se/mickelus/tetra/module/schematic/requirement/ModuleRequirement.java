package se.mickelus.tetra.module.schematic.requirement;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import se.mickelus.mutil.util.JsonOptional;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ModuleRegistry;
import se.mickelus.tetra.module.schematic.CraftingContext;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ModuleRequirement implements CraftingRequirement {
    String moduleKey;
    String moduleVariant;
    String materialPattern;

    public ModuleRequirement(String moduleKey, String moduleVariant, String moduleMaterial) {
        this.moduleKey = moduleKey;
        this.moduleVariant = moduleVariant;

        if (moduleMaterial != null) {
            this.materialPattern = "\\/" + moduleMaterial + "(?:_|$)";
        }
    }

    @Override
    public boolean test(CraftingContext context) {
        if (context.targetModule != null) {
            if (moduleKey != null && !moduleKey.equals(context.targetModule.getKey())) {
                return false;
            }
            String currentVariant = context.targetModule.getVariantData(context.targetStack).key;
            if (moduleVariant != null && !moduleVariant.equals(currentVariant)) {
                return false;
            }
            if (materialPattern != null && !currentVariant.matches(materialPattern)) {
                return false;
            }
            return true;
        }
        return false;
    }

    public static class Deserializer implements JsonDeserializer<CraftingRequirement> {
        @Override
        public CraftingRequirement deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new ModuleRequirement(
                    JsonOptional.field(json.getAsJsonObject(), "module")
                            .map(JsonElement::getAsString)
                            .orElse(null),
                    JsonOptional.field(json.getAsJsonObject(), "variant")
                            .map(JsonElement::getAsString)
                            .orElse(null),
                    JsonOptional.field(json.getAsJsonObject(), "material")
                            .map(JsonElement::getAsString)
                            .orElse(null));
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    @Nullable
    public List<Component> getDescription() {

        String[] values = new String[] {
                moduleKey != null ? I18n.get("tetra.holo.module_requirement.module_key", getModuleName(moduleKey)) : null,
                moduleVariant != null ? I18n.get("tetra.holo.module_requirement.variant_key", ItemModule.getVariantName(moduleVariant)) : null,
                materialPattern != null ? I18n.get("tetra.holo.module_requirement.material", I18n.get("tetra.material." + materialPattern)) : null,
        };

        return List.of(Component.literal("Module " + Arrays.stream(values).filter(Objects::nonNull).collect(Collectors.joining(", "))));
    }

    static String getModuleName(String moduleKey) {
        if (I18n.exists("tetra.module." + moduleKey + ".name")) {
            return ItemModule.getModuleName(moduleKey);
        }

        return Optional.ofNullable(ModuleRegistry.instance.getModule(new ResourceLocation("tetra", moduleKey)))
                .map(module -> ItemModule.getModuleName(module.getUnlocalizedName()))
                .orElse(moduleKey);
    }
}
