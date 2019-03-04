package se.mickelus.tetra.items.sword;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import se.mickelus.tetra.items.BasicMajorModule;
import se.mickelus.tetra.items.BasicModule;
import se.mickelus.tetra.items.ItemModularHandheld;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.schema.*;
import se.mickelus.tetra.network.PacketHandler;

public class ItemSwordModular extends ItemModularHandheld {

    public final static String bladeKey = "sword/blade";
    public final static String hiltKey = "sword/hilt";

    public final static String guardKey = "sword/guard";
    public final static String pommelKey = "sword/pommel";
    public final static String fullerKey = "sword/fuller";


    static final String unlocalizedName = "sword_modular";

    private final ItemModuleMajor basicBladeModule;
    private final ItemModuleMajor shortBladeModule;
    private final ItemModuleMajor heavyBladeModule;
    private final ItemModuleMajor macheteModule;

    public ItemSwordModular() {
        setUnlocalizedName(unlocalizedName);
        setRegistryName(unlocalizedName);
        setMaxStackSize(1);

        blockDestroyDamage = 2;

        majorModuleKeys = new String[] { bladeKey, hiltKey };
        minorModuleKeys = new String[] { fullerKey, guardKey, pommelKey };

        requiredModules = new String[] { bladeKey, hiltKey };

        basicBladeModule = new BasicMajorModule(bladeKey, "sword/basic_blade",
                "sword/improvements/blade_enchants", "sword/improvements/basic_blade");
        shortBladeModule = new BasicMajorModule(bladeKey, "sword/short_blade",
                "sword/improvements/blade_enchants", "sword/improvements/short_blade");
        heavyBladeModule = new BasicMajorModule(bladeKey, "sword/heavy_blade",
                "sword/improvements/blade_enchants", "sword/improvements/heavy_blade");
        macheteModule = new BasicMajorModule(bladeKey, "sword/machete", "sword/improvements/blade_enchants");

        new HiltModule(hiltKey);

        new BasicModule(guardKey, "sword/makeshift_guard");
        new BasicModule(guardKey, "sword/wide_guard");

        new BasicModule(pommelKey, "sword/decorative_pommel");
        new BasicModule(pommelKey, "sword/counterweight");
        new BasicModule(pommelKey, "sword/grip_loop");
        new BasicModule(guardKey, "sword/forefinger_ring");
        new BasicModule(guardKey, "sword/reinforced_bolster");
        new BasicModule(fullerKey, "sword/reinforced_fuller");
    }

    @Override
    public void init(PacketHandler packetHandler) {
        ItemUpgradeRegistry.instance.registerConfigSchema("sword/basic_blade");
        ItemUpgradeRegistry.instance.registerConfigSchema("sword/basic_blade_improvements");
        new BookEnchantSchema(basicBladeModule);

        ItemUpgradeRegistry.instance.registerConfigSchema("sword/short_blade");
        ItemUpgradeRegistry.instance.registerConfigSchema("sword/short_blade_improvements");
        new BookEnchantSchema(shortBladeModule);

        ItemUpgradeRegistry.instance.registerConfigSchema("sword/heavy_blade");
        new BookEnchantSchema(heavyBladeModule);

        ItemUpgradeRegistry.instance.registerConfigSchema("sword/machete");
        new BookEnchantSchema(macheteModule);

        ItemUpgradeRegistry.instance.registerConfigSchema("sword/basic_hilt");
        new BookEnchantSchema(HiltModule.instance);

        ItemUpgradeRegistry.instance.registerConfigSchema("sword/wide_guard");
        ItemUpgradeRegistry.instance.registerConfigSchema("sword/counterweight");
        ItemUpgradeRegistry.instance.registerConfigSchema("sword/grip_loop");
        ItemUpgradeRegistry.instance.registerConfigSchema("sword/forefinger_ring");
        ItemUpgradeRegistry.instance.registerConfigSchema("sword/reinforced_bolster");
        ItemUpgradeRegistry.instance.registerConfigSchema("sword/reinforced_fuller");

        new RepairSchema(this);
        RemoveSchema.registerRemoveSchemas(this);

        ItemUpgradeRegistry.instance.registerReplacementDefinition("sword");
    }

    @Override
    public boolean canHarvestBlock(IBlockState blockState) {
        return blockState.getBlock() == Blocks.WEB;
    }
}
