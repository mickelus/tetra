package se.mickelus.tetra.items.sword;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import se.mickelus.tetra.items.ItemModularHandheld;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.schema.BasicSchema;
import se.mickelus.tetra.module.schema.ImprovementSchema;
import se.mickelus.tetra.module.schema.RepairSchema;
import se.mickelus.tetra.network.PacketPipeline;

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
    public void init(PacketPipeline packetPipeline) {
        new BasicSchema("blade_schema", BladeModule.instance, this);

        new BasicSchema("short_blade_schema", ShortBladeModule.instance, this);
        new ImprovementSchema(ShortBladeModule.instance, ShortBladeModule.hookedImprovement);
        new ImprovementSchema(ShortBladeModule.instance, ShortBladeModule.temperedImprovement);
        new ImprovementSchema(ShortBladeModule.instance, ShortBladeModule.serratedImprovement);

        new BasicSchema("heavy_blade_schema", HeavyBladeModule.instance, this);
        new BasicSchema("hilt_schema", HiltModule.instance, this);

        new RepairSchema(this);

        ItemUpgradeRegistry.instance.registerPlaceholder(this::replaceSword);
    }

    private ItemStack replaceSword(ItemStack originalStack) {
        Item originalItem = originalStack.getItem();
        ItemStack newStack;

        if (!(originalItem instanceof ItemSword)) {
            return null;
        }

        newStack = createItemStack(((ItemSword) originalItem).getToolMaterialName());
        newStack.setItemDamage(originalStack.getItemDamage());

        return newStack;
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

        BladeModule.instance.addModule(itemStack, new ItemStack[] {bladeMaterial}, false, null);
        HiltModule.instance.addModule(itemStack, new ItemStack[] {new ItemStack(Items.STICK)}, false, null);
        MakeshiftGuardModule.instance.addModule(itemStack, new ItemStack[] {bladeMaterial}, false, null);
        DecorativePommelModule.instance.addModule(itemStack, new ItemStack[] {bladeMaterial}, false, null);
        return itemStack;
    }

    @Override
    public boolean onBlockDestroyed(ItemStack itemStack, World worldIn, IBlockState state, BlockPos pos, EntityLivingBase entityLiving) {
        if (state.getBlockHardness(worldIn, pos) > 0) {
            applyDamage(2, itemStack, entityLiving);
        }

        return true;
    }
}
