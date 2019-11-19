package se.mickelus.tetra.items.sword;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.client.model.ModularModelLoader;
import se.mickelus.tetra.items.BasicMajorModule;
import se.mickelus.tetra.items.BasicModule;
import se.mickelus.tetra.items.ItemModularHandheld;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.Priority;
import se.mickelus.tetra.module.schema.BookEnchantSchema;
import se.mickelus.tetra.module.schema.RemoveSchema;
import se.mickelus.tetra.module.schema.RepairSchema;
import se.mickelus.tetra.network.PacketHandler;

public class ItemSwordModular extends ItemModularHandheld {

    public final static String bladeKey = "sword/blade";
    public final static String hiltKey = "sword/hilt";

    public final static String guardKey = "sword/guard";
    public final static String pommelKey = "sword/pommel";
    public final static String fullerKey = "sword/fuller";


    public static final String unlocalizedName = "sword_modular";

    private ItemModuleMajor basicBladeModule;
    private ItemModuleMajor shortBladeModule;
    private ItemModuleMajor heavyBladeModule;
    private ItemModuleMajor macheteModule;
    private ItemModuleMajor hiltModule;

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ItemSwordModular instance;

    public ItemSwordModular() {
        super(new Properties().maxStackSize(1));
        setRegistryName(unlocalizedName);

        blockDestroyDamage = 2;

        majorModuleKeys = new String[] { bladeKey, hiltKey };
        minorModuleKeys = new String[] { fullerKey, guardKey, pommelKey };

        requiredModules = new String[] { bladeKey, hiltKey };

        updateConfig(ConfigHandler.honeSwordBase, ConfigHandler.honeSwordIntegrityMultiplier);
    }

    @Override
    public void init(PacketHandler packetHandler) {
        basicBladeModule = new BasicMajorModule(bladeKey, "sword/basic_blade",
                "sword/shared_blade", "sword/shared_blade_hone", "sword/basic_blade",
                "settling_improvements");
        new BookEnchantSchema(basicBladeModule);

        shortBladeModule = new BasicMajorModule(bladeKey, "sword/short_blade",
                "sword/shared_blade", "sword/shared_blade_hone", "sword/short_blade",
                "settling_improvements");
        new BookEnchantSchema(shortBladeModule);

        heavyBladeModule = new BasicMajorModule(bladeKey, "sword/heavy_blade",
                "sword/shared_blade", "sword/shared_blade_hone", "sword/heavy_blade", "settling_improvements");
        new BookEnchantSchema(heavyBladeModule);

        macheteModule = new BasicMajorModule(bladeKey, "sword/machete", "sword/shared_blade", "sword/shared_blade_hone",
                "settling_improvements");
        new BookEnchantSchema(macheteModule);

        hiltModule = new BasicMajorModule(hiltKey, "sword/basic_hilt", "sword/shared_hilt", "sword/shared_hilt_hone",
                "settling_improvements")
                .withRenderLayer(Priority.LOWER);
        new BookEnchantSchema(hiltModule);

        new BasicModule(guardKey, "sword/makeshift_guard");

        new BasicModule(guardKey, "sword/wide_guard");

        new BasicModule(guardKey, "sword/forefinger_ring");

        new BasicModule(guardKey, "sword/binding", "sword/binding");

        new BasicModule(guardKey, "sword/socket");

        new BasicModule(pommelKey, "sword/decorative_pommel");
        new BasicModule(pommelKey, "sword/counterweight");

        new BasicModule(pommelKey, "sword/grip_loop");

        new BasicModule(fullerKey, "sword/reinforced_fuller");

        new RepairSchema(this);
        RemoveSchema.registerRemoveSchemas(this);
    }

    public void updateConfig(int honeBase, int honeIntegrityMultiplier) {
        this.honeBase = honeBase;
        this.honeIntegrityMultiplier = honeIntegrityMultiplier;
    }

    @Override
    public boolean canHarvestBlock(BlockState blockState) {
        return blockState.getBlock() == Blocks.COBWEB;
    }
}
