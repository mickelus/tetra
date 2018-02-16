package se.mickelus.tetra.items.sword;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.DataHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.module.*;

public class HiltModule extends ItemModuleMajor<WeaponModuleData> {

    public static final String key = "sword/basic_hilt";

    public static HiltModule instance;

    public HiltModule(String slotKey) {
        super(slotKey, key);

        data = DataHandler.instance.getModuleData(key, WeaponModuleData[].class);

        renderLayer = RenderLayer.LOWER;

        instance = this;
        ItemUpgradeRegistry.instance.registerModule(key, this);
    }
}
