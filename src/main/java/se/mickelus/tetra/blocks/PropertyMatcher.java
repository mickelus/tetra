package se.mickelus.tetra.blocks;

import com.google.common.base.Predicates;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
public class PropertyMatcher implements Predicate<BlockState> {
    public static final PropertyMatcher any = new PropertyMatcher();
    private final Map<Property<?>, Predicate<?>> propertyPredicates = Maps.newHashMap();
    private Block block = null;

    public static PropertyMatcher deserialize(JsonElement json) {
        PropertyMatcher result = new PropertyMatcher();
        if (json.isJsonObject()) {
            JsonObject jsonObject = GsonHelper.convertToJsonObject(json, "propertyMatcher");

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
                ResourceLocation resourceLocation = new ResourceLocation(blockString);
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
