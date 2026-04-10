package se.mickelus.tetra.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.UUID;

public final class ItemStackTagHelper {
    private static final RegistryAccess.Frozen registryAccess = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);

    private ItemStackTagHelper() {}

    public static boolean hasTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && !data.isEmpty();
    }

    @Nullable
    public static CompoundTag getTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null || data.isEmpty()) {
            return null;
        }
        boolean[] hadCycle = new boolean[] { false };
        CompoundTag safeCopy = safeCopy(data.getUnsafe(), hadCycle);
        if (hadCycle[0]) {
            setTag(stack, safeCopy);
        }
        return new LiveCompoundTag(stack, safeCopy);
    }

    public static CompoundTag getOrCreateTag(ItemStack stack) {
        CompoundTag tag = getTag(stack);
        return tag != null ? tag : new LiveCompoundTag(stack, new CompoundTag());
    }

    public static void setTag(ItemStack stack, @Nullable CompoundTag tag) {
        CustomData.set(DataComponents.CUSTOM_DATA, stack, tag == null ? new CompoundTag() : tag);
    }

    @Nullable
    public static CompoundTag getTagElement(ItemStack stack, String key) {
        CompoundTag tag = getTag(stack);
        return tag != null && tag.contains(key, Tag.TAG_COMPOUND) ? tag.getCompound(key) : null;
    }

    public static void removeTagKey(ItemStack stack, String key) {
        CompoundTag tag = getTag(stack);
        if (tag != null) {
            tag.remove(key);
        }
    }

    public static void addTagElement(ItemStack stack, String key, CompoundTag element) {
        getOrCreateTag(stack).put(key, element == null ? new CompoundTag() : safeCopy(element, new boolean[] { false }));
    }

    public static boolean isSerializedStack(CompoundTag tag) {
        return tag != null && tag.contains("id", Tag.TAG_STRING);
    }

    public static ItemStack parseStack(CompoundTag tag) {
        if (!isSerializedStack(tag)) {
            return ItemStack.EMPTY;
        }
        return ItemStack.parseOptional(registryAccess, tag);
    }

    public static CompoundTag saveStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return new CompoundTag();
        }
        return (CompoundTag) stack.save(registryAccess, new CompoundTag());
    }

    private static CompoundTag safeCopy(CompoundTag source, boolean[] hadCycle) {
        return safeCopyTag(source, new IdentityHashMap<>(), hadCycle);
    }

    private static CompoundTag safeCopyTag(CompoundTag root, IdentityHashMap<Tag, Tag> seen, boolean[] hadCycle) {
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
                hadCycle[0] = true;
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

    private static final class LiveCompoundTag extends CompoundTag {
        private final ItemStack stack;

        private LiveCompoundTag(ItemStack stack, CompoundTag source) {
            this.stack = stack;
            merge(source.copy());
        }

        private void sync() {
            setTag(stack, this.copy());
        }

        @Override
        public @Nullable Tag put(String key, Tag value) {
            Tag result = super.put(key, value);
            sync();
            return result;
        }

        @Override
        public void putByte(String key, byte value) {
            super.putByte(key, value);
            sync();
        }

        @Override
        public void putShort(String key, short value) {
            super.putShort(key, value);
            sync();
        }

        @Override
        public void putInt(String key, int value) {
            super.putInt(key, value);
            sync();
        }

        @Override
        public void putLong(String key, long value) {
            super.putLong(key, value);
            sync();
        }

        @Override
        public void putUUID(String key, UUID value) {
            super.putUUID(key, value);
            sync();
        }

        @Override
        public void putFloat(String key, float value) {
            super.putFloat(key, value);
            sync();
        }

        @Override
        public void putDouble(String key, double value) {
            super.putDouble(key, value);
            sync();
        }

        @Override
        public void putString(String key, String value) {
            super.putString(key, value);
            sync();
        }

        @Override
        public void putByteArray(String key, byte[] value) {
            super.putByteArray(key, value);
            sync();
        }

        @Override
        public void putByteArray(String key, List<Byte> value) {
            super.putByteArray(key, value);
            sync();
        }

        @Override
        public void putIntArray(String key, int[] value) {
            super.putIntArray(key, value);
            sync();
        }

        @Override
        public void putIntArray(String key, List<Integer> value) {
            super.putIntArray(key, value);
            sync();
        }

        @Override
        public void putLongArray(String key, long[] value) {
            super.putLongArray(key, value);
            sync();
        }

        @Override
        public void putLongArray(String key, List<Long> value) {
            super.putLongArray(key, value);
            sync();
        }

        @Override
        public void putBoolean(String key, boolean value) {
            super.putBoolean(key, value);
            sync();
        }

        @Override
        public void remove(String key) {
            super.remove(key);
            sync();
        }

        @Override
        public CompoundTag merge(CompoundTag other) {
            CompoundTag result = super.merge(other);
            sync();
            return result;
        }
    }
}
