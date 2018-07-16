package se.mickelus.tetra.items.sword;

import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import se.mickelus.tetra.items.ItemModularHandheld;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.schema.*;
import se.mickelus.tetra.network.PacketHandler;

import java.util.Map;

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

        majorModuleNames = new String[]{"Blade", "Hilt"};
        majorModuleKeys = new String[]{bladeKey, hiltKey};
        minorModuleNames = new String[]{"Fuller", "Guard", "Pommel"};
        minorModuleKeys = new String[]{fullerKey, guardKey, pommelKey};

        new BladeModule(bladeKey);
        new ShortBladeModule(bladeKey);
        new HeavyBladeModule(bladeKey);
        new HiltModule(hiltKey);

        new MakeshiftGuardModule(guardKey);
        new DecorativePommelModule(pommelKey);
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

        new RepairSchema(this);

        ItemUpgradeRegistry.instance.registerReplacementDefinition("sword");
    }

    @Override
    public boolean canHarvestBlock(IBlockState blockState) {
        return blockState.getBlock() == Blocks.WEB;
    }
}
