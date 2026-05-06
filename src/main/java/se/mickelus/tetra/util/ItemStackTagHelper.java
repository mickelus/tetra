package se.mickelus.tetra.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;

// Cross-version compat: this helper bridges 1.20-style raw NBT to 1.21's immutable CustomData component.
// The shape (getTag/getOrCreateTag/setTag/getTagElement/addTagElement/removeTagKey) intentionally
// mirrors upstream 1.20 ItemStack so the ~30 NBT-style call sites stay parallel with the 1.20 source.
// Do not migrate these keys to typed DataComponentTypes — that forks the data model.
public final class ItemStackTagHelper {
    private ItemStackTagHelper() {}

    // Re-entrancy: when a mutate is in flight for a stack, nested mutate(stack, ...) calls
    // on the same stack share the in-flight tag and skip the read/write, so helpers that
    // call other helpers don't clobber each other's pending writes.
    private static final ThreadLocal<Map<ItemStack, CompoundTag>> activeMutations =
            ThreadLocal.withInitial(IdentityHashMap::new);

    public static boolean hasTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && !data.isEmpty();
    }

    /**
     * Returns a detached defensive copy of the stack's CUSTOM_DATA tag, or null if absent/empty.
     * Mutations on the returned tag DO NOT propagate back to the stack — use {@link #mutate} for writes.
     */
    @Nullable
    public static CompoundTag getTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null || data.isEmpty()) {
            return null;
        }
        return safeCopy(data.getUnsafe());
    }

    /**
     * Like {@link #getTag} but returns an empty tag instead of null when CUSTOM_DATA is absent.
     * Returned tag is a detached copy; mutations DO NOT propagate — use {@link #mutate} for writes.
     */
    public static CompoundTag getOrCreateTag(ItemStack stack) {
        CompoundTag tag = getTag(stack);
        return tag != null ? tag : new CompoundTag();
    }

    public static void setTag(ItemStack stack, @Nullable CompoundTag tag) {
        CustomData.set(DataComponents.CUSTOM_DATA, stack, tag == null ? new CompoundTag() : tag);
    }

    /**
     * Reads CUSTOM_DATA, runs the consumer on a defensive copy, then writes it back atomically.
     * This is the only correct way to mutate the stack's CUSTOM_DATA in place.
     * Re-entrant: nested calls on the same stack reuse the outer in-flight tag.
     */
    public static void mutate(ItemStack stack, Consumer<CompoundTag> consumer) {
        if (stack.isEmpty()) {
            return;
        }

        Map<ItemStack, CompoundTag> active = activeMutations.get();
        CompoundTag inFlight = active.get(stack);
        if (inFlight != null) {
            consumer.accept(inFlight);
            return;
        }

        CompoundTag tag = getOrCreateTag(stack);
        active.put(stack, tag);
        try {
            consumer.accept(tag);
            setTag(stack, tag);
        } finally {
            active.remove(stack);
            if (active.isEmpty()) {
                activeMutations.remove();
            }
        }
    }

    /**
     * Returns a detached defensive copy of the named child compound, or null if absent.
     * Mutations on the returned tag DO NOT propagate — use {@link #mutate} for writes.
     */
    @Nullable
    public static CompoundTag getTagElement(ItemStack stack, String key) {
        CompoundTag tag = getTag(stack);
        return tag != null && tag.contains(key, Tag.TAG_COMPOUND) ? tag.getCompound(key) : null;
    }

    public static void removeTagKey(ItemStack stack, String key) {
        if (!hasTag(stack)) {
            return;
        }
        mutate(stack, tag -> tag.remove(key));
    }

    public static void addTagElement(ItemStack stack, String key, CompoundTag element) {
        mutate(stack, tag -> tag.put(key, element == null ? new CompoundTag() : safeCopy(element)));
    }

    public static boolean isSerializedStack(CompoundTag tag) {
        return tag != null && tag.contains("id", Tag.TAG_STRING);
    }

    public static ItemStack parseStack(HolderLookup.Provider registryAccess, CompoundTag tag) {
        if (!isSerializedStack(tag)) {
            return ItemStack.EMPTY;
        }
        return ItemStack.parseOptional(registryAccess, tag);
    }

    public static CompoundTag saveStack(ItemStack stack, HolderLookup.Provider registryAccess) {
        if (stack == null || stack.isEmpty()) {
            return new CompoundTag();
        }
        return (CompoundTag) stack.save(registryAccess, new CompoundTag());
    }

    private static CompoundTag safeCopy(CompoundTag source) {
        return safeCopyTag(source, new IdentityHashMap<>());
    }

    private static CompoundTag safeCopyTag(CompoundTag root, IdentityHashMap<Tag, Tag> seen) {
        if (root == null) {
            return new CompoundTag();
        }

        CompoundTag rootCopy = new CompoundTag();
        seen.put(root, rootCopy);

        ArrayList<Frame> stack = new ArrayList<>();
        stack.add(Frame.forCompound(root, rootCopy));

        while (!stack.isEmpty()) {
            Frame frame = stack.get(stack.size() - 1);
            if (!frame.advance()) {
                stack.remove(stack.size() - 1);
                continue;
            }

            Tag child = frame.currentValue;
            if (child == null) {
                frame.putValue(new CompoundTag());
                continue;
            }

            Tag existing = seen.get(child);
            if (existing != null) {
                frame.putValue(existing instanceof ListTag ? new ListTag() : new CompoundTag());
                continue;
            }

            if (child instanceof CompoundTag compoundChild) {
                CompoundTag copy = new CompoundTag();
                seen.put(child, copy);
                frame.putValue(copy);
                stack.add(Frame.forCompound(compoundChild, copy));
            } else if (child instanceof ListTag listChild) {
                ListTag copy = new ListTag();
                seen.put(child, copy);
                frame.putValue(copy);
                stack.add(Frame.forList(listChild, copy));
            } else {
                frame.putValue(child.copy());
            }
        }

        return rootCopy;
    }

    private static final class Frame {
        private final CompoundTag compoundSource;
        private final CompoundTag compoundDest;
        private final ListTag listSource;
        private final ListTag listDest;
        private final java.util.Iterator<String> keys;
        private int index = -1;
        private String currentKey;
        private Tag currentValue;

        private Frame(CompoundTag compoundSource, CompoundTag compoundDest) {
            this.compoundSource = compoundSource;
            this.compoundDest = compoundDest;
            this.listSource = null;
            this.listDest = null;
            this.keys = compoundSource.getAllKeys().iterator();
        }

        private Frame(ListTag listSource, ListTag listDest) {
            this.compoundSource = null;
            this.compoundDest = null;
            this.listSource = listSource;
            this.listDest = listDest;
            this.keys = null;
        }

        static Frame forCompound(CompoundTag source, CompoundTag dest) {
            return new Frame(source, dest);
        }

        static Frame forList(ListTag source, ListTag dest) {
            return new Frame(source, dest);
        }

        boolean advance() {
            if (compoundSource != null) {
                if (!keys.hasNext()) {
                    return false;
                }
                currentKey = keys.next();
                currentValue = compoundSource.get(currentKey);
                return true;
            }

            index++;
            if (index >= listSource.size()) {
                return false;
            }
            currentValue = listSource.get(index);
            return true;
        }

        void putValue(Tag value) {
            if (compoundSource != null) {
                compoundDest.put(currentKey, value);
            } else {
                listDest.add(value);
            }
        }
    }
}
