package se.mickelus.tetra.module;


import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import se.mickelus.tetra.NBTHelper;

import java.util.Arrays;

public abstract class ItemModuleMajor<T extends ModuleData> extends ItemModule<T> {

    protected String[] improvements = new String[0];

    public ItemModuleMajor(String slotKey, String moduleKey) {
        super(slotKey, moduleKey);
    }

    public int getImprovementLevel(String improvementName, ItemStack itemStack) {
        return NBTHelper.getTag(itemStack).getInteger(slotKey + ":" + improvementName);
    }

    public String[] getImprovements(ItemStack itemStack) {
        NBTTagCompound tag = NBTHelper.getTag(itemStack);
        return Arrays.stream(improvements)
            .filter(improvement -> tag.hasKey(slotKey + ":" + improvement))
            .toArray(String[]::new);
    }

    public boolean acceptsImprovement(String improvement) {
        return Arrays.stream(improvements).anyMatch(improvement::equals);
    }

    public void addImprovement(ItemStack itemStack, String improvement, int level) {
        NBTHelper.getTag(itemStack).setInteger(slotKey + ":" + improvement, level);
    }

    @Override
    public ItemStack[] removeModule(ItemStack targetStack) {
        ItemStack[] salvage = super.removeModule(targetStack);

        NBTTagCompound tag = NBTHelper.getTag(targetStack);
        Arrays.stream(improvements)
            .map(improvement -> slotKey + ":" + improvement)
            .forEach(tag::removeTag);

        return salvage;
    }

    @Override
    public int getIntegrityCost(ItemStack itemStack) {
        return super.getIntegrityCost(itemStack) - getImprovementIntegrityCost(itemStack);
    }

    private int getImprovementIntegrityCost(ItemStack itemStack) {
        return getImprovements(itemStack).length;
    }
}
