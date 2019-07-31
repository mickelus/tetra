package se.mickelus.tetra.blocks;

import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

import java.util.Map;
import java.util.function.Predicate;

public class PropertyMatcher implements Predicate<IBlockState> {
    private Block block = null;
    private final Map< IProperty<?>, Predicate<?>> propertyPredicates = Maps.newHashMap();
    @Override
    public boolean test(IBlockState blockState) {
        if (block != null && block != blockState.getBlock()) {
            return false;
        }

        for (Map.Entry<IProperty<?>, Predicate<?>> entry : this.propertyPredicates.entrySet()) {
            if (!matches(blockState, (IProperty) entry.getKey(), (Predicate) entry.getValue())) {
                return false;
            }
        }

        return true;
    }

    protected <T extends Comparable<T>> boolean matches(IBlockState blockState, IProperty<T> property, Predicate<T> predicate) {
        return predicate.test(blockState.getValue(property));
    }

    public <V extends Comparable<V>> PropertyMatcher where(IProperty<V> property, Predicate<? extends V> is) {
         this.propertyPredicates.put(property, is);
         return this;
    }

    public static PropertyMatcher deserialize(JsonElement json) {
        PropertyMatcher result = new PropertyMatcher();
        if (json.isJsonObject()) {
            JsonObject jsonObject = JsonUtils.getJsonObject(json, "propertyMatcher");

            if (jsonObject.has("block")) {
                String blockString = jsonObject.get("block").getAsString();
                if (blockString != null) {
                    ResourceLocation resourceLocation = new ResourceLocation(blockString);
                    if (Block.REGISTRY.containsKey(resourceLocation)) {
                        result.block = Block.REGISTRY.getObject(resourceLocation);
                    }
                }
            }


            if (result.block != null && jsonObject.has("state")) {
                BlockStateContainer stateContainer = result.block.getBlockState();
                for (Map.Entry<String, JsonElement> entry : jsonObject.get("state").getAsJsonObject().entrySet()) {
                    IProperty<?> property = stateContainer.getProperty(entry.getKey());

                    if (property == null) {
                        throw new JsonSyntaxException("Unknown block state property '" + entry.getKey() + "' for block '"
                                + result.block.getUnlocalizedName() + "'");
                    }

                    String s = JsonUtils.getString(entry.getValue(), entry.getKey());
                    Optional<?> optional = property.parseValue(s);

                    if (!optional.isPresent()) {
                        throw new JsonSyntaxException("Invalid block state value '" + s + "' for property '" + entry.getKey() + "' on block '"
                                + result.block.getUnlocalizedName() + "'");
                    }

                    result.propertyPredicates.put(property, Predicates.equalTo(optional.get()));
                }
            }
        } else {
            String blockString = json.getAsString();
            if (blockString != null) {
                ResourceLocation resourceLocation = new ResourceLocation(blockString);
                if (Block.REGISTRY.containsKey(resourceLocation)) {
                    result.block = Block.REGISTRY.getObject(resourceLocation);
                }
            }
        }


        return result;
    }
}
