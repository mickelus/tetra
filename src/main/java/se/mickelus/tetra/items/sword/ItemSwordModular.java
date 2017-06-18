package se.mickelus.tetra.items.sword;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.registry.GameRegistry;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.items.TetraCreativeTabs;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.network.PacketPipeline;

public class ItemSwordModular extends ItemModular {

    public final static String bladeKey = "sword:blade";
    public final static String hiltKey = "sword:hilt";


    private static final String unlocalizedName = "sword_modular";

    public ItemSwordModular() {
        setUnlocalizedName(unlocalizedName);
        setRegistryName(unlocalizedName);
        GameRegistry.register(this);
        setCreativeTab(TetraCreativeTabs.getInstance());

        majorModuleNames = new String[] {"Blade", "Hilt"};
        majorModuleKeys = new String[] {bladeKey, hiltKey};
        minorModuleNames = new String[] {"Guard", "Pommel", "Fuller"};
        minorModuleKeys = new String[] {"sword:guard", "sword:pommel", "sword:fuller"};

    }

    @Override
    public void init(PacketPipeline packetPipeline) {
        ItemUpgradeRegistry.instance.registerPlaceholder(this::replaceSword);
        new BladeModule();
        new HiltModule();
    }

    private ItemStack replaceSword(ItemStack originalStack) {
        Item originalItem = originalStack.getItem();

        if (!(originalItem instanceof ItemSword)) {
            return null;
        }

        return createItemStack(((ItemSword) originalItem).getToolMaterialName());
    }

    private ItemStack createItemStack(String material) {
        ItemStack itemStack = new ItemStack(this);
        itemStack.setTagCompound(new NBTTagCompound());

        ItemStack bladeMaterial;
        switch (material) {
            case "WOOD":
                bladeMaterial = new ItemStack(Blocks.PLANKS, 2);
                break;
            case "STONE":
                bladeMaterial = new ItemStack(Blocks.COBBLESTONE, 2);
                break;
            case "IRON":
                bladeMaterial = new ItemStack(Items.IRON_INGOT, 2);
                break;
            case "DIAMOND":
                bladeMaterial = new ItemStack(Items.DIAMOND, 2);
                break;
            default:
                bladeMaterial = new ItemStack(Blocks.PLANKS, 2);
                break;
        }

        BladeModule.instance.addModule(itemStack, new ItemStack[]{bladeMaterial});
        HiltModule.instance.addModule(itemStack, new ItemStack[] {new ItemStack(Items.STICK)});
        return itemStack;
    }
}
