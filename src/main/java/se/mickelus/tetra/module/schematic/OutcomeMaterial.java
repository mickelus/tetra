package se.mickelus.tetra.module.schematic;

import com.google.gson.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

/**
 * Materials define a required item in a schematic outcome. It's parsed (and mostly it behaves) as if it was an
 * item predicate. Count is stored separately and has to be smaller than size of a provided itemstack for it to match.
 * Example json:
 * {
 *     "item": "minecraft:oak_planks",
 *     "count": 2
 * }
 */
public class OutcomeMaterial {
    private static final Logger logger = LogManager.getLogger();

    private ItemPredicate predicate;
    private LazyOptional<ItemPredicate> lazyPredicate;
    public int count = 1;

    private ItemStack itemStack;
    private ResourceLocation tagLocation;

    public static class Deserializer implements JsonDeserializer<OutcomeMaterial> {

        @Override
        public OutcomeMaterial deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) {
            OutcomeMaterial material = new OutcomeMaterial();

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

                material.lazyPredicate = LazyOptional.of(() -> {
                    try {
                        return ItemPredicate.deserialize(element);
                    } catch (JsonParseException e) {
                        // skips setting craft predicate, material will be treated as invalid
                        logger.debug("Failed to parse outcome material predicate for \"{}\": {}", jsonObject.toString(), e.getMessage());
                        throw e;
                    }
                });

            }
            return material;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public ITextComponent[] getDisplayNames() {
        if (getPredicate() == null) {
            return new ITextComponent[] {new StringTextComponent("Unknown material")};
        } else if (itemStack != null) {
            return new ITextComponent[] {itemStack.getDisplayName()};
        } else if (tagLocation != null) {
            return TagCollectionManager.func_232928_e_().func_232925_b_()
                    .getOrCreate(tagLocation)
                    .getAllElements()
                    .stream()
                    .map(item -> item.getDisplayName(item.getDefaultInstance()))
                    .toArray(ITextComponent[]::new);
        }

        return new ITextComponent[] {new StringTextComponent("Unknown material")};
    }

    @OnlyIn(Dist.CLIENT)
    public ItemStack[] getApplicableItemStacks() {
        if (getPredicate() == null) {
            return new ItemStack[0];
        } else if (itemStack != null && !itemStack.isEmpty()) {
            return new ItemStack[] { itemStack };
        } else if (tagLocation != null) {
            return TagCollectionManager.func_232928_e_().func_232925_b_()
                    .getOrCreate(tagLocation)
                    .getAllElements()
                    .stream()
                    .map(Item::getDefaultInstance)
                    .map(this::setCount)
                    .toArray(ItemStack[]::new);
        }

        return new ItemStack[0];
    }

    @Nullable
    public ItemPredicate getPredicate() {
        try {
            return lazyPredicate.orElse(null);
        } catch (JsonParseException e) {
            return null;
        }
    }

    private ItemStack setCount(ItemStack itemStack) {
        itemStack.setCount(count);
        return itemStack;
    }

    public boolean isTagged() {
        return tagLocation != null;
    }

    public boolean isValid() {
        return isTagged() || itemStack != null;
    }
}
