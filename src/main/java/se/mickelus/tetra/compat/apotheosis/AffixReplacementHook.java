package se.mickelus.tetra.compat.apotheosis;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.function.BiFunction;

public class AffixReplacementHook implements BiFunction<ItemStack, ItemStack, ItemStack> {
    @Override
    public ItemStack apply(ItemStack original, ItemStack replacementStack) {
        CompoundTag replacementTag = replacementStack.getTag();
        Optional.ofNullable(original.getTagElement("affix_data"))
                .map(CompoundTag::copy)
                .ifPresent(nbt -> replacementTag.put("affix_data", nbt));
        Optional.ofNullable(original.getTagElement("affix_data"))
                .map(CompoundTag::copy)
                .ifPresent(nbt -> replacementTag.put("apoth_boss", nbt));

        return replacementStack;
    }
}
