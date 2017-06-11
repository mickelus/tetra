package se.mickelus.tetra.module;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import se.mickelus.tetra.TetraMod;

import java.util.List;
import java.util.function.Function;

public class ItemUpgradeRegistry {

    public static final String moduleName = TetraMod.MOD_ID + "moduleName";

    public static ItemUpgradeRegistry instance;

    private List<Function<ItemStack, ItemStack>> replacementFunctions;

    public ItemUpgradeRegistry() {
        instance = this;
    }

    public boolean canUpgrade(ItemStack itemStack) {
        return true;
    }

    public UpgradeSchema[] getAvailableUpgrades(ItemStack itemStack) {
        return new UpgradeSchema[0];
    }

    public void registerPlaceholder(Function<ItemStack, ItemStack> replacementFunction) {
        replacementFunctions.add(replacementFunction);
    }

    public ItemStack getPlaceholder(ItemStack itemStack) {
        for (Function<ItemStack, ItemStack> replacementFunction : replacementFunctions) {
            ItemStack replacementStack = replacementFunction.apply(itemStack);
            if (replacementStack != null) {
                return replacementStack;
            }
        }
        return null;
    }

    public ItemModule getModuleFromNBT(NBTTagCompound tag) {

        return null;
    }
}
