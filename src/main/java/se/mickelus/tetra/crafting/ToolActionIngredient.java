package se.mickelus.tetra.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.module.schematic.requirement.IntegerPredicate;

import javax.annotation.Nullable;

public class ToolActionIngredient extends Ingredient {
    private final ToolAction toolAction;
    private final IntegerPredicate tier;

    protected ToolActionIngredient(ToolAction toolAction, IntegerPredicate tier) {
        super(ForgeRegistries.ITEMS.getValues().stream()
                .map(Item::getDefaultInstance)
                .filter(stack -> stack.canPerformAction(toolAction))
                .map(ItemValue::new));

        this.toolAction = toolAction;
        this.tier = tier;
    }

    @Override
    public boolean test(@Nullable ItemStack input) {
        if (input == null) {
            return false;
        }

        return input.canPerformAction(toolAction)
                && (tier == null || input.getItem() instanceof ItemModularHandheld item && tier.test(item.getHarvestTier(input, toolAction)));
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return ToolActionIngredient.Serializer.instance;
    }

    @Override
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", CraftingHelper.getID(getSerializer()).toString());
        json.addProperty("tool", toolAction.name());
        if (tier != null) {
            json.add("tier", tier.serialize());
        }

        return json;
    }

    /**
     * Read/write directly to json to make it easier to write in recipe jsons
     */
    public static class Serializer implements IIngredientSerializer<ToolActionIngredient> {
        public static final ToolActionIngredient.Serializer instance = new ToolActionIngredient.Serializer();

        @Override
        public ToolActionIngredient parse(JsonObject json) {
            ToolAction toolAction = ToolAction.get(json.get("tool").getAsString());
            IntegerPredicate tier = json.has("tier")
                    ? DataManager.gson.fromJson(json.getAsJsonObject("tier"), IntegerPredicate.class)
                    : null;
            return new ToolActionIngredient(toolAction, tier);
        }

        @Override
        public ToolActionIngredient parse(FriendlyByteBuf buffer) {
            ToolAction toolAction = ToolAction.get(buffer.readUtf());
            IntegerPredicate tier = IntegerPredicate.fromBuffer(buffer);
            return new ToolActionIngredient(toolAction, tier);
        }

        @Override
        public void write(FriendlyByteBuf buffer, ToolActionIngredient ingredient) {
            buffer.writeUtf(ingredient.toolAction.name());
            if (ingredient.tier != null) {
                ingredient.tier.toBuffer(buffer);
            } else {
                IntegerPredicate.writeNull(buffer);
            }
        }
    }
}
