package se.mickelus.tetra.items.sword;

import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.ItemModularHandheld;
import se.mickelus.tetra.module.SchemaRegistry;
import se.mickelus.tetra.module.schema.RemoveSchema;
import se.mickelus.tetra.module.schema.RepairSchema;

public class ItemSwordModular extends ItemModularHandheld {

    public final static String bladeKey = "sword/blade";
    public final static String hiltKey = "sword/hilt";

    public final static String guardKey = "sword/guard";
    public final static String pommelKey = "sword/pommel";
    public final static String fullerKey = "sword/fuller";

    public static final String unlocalizedName = "sword_modular";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ItemSwordModular instance;

    public ItemSwordModular() {
        super(new Properties().maxStackSize(1));
        setRegistryName(unlocalizedName);

        blockDestroyDamage = 2;

        majorModuleKeys = new String[] { bladeKey, hiltKey };
        minorModuleKeys = new String[] { fullerKey, guardKey, pommelKey };

        requiredModules = new String[] { bladeKey, hiltKey };

        updateConfig(ConfigHandler.honeSwordBase.get(), ConfigHandler.honeSwordIntegrityMultiplier.get());

        SchemaRegistry.instance.registerSchema(new RepairSchema(this));
        RemoveSchema.registerRemoveSchemas(this);
    }

    public void updateConfig(int honeBase, int honeIntegrityMultiplier) {
        this.honeBase = honeBase;
        this.honeIntegrityMultiplier = honeIntegrityMultiplier;
    }
}
