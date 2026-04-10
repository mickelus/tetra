package se.mickelus.tetra.data.predicate;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@ParametersAreNonnullByDefault
public class SimpleItemPredicate implements TetraItemPredicate {
    private final Set<Item> items;
    @Nullable
    private final TagKey<Item> tag;

    public SimpleItemPredicate(Collection<ResourceLocation> itemIds, @Nullable ResourceLocation tagId) {
        this.items = new HashSet<>();
        itemIds.stream()
                .map(ForgeRegistries.ITEMS::getValue)
                .filter(item -> item != null)
                .forEach(items::add);
        this.tag = tagId != null ? ItemTags.create(tagId) : null;
    }

    @Override
    public boolean matches(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        }

        if (!items.isEmpty() && !items.contains(itemStack.getItem())) {
            return false;
        }

        return tag == null || itemStack.is(tag);
    }
}
