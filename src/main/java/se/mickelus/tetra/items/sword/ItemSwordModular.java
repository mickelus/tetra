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


        updateConfig(ConfigHandler.honeSwordBase, ConfigHandler.honeSwordIntegrityMultiplier);
    }

    @Override
    public void clientPreInit() {

        ModularModelLoader.registerItem(this);
    }

    @Override
    public void init(PacketHandler packetHandler) {majorModuleKeys = new String[] { bladeKey, hiltKey };
        minorModuleKeys = new String[] { fullerKey, guardKey, pommelKey };

        requiredModules = new String[] { bladeKey, hiltKey };

        setupModules();
        setupSchemas();

        ItemUpgradeRegistry.instance.registerReplacementDefinition("sword");
    }

    private void setupModules() {
        basicBladeModule = new BasicMajorModule(bladeKey, "sword/basic_blade",
                "sword/improvements/shared_blade", "sword/improvements/shared_blade_hone", "sword/improvements/basic_blade",
                "settling_improvements");
        shortBladeModule = new BasicMajorModule(bladeKey, "sword/short_blade",
                "sword/improvements/shared_blade", "sword/improvements/shared_blade_hone", "sword/improvements/short_blade",
                "settling_improvements");
        heavyBladeModule = new BasicMajorModule(bladeKey, "sword/heavy_blade",
                "sword/improvements/shared_blade", "sword/improvements/shared_blade_hone", "sword/improvements/heavy_blade", "settling_improvements");
        macheteModule = new BasicMajorModule(bladeKey, "sword/machete", "sword/improvements/shared_blade", "sword/improvements/shared_blade_hone",
                "settling_improvements");

        hiltModule = new BasicMajorModule(hiltKey, "sword/basic_hilt", "sword/improvements/shared_hilt", "sword/improvements/shared_hilt_hone",
                "settling_improvements")
                .withRenderLayer(Priority.LOWER);

        new BasicModule(guardKey, "sword/makeshift_guard");
        //        new BasicModule(guardKey, "sword/wide_guard");
        //        new BasicModule(guardKey, "sword/forefinger_ring");
        //        new BasicModule(guardKey, "sword/binding", "sword/tweaks/binding");
        //        new BasicModule(guardKey, "sword/socket");

        new BasicModule(pommelKey, "sword/decorative_pommel");
        //        new BasicModule(pommelKey, "sword/counterweight");
        //        new BasicModule(pommelKey, "sword/grip_loop");
        //
        //        new BasicModule(fullerKey, "sword/reinforced_fuller");
    }

    private void setupSchemas() {
        ItemUpgradeRegistry.instance.registerConfigSchema("sword/basic_blade");
        ItemUpgradeRegistry.instance.registerConfigSchema("sword/basic_blade_improvements");
        new BookEnchantSchema(basicBladeModule);

        ItemUpgradeRegistry.instance.registerConfigSchema("sword/short_blade");
        ItemUpgradeRegistry.instance.registerConfigSchema("sword/short_blade_improvements");
        new BookEnchantSchema(shortBladeModule);
//
        ItemUpgradeRegistry.instance.registerConfigSchema("sword/heavy_blade");
        new BookEnchantSchema(heavyBladeModule);

        ItemUpgradeRegistry.instance.registerConfigSchema("sword/machete");
        new BookEnchantSchema(macheteModule);

        ItemUpgradeRegistry.instance.registerConfigSchema("sword/basic_hilt");
        ItemUpgradeRegistry.instance.registerConfigSchema("sword/basic_hilt_improvements");
        new BookEnchantSchema(hiltModule);

//        ItemUpgradeRegistry.instance.registerConfigSchema("sword/wide_guard");
//        ItemUpgradeRegistry.instance.registerConfigSchema("sword/counterweight");
//        ItemUpgradeRegistry.instance.registerConfigSchema("sword/grip_loop");
//        ItemUpgradeRegistry.instance.registerConfigSchema("sword/forefinger_ring");
//        ItemUpgradeRegistry.instance.registerConfigSchema("sword/binding");
//        ItemUpgradeRegistry.instance.registerConfigSchema("sword/reinforced_fuller");

        ItemUpgradeRegistry.instance.registerConfigSchema("sword/shared_blade_hone");
        ItemUpgradeRegistry.instance.registerConfigSchema("sword/shared_hilt_hone");

        ItemUpgradeRegistry.instance.registerConfigSchema("sword/socket");

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
