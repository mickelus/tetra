package se.mickelus.tetra.module;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import se.mickelus.tetra.TetraMod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ItemUpgradeRegistry {

    public static ItemUpgradeRegistry instance;

    private List<Function<ItemStack, ItemStack>> replacementFunctions;

    private Map<String, UpgradeSchema> schemaMap;

    private Map<String, ItemModule> moduleMap;

    public ItemUpgradeRegistry() {
        instance = this;
        replacementFunctions = new ArrayList<> ();
        schemaMap = new HashMap<>();
        moduleMap = new HashMap<>();
    }

    public UpgradeSchema[] getAvailableSchemas(EntityPlayer player, ItemStack itemStack) {
        return schemaMap.values().stream()
                .filter(upgradeSchema -> playerHasSchema(player, upgradeSchema))
                .filter(upgradeSchema -> upgradeSchema.canUpgrade(itemStack))
                .toArray(UpgradeSchema[]::new);
    }

    public UpgradeSchema getSchema(String key) {
        return schemaMap.get(key);
    }

    public boolean playerHasSchema(EntityPlayer player, UpgradeSchema schema) {
        return true;
    }

    public void registerSchema(UpgradeSchema upgradeSchema) {
        schemaMap.put(upgradeSchema.getKey(), upgradeSchema);
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
        return ItemStack.EMPTY;
    }

    public void registerModule(String key, ItemModule module) {
        moduleMap.put(key, module);
    }

    public ItemModule getModule(String key) {
        return moduleMap.get(key);
    }
}
