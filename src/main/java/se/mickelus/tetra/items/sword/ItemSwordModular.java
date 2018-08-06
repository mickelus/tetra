package se.mickelus.tetra.items.sword;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import se.mickelus.tetra.items.BasicModule;
import se.mickelus.tetra.items.ItemModularHandheld;
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

    public ItemSwordModular() {
        setUnlocalizedName(unlocalizedName);
        setRegistryName(unlocalizedName);
        setMaxStackSize(1);

        blockDestroyDamage = 2;

        majorModuleKeys = new String[]{bladeKey, hiltKey};
        minorModuleKeys = new String[]{fullerKey, guardKey, pommelKey};

        requiredModules = new String[]{bladeKey, hiltKey};

        new BladeModule(bladeKey);
        new ShortBladeModule(bladeKey);
        new HeavyBladeModule(bladeKey);
        new HiltModule(hiltKey);

        new BasicModule(guardKey, "sword/makeshift_guard");
        new BasicModule(guardKey, "sword/wide_guard");
        new BasicModule(pommelKey, "sword/decorative_pommel");
    }

    @Override
    public void init(PacketHandler packetHandler) {
        ItemUpgradeRegistry.instance.registerConfigSchema("sword/basic_blade");
        ItemUpgradeRegistry.instance.registerConfigSchema("sword/basic_blade_improvements");
        new BookEnchantSchema(BladeModule.instance);

        ItemUpgradeRegistry.instance.registerConfigSchema("sword/short_blade");
        ItemUpgradeRegistry.instance.registerConfigSchema("sword/short_blade_improvements");
        new BookEnchantSchema(ShortBladeModule.instance);

        ItemUpgradeRegistry.instance.registerConfigSchema("sword/heavy_blade");
        new BookEnchantSchema(HeavyBladeModule.instance);

        ItemUpgradeRegistry.instance.registerConfigSchema("sword/basic_hilt");
        new BookEnchantSchema(HiltModule.instance);

        ItemUpgradeRegistry.instance.registerConfigSchema("sword/wide_guard");

        new RepairSchema(this);
        new RemoveSchema(this);

        ItemUpgradeRegistry.instance.registerReplacementDefinition("sword");
    }

    @Override
    public boolean canHarvestBlock(IBlockState blockState) {
        return blockState.getBlock() == Blocks.WEB;
    }
}
