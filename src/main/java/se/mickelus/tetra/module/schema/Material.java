package se.mickelus.tetra.module.schema;

import com.google.gson.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.lang.reflect.Type;

/**
 * Materials define a required item in a schema outcome. It's parsed (and mostly it behaves) as if it was an
 * item predicate. Count is stored separately and has to be smaller than size of a provided itemstack for it to match.
 * Example json:
 * {
 *     "item": "minecraft:oak_planks",
 *     "count": 2
 * }
 */
public class Material {
    public ItemPredicate predicate;
    public int count = 1;

    private ItemStack itemStack;
    private ResourceLocation tagLocation;

    public static class MaterialDeserializer implements JsonDeserializer<Material> {

        @Override
        public Material deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) {
            Material material = new Material();

            if (element != null && !element.isJsonNull()) {
                JsonObject jsonObject = JSONUtils.getJsonObject(element, "material");

                material.count = JSONUtils.getInt(jsonObject, "count", 1);
                jsonObject.remove("count");

                if (jsonObject.has("item")) {
                    Item item = JSONUtils.getItem(jsonObject, "item");
                    material.itemStack = new ItemStack(item, material.count);

                    if (jsonObject.has("nbt")) {
                        try {
                            CompoundNBT compoundnbt = JsonToNBT.getTagFromJson(JSONUtils.getString(jsonObject.get("nbt"), "nbt"));
                            material.itemStack.setTag(compoundnbt);
                        } catch (CommandSyntaxException exception) {
                            throw new JsonSyntaxException("Encountered invalid nbt tag when parsing material: " + exception.getMessage());
                        }
                    }
                } else if (jsonObject.has("tag")) {
                    material.tagLocation = new ResourceLocation(JSONUtils.getString(jsonObject, "tag"));
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

    @OnlyIn(Dist.CLIENT)
    public ITextComponent[] getDisplayNames() {
        if (itemStack != null) {
            return new ITextComponent[] {itemStack.getDisplayName()};
        } else if (tagLocation != null) {
            return ItemTags.getCollection()
                    .getOrCreate(tagLocation)
                    .getAllElements()
                    .stream()
                    .map(item -> item.getDisplayName(item.getDefaultInstance()))
                    .toArray(ITextComponent[]::new);
        }

        return new ITextComponent[] {new StringTextComponent("Unknown material")};
    }

    @OnlyIn(Dist.CLIENT)
    public ItemStack[] getApplicableItemstacks() {
        if (itemStack != null && !itemStack.isEmpty()) {
            return new ItemStack[] { itemStack };
        } else if (tagLocation != null) {
            return ItemTags.getCollection()
                    .getOrCreate(tagLocation)
                    .getAllElements()
                    .stream()
                    .map(Item::getDefaultInstance)
                    .map(this::setCount)
                    .toArray(ItemStack[]::new);
        }

        return new ItemStack[0];
    }

    private ItemStack setCount(ItemStack itemStack) {
        itemStack.setCount(count);
        return itemStack;
    }
}
