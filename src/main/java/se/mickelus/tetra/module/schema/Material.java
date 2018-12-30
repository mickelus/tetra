package se.mickelus.tetra.module.schema;

import com.google.gson.*;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
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
    public ItemStack repairMaterial = ItemStack.EMPTY;
    public ItemStack salvageMaterial = ItemStack.EMPTY;
    public ItemPredicate craftPredicate;

    public int count = 1;

    public static class MaterialDeserializer implements JsonDeserializer<Material> {

        @Override
        public Material deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            Material material = new Material();

            if (element != null && !element.isJsonNull()) {

                JsonObject jsonObject = JsonUtils.getJsonObject(element, "material");
                String type = JsonUtils.getString(jsonObject, "type", "");
                Item item = null;
                int data = 0;
                if ("forge:ore_dict".equals(type)) {
                    String oreName = JsonUtils.getString(jsonObject, "ore");
                    if (OreDictionary.doesOreNameExist(oreName)) {
                        NonNullList<ItemStack> itemStacks = OreDictionary.getOres(oreName);
                        if (!itemStacks.isEmpty()) {
                            ItemStack itemStack = itemStacks.get(0);
                            data = itemStack.getMetadata();
                            item = itemStack.getItem();
                        }
                    }
                } else if (jsonObject.has("item")) {
                    ResourceLocation resourcelocation = new ResourceLocation(JsonUtils.getString(jsonObject, "item"));
                    item = Item.REGISTRY.getObject(resourcelocation);
                }

                if (item != null) {
                    material.count = JsonUtils.getInt(jsonObject, "count", 1);
                    if (data != 0) {
                        data = JsonUtils.getInt(jsonObject, "data", 0);
                    }
                    material.repairMaterial = new ItemStack(item, material.count, data);
                    material.salvageMaterial = new ItemStack(item, material.count, data);

                    jsonObject.remove("count");
                    material.craftPredicate = ItemPredicate.deserialize(element);
                }
            }
            return material;
        }
    }
}
