package se.mickelus.tetra.module.schema;

import com.google.gson.*;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import java.lang.reflect.Type;

/**
 * Materials define a required item in a schema outcome. It's parsed (and mostly it behaves) as if it was an
 * item predicate. Count is stored separately and has to be smaller than size of a provided itemstack for it to match.
 * Example json:
 * {
 *     "item": "minecraft:planks",
 *     "count": 2,
 *     "data": 0
 * }
 */
public class Material {
    public ItemPredicate predicate;
    public int count = 1;

    private ItemStack itemStack;
    private String ore;

    public static class MaterialDeserializer implements JsonDeserializer<Material> {

        @Override
        public Material deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            Material material = new Material();

            if (element != null && !element.isJsonNull()) {
                JsonObject jsonObject = JsonUtils.getJsonObject(element, "material");
                String type = JsonUtils.getString(jsonObject, "type", "");

                material.count = JsonUtils.getInt(jsonObject, "count", 1);
                jsonObject.remove("count");

                if (jsonObject.has("item")) {
                    ResourceLocation resourcelocation = new ResourceLocation(JsonUtils.getString(jsonObject, "item"));
                    Item item = Item.REGISTRY.getObject(resourcelocation);
                    int data = JsonUtils.getInt(jsonObject, "data", 0);
                    material.itemStack = new ItemStack(item, material.count, data);
                } else if ("forge:ore_dict".equals(type) && jsonObject.has("ore")) {
                    material.ore = JsonUtils.getString(jsonObject, "ore");
                }

                try {
                    material.predicate = ItemPredicate.deserialize(element);
                } catch (JsonSyntaxException e) {
                    // skips setting craft predicate, material will be treated as invalid
                }
            }
            return material;
        }
    }

    @SideOnly(Side.CLIENT)
    public String getDisplayName() {
        if (itemStack != null) {
            return itemStack.getDisplayName();
        } else if (ore != null) {
            NonNullList<ItemStack> itemStacks = OreDictionary.getOres(ore);
            if (!itemStacks.isEmpty()) {
                return itemStacks.get(0).getDisplayName();
            }
        }

        return "Unknown material";
    }
}
