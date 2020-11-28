package se.mickelus.tetra.blocks;

import com.google.common.base.Predicates;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class PropertyMatcher implements Predicate<BlockState> {
    public static final PropertyMatcher any = new PropertyMatcher();

    private Block block = null;
    private final Map< Property<?>, Predicate<?>> propertyPredicates = Maps.newHashMap();

    @Override
    public boolean test(BlockState blockState) {
        if (block != null && block != blockState.getBlock()) {
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
        return predicate.test(blockState.get(property));
    }

    public <V extends Comparable<V>> PropertyMatcher where(Property<V> property, Predicate<? extends V> is) {
         this.propertyPredicates.put(property, is);
         return this;
    }

    public static PropertyMatcher deserialize(JsonElement json) {
        PropertyMatcher result = new PropertyMatcher();
        if (json.isJsonObject()) {
            JsonObject jsonObject = JSONUtils.getJsonObject(json, "propertyMatcher");

            if (jsonObject.has("block")) {
                String blockString = jsonObject.get("block").getAsString();
                if (blockString != null) {
                    ResourceLocation resourceLocation = new ResourceLocation(blockString);
                    if (ForgeRegistries.BLOCKS.containsKey(resourceLocation)) {
                        result.block = ForgeRegistries.BLOCKS.getValue(resourceLocation);
                    }
                }
            }


            if (result.block != null && jsonObject.has("state")) {
                StateContainer<Block, BlockState> stateContainer = result.block.getStateContainer();
                for (Map.Entry<String, JsonElement> entry : jsonObject.get("state").getAsJsonObject().entrySet()) {
                    Property<?> property = stateContainer.getProperty(entry.getKey());

                    if (property == null) {
                        throw new JsonSyntaxException("Unknown block state property '" + entry.getKey() + "' for block '"
                                + result.block.getTranslationKey() + "'");
                    }

                    String s = JSONUtils.getString(entry.getValue(), entry.getKey());
                    Optional<?> optional = property.parseValue(s);

                    if (!optional.isPresent()) {
                        throw new JsonSyntaxException("Invalid block state value '" + s + "' for property '" + entry.getKey() + "' on block '"
                                + result.block.getTranslationKey() + "'");
                    }

                    result.propertyPredicates.put(property, Predicates.equalTo(optional.get()));
                }
            }
        } else {
            String blockString = json.getAsString();
            if (blockString != null) {
                ResourceLocation resourceLocation = new ResourceLocation(blockString);
                if (ForgeRegistries.BLOCKS.containsKey(resourceLocation)) {
                    result.block = ForgeRegistries.BLOCKS.getValue(resourceLocation);
                }
            }
        }


        return result;
    }
}
