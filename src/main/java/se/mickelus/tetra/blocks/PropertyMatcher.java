package se.mickelus.tetra.blocks;

import com.google.common.base.Predicates;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import se.mickelus.tetra.compat.forge.registries.ForgeRegistries;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
public class PropertyMatcher implements Predicate<BlockState> {
    public static final Codec<PropertyMatcher> CODEC = Codec.PASSTHROUGH.xmap(
            dynamic -> deserialize(dynamic.convert(JsonOps.INSTANCE).getValue()),
            matcher -> new Dynamic<>(JsonOps.INSTANCE, matcher.serialized)
    );
    public static final PropertyMatcher any = new PropertyMatcher();
    private final Map<Property<?>, Predicate<?>> propertyPredicates = Maps.newHashMap();
    private Block block = null;
    private TagKey<Block> tag = null;
    private JsonElement serialized = JsonNull.INSTANCE;

    public static PropertyMatcher deserialize(JsonElement json) {
        PropertyMatcher result = new PropertyMatcher();
        result.serialized = json.deepCopy();
        if (json.isJsonObject()) {
            JsonObject jsonObject = GsonHelper.convertToJsonObject(json, "propertyMatcher");

            if (jsonObject.has("block")) {
                String blockString = jsonObject.get("block").getAsString();
                if (blockString != null) {
                    ResourceLocation resourceLocation = ResourceLocation.parse(blockString);
                    if (ForgeRegistries.BLOCKS.containsKey(resourceLocation)) {
                        result.block = ForgeRegistries.BLOCKS.getValue(resourceLocation);
                    }
                }
            }

            if (jsonObject.has("tag")) {
                String tagString = jsonObject.get("tag").getAsString();
                if (tagString != null) {
                    result.tag = BlockTags.create(ResourceLocation.parse(tagString));
                }
            }

            if (result.block != null && jsonObject.has("state")) {
                StateDefinition<Block, BlockState> stateContainer = result.block.getStateDefinition();
                for (Map.Entry<String, JsonElement> entry : jsonObject.get("state").getAsJsonObject().entrySet()) {
                    Property<?> property = stateContainer.getProperty(entry.getKey());

                    if (property == null) {
                        throw new JsonSyntaxException("Unknown block state property '" + entry.getKey() + "' for block '"
                                + result.block.getDescriptionId() + "'");
                    }

                    String s = GsonHelper.convertToString(entry.getValue(), entry.getKey());
                    Optional<?> optional = property.getValue(s);

                    if (!optional.isPresent()) {
                        throw new JsonSyntaxException("Invalid block state value '" + s + "' for property '" + entry.getKey() + "' on block '"
                                + result.block.getDescriptionId() + "'");
                    }

                    result.propertyPredicates.put(property, Predicates.equalTo(optional.get()));
                }
            }
        } else {
            String blockString = json.getAsString();
            if (blockString != null) {
                ResourceLocation resourceLocation = ResourceLocation.parse(blockString);
                if (ForgeRegistries.BLOCKS.containsKey(resourceLocation)) {
                    result.block = ForgeRegistries.BLOCKS.getValue(resourceLocation);
                }
            }
        }


        return result;
    }

    @Override
    public boolean test(BlockState blockState) {
        if (block != null && block != blockState.getBlock()) {
            return false;
        }

        if (tag != null && !blockState.getBlockHolder().is(tag)) {
            return false;
        }

        for (Map.Entry<Property<?>, Predicate<?>> entry : this.propertyPredicates.entrySet()) {
            if (!matches(blockState, (Property) entry.getKey(), (Predicate) entry.getValue())) {
                return false;
            }
        }

        return true;
    }

    protected <T extends Comparable<T>> boolean matches(BlockState blockState, Property<T> property, Predicate<T> predicate) {
        return predicate.test(blockState.getValue(property));
    }

    public <V extends Comparable<V>> PropertyMatcher where(Property<V> property, Predicate<? extends V> is) {
        this.propertyPredicates.put(property, is);
        return this;
    }
}
