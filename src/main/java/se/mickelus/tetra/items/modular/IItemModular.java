package se.mickelus.tetra.items.modular;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.data.ModuleModel;

import javax.annotation.Nullable;

public interface IItemModular {

    public boolean isModuleRequired(String moduleSlot);
    public int getNumMajorModules();
    public String[] getMajorModuleKeys();
    public String[] getMajorModuleNames();
    public ItemModuleMajor[] getMajorModules(ItemStack itemStack);

    public int getNumMinorModules();
    public String[] getMinorModuleKeys();
    public String[] getMinorModuleNames();
    public ItemModule[] getMinorModules(ItemStack itemStack);

    public ItemStack getDefaultStack();

    /**
     * Resets and applies effects for the current setup of modules & improvements. Applies enchantments and other things which cannot be emulated
     * through other means. Call this after each time the module setup changes.
     * @param itemStack The modular item itemstack
     * @param severity
     */
    public void assemble(ItemStack itemStack, World world, float severity);

    public ImmutableList<ModuleModel> getModels(ItemStack itemStack, @Nullable LivingEntity entity);

    default public String getTransformVariant(ItemStack itemStack, @Nullable LivingEntity entity) {
        return null;
    }
}
