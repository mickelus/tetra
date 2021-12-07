package se.mickelus.tetra.module.schematic;

import com.google.gson.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.SerializationTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.tetra.data.deserializer.ItemPredicateDeserializer;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@ParametersAreNonnullByDefault
public class OutcomeMaterial {
    private static final Logger logger = LogManager.getLogger();
    private static final JsonArray emptyArray = new JsonArray();

    public int count = 1;

    protected Collection<ItemStack> itemStacks = Collections.emptyList();
    protected ResourceLocation tagLocation;

    private ItemPredicate predicate;

    public OutcomeMaterial offsetCount(float multiplier, int offset) {
        OutcomeMaterial result = new OutcomeMaterial();
        result.count = Math.round(count * multiplier) + offset;

        result.itemStacks = itemStacks.stream()
                .map(ItemStack::copy)
                .peek(result::setCount)
                .collect(Collectors.toList());

        result.tagLocation = tagLocation;
        result.predicate = predicate;

        return result;
    }

    // todo: this is rather hacky, networked tags are required on clients in multiplayer, but that's not always available
    protected static TagCollection<Item> getTagCollection() {
        if (FMLEnvironment.dist.isClient()) {
            if (Minecraft.getInstance().level != null) {
                return Minecraft.getInstance().level.getTagManager().getOrEmpty(Registry.ITEM_REGISTRY);
            }
        }

        return SerializationTags.getInstance().getOrEmpty(Registry.ITEM_REGISTRY);
    }

    @OnlyIn(Dist.CLIENT)
    public Component[] getDisplayNames() {
        if (getPredicate() == null) {
            return new Component[] {new TextComponent("Unknown material")};
        } else if (itemStacks != null) {
            return itemStacks.stream().map(ItemStack::getHoverName).toArray(Component[]::new);
        } else if (tagLocation != null) {
            return getTagCollection()
                    .getTagOrEmpty(tagLocation)
                    .getValues()
                    .stream()
                    .map(item -> item.getName(item.getDefaultInstance()))
                    .toArray(Component[]::new);
        }

        return new Component[] {new TextComponent("Unknown material")};
    }

    public ItemStack[] getApplicableItemStacks() {
        if (getPredicate() == null) {
            return new ItemStack[0];
        } else if (itemStacks != null && !itemStacks.isEmpty()) {
            return itemStacks.toArray(ItemStack[]::new);
        } else if (tagLocation != null) {
            return getTagCollection()
                    .getTagOrEmpty(tagLocation)
                    .getValues()
                    .stream()
                    .map(Item::getDefaultInstance)
                    .map(this::setCount)
                    .toArray(ItemStack[]::new);
        }

        return new ItemStack[0];
    }

    @Nullable
    public ItemPredicate getPredicate() {
        return predicate;
    }

    private ItemStack setCount(ItemStack itemStack) {
        itemStack.setCount(count);
        return itemStack;
    }

    public boolean isTagged() {
        return tagLocation != null;
    }

    public boolean isValid() {
        return predicate != null;
    }

    public static class Deserializer implements JsonDeserializer<OutcomeMaterial> {

        @Override
        public OutcomeMaterial deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) {
            OutcomeMaterial material = new OutcomeMaterial();

            if (element != null && !element.isJsonNull()) {
                JsonObject jsonObject = GsonHelper.convertToJsonObject(element, "material");

                material.count = GsonHelper.getAsInt(jsonObject, "count", 1);
                jsonObject.remove("count");

                if (jsonObject.has("items")) {
                    try {
                        material.itemStacks = StreamSupport.stream(GsonHelper.getAsJsonArray(jsonObject, "items", emptyArray).spliterator(), false)
                                .map(jsonElement -> GsonHelper.convertToString(jsonElement, "item"))
                                .map(ResourceLocation::new)
                                .map(Registry.ITEM::getOptional)
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .map(item -> new ItemStack(item, material.count))
                                .collect(Collectors.toList());
                    } catch (JsonSyntaxException e) {
                        material.itemStacks = Collections.emptyList();
                    }

                    if (!material.itemStacks.isEmpty() && jsonObject.has("nbt")) {
                        try {
                            CompoundTag compoundnbt = TagParser.parseTag(GsonHelper.convertToString(jsonObject.get("nbt"), "nbt"));
                            material.itemStacks.forEach(itemStack -> itemStack.setTag(compoundnbt));
                        } catch (CommandSyntaxException exception) {
                            throw new JsonSyntaxException("Encountered invalid nbt tag when parsing material: " + exception.getMessage());
                        }
                    }

                } else if (jsonObject.has("tag")) {
                    material.tagLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "tag"));
                }

                if (!jsonObject.has("type") && jsonObject.has("tag")) {
                    material.predicate = deserializeTagPredicate(element);
                } else {
                    material.predicate = ItemPredicateDeserializer.deserialize(element);
                }
            }
            return material;
        }

        // todo: workaround as vanilla predicates always use the non-networked tag manager
        private ItemPredicate deserializeTagPredicate(@Nullable JsonElement element) {
            JsonObject jsonObject = GsonHelper.convertToJsonObject(element, "item");

            ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "tag"));
            Tag<Item> tag = getTagCollection().getTag(resourceLocation);

            if (tag == null) {
                logger.debug("Failed to parse outcome material predicate for \"{}\": Unknown tag '{}'", jsonObject.toString(), resourceLocation);
                return null;
            }

            MinMaxBounds.Ints count = MinMaxBounds.Ints.fromJson(jsonObject.get("count"));
            MinMaxBounds.Ints durability = MinMaxBounds.Ints.fromJson(jsonObject.get("durability"));
            EnchantmentPredicate[] enchantments = EnchantmentPredicate.fromJsonArray(jsonObject.get("enchantments"));
            EnchantmentPredicate[] storedEnchantments = EnchantmentPredicate.fromJsonArray(jsonObject.get("stored_enchantments"));
            NbtPredicate nbt = NbtPredicate.fromJson(jsonObject.get("nbt"));

            return new ItemPredicate(tag, null, count, durability, enchantments, storedEnchantments, null, nbt);
        }
    }
}
