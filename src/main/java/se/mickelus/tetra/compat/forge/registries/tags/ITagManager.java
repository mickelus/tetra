package se.mickelus.tetra.compat.forge.registries.tags;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public interface ITagManager<T> {
    TagKey<T> createTagKey(ResourceLocation location);

    ITag<T> getTag(TagKey<T> key);
}
