package se.mickelus.tetra.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import se.mickelus.tetra.blocks.scroll.ScrollData;
import se.mickelus.tetra.blocks.scroll.ScrollItem;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
public class ScrollIngredient extends Ingredient {
    private final ItemStack itemStack;

    private final ScrollData data;

    protected ScrollIngredient(ItemStack itemStack, ScrollData data) {
        super(Stream.of(new Ingredient.ItemValue(itemStack)));
        this.itemStack = itemStack;

        this.data = data;
    }

    @Override
    public boolean test(@Nullable ItemStack input) {
        if (input == null || input.getItem() != ScrollItem.instance) {
            return false;
        }

        ScrollData inputData = ScrollData.read(input);

        return data.key.equals(inputData.key);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return Serializer.instance;
    }

    @Override
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        data.write(json);

        return json;
    }

    /**
     * Read/write directly to json to make it easier to write in recipe jsons
     */
    public static class Serializer implements IIngredientSerializer<ScrollIngredient> {
        public static final ScrollIngredient.Serializer instance = new ScrollIngredient.Serializer();

        @Override
        public ScrollIngredient parse(JsonObject json) {
            ScrollData data = ScrollData.read(json);
            ItemStack itemStack = new ItemStack(ScrollItem.instance);
            data.write(itemStack);

            return new ScrollIngredient(itemStack, data);
        }

        @Override
        public ScrollIngredient parse(FriendlyByteBuf buffer) {
            ItemStack itemStack = buffer.readItem();
            return new ScrollIngredient(itemStack, ScrollData.read(itemStack));
        }

        @Override
        public void write(FriendlyByteBuf buffer, ScrollIngredient ingredient) {
            buffer.writeItem(ingredient.itemStack);
        }
    }
}
