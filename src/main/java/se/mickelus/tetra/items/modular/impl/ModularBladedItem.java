package se.mickelus.tetra.items.modular.impl;

import net.minecraft.item.Item;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.module.SchematicRegistry;
import se.mickelus.tetra.module.schematic.RemoveSchematic;
import se.mickelus.tetra.module.schematic.RepairSchematic;

public class ModularBladedItem extends ItemModularHandheld {

    public final static String bladeKey = "sword/blade";
    public final static String hiltKey = "sword/hilt";

    public final static String guardKey = "sword/guard";
    public final static String pommelKey = "sword/pommel";
    public final static String fullerKey = "sword/fuller";

    public static final String unlocalizedName = "modular_sword";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ModularBladedItem instance;

    public ModularBladedItem() {
        super(new Item.Properties().maxStackSize(1));
        setRegistryName(unlocalizedName);

        blockDestroyDamage = 2;

        majorModuleKeys = new String[] { bladeKey, hiltKey };
        minorModuleKeys = new String[] { fullerKey, guardKey, pommelKey };

        requiredModules = new String[] { bladeKey, hiltKey };

        updateConfig(ConfigHandler.honeSwordBase.get(), ConfigHandler.honeSwordIntegrityMultiplier.get());

        SchematicRegistry.instance.registerSchematic(new RepairSchematic(this));
        RemoveSchematic.registerRemoveSchematics(this);
    }

    public void updateConfig(int honeBase, int honeIntegrityMultiplier) {
        this.honeBase = honeBase;
        this.honeIntegrityMultiplier = honeIntegrityMultiplier;
    }
}
