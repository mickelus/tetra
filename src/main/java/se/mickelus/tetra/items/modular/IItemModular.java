package se.mickelus.tetra.items.modular;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import se.mickelus.tetra.gui.GuiModuleOffsets;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.data.ModuleModel;

import javax.annotation.Nullable;

public interface IItemModular {

    @OnlyIn(Dist.CLIENT)
    GuiModuleOffsets[] defaultMajorOffsets = {
            new GuiModuleOffsets(),
            new GuiModuleOffsets(4, 0),
            new GuiModuleOffsets(4, 0, 4, 18),
            new GuiModuleOffsets(4, 0, 4, 18, -4, 0),
            new GuiModuleOffsets(4, 0, 4, 18, -4, 0, -4, 18)
    };

    @OnlyIn(Dist.CLIENT)
    GuiModuleOffsets[] defaultMinorOffsets = {
            new GuiModuleOffsets(),
            new GuiModuleOffsets(-21, 12),
            new GuiModuleOffsets(-18, 5, -18, 18),
            new GuiModuleOffsets(-12, -1, -21, 12, -12, 25),
    };

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

    @OnlyIn(Dist.CLIENT)
    public ImmutableList<ModuleModel> getModels(ItemStack itemStack, @Nullable LivingEntity entity);

    @OnlyIn(Dist.CLIENT)
    default public String getTransformVariant(ItemStack itemStack, @Nullable LivingEntity entity) {
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    default public GuiModuleOffsets getMajorGuiOffsets() {
        return defaultMajorOffsets[getNumMajorModules()];
    }

    @OnlyIn(Dist.CLIENT)
    default public GuiModuleOffsets getMinorGuiOffsets() {
        return defaultMinorOffsets[getNumMinorModules()];
    }
}
