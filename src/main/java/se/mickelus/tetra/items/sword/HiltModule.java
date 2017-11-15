package se.mickelus.tetra.items.sword;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.DataHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.module.*;

public class HiltModule extends ItemModuleMajor<WeaponModuleData> {

    public static final String key = "basic_hilt";
    public static final String materialKey = "basic_hilt_material";

    public static HiltModule instance;

    public HiltModule(String slotKey) {
        super(slotKey, key, materialKey);

        data = DataHandler.instance.getModuleData(key, WeaponModuleData[].class);

        renderLayer = RenderLayer.LOWER;

        instance = this;
        ItemUpgradeRegistry.instance.registerModule(key, this);
    }
    @Override
    public ItemStack[] removeModule(ItemStack targetStack, ItemStack[] tools) {
        return new ItemStack[0];
    }
}
