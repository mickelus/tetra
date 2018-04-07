package se.mickelus.tetra.items.duplex_tool;

import com.google.common.collect.ImmutableList;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import se.mickelus.tetra.DataHandler;
import se.mickelus.tetra.blocks.workbench.BlockWorkbench;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.items.ItemModularHandheld;
import se.mickelus.tetra.items.TetraCreativeTabs;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.schema.BasicSchema;
import se.mickelus.tetra.module.schema.ModuleSlotSchema;
import se.mickelus.tetra.module.schema.RepairSchema;
import se.mickelus.tetra.network.PacketPipeline;


public class ItemDuplexToolModular extends ItemModularHandheld {

    public final static String headLeftKey = "duplex/head_left";
    public final static String headRightKey = "duplex/head_right";

    public final static String handleKey = "duplex/handle";
    public final static String bindingKey = "duplex/binding";
    public final static String accessoryKey = "duplex/accessory";

    public final static String leftSuffix = "_left";
    public final static String rightSuffix = "_right";

    private static final String unlocalizedName = "duplex_tool_modular";

    public static DuplexHeadModule basicHammerHeadLeft;
    public static DuplexHeadModule basicHammerHeadRight;

    public static DuplexHeadModule basicAxeLeft;
    public static DuplexHeadModule basicAxeRight;

    public static DuplexHeadModule basicPickaxeLeft;
    public static DuplexHeadModule basicPickaxeRight;

    public static DuplexHeadModule butt;

    public ItemDuplexToolModular() {

        setUnlocalizedName(unlocalizedName);
        setRegistryName(unlocalizedName);
        setMaxStackSize(1);
        setCreativeTab(TetraCreativeTabs.getInstance());

        majorModuleNames = new String[]{"Head §7(left)", "Head §7(right)"};
        majorModuleKeys = new String[]{headLeftKey,headRightKey, };
        minorModuleNames = new String[]{"Binding", "Handle", "Accessory"};
        minorModuleKeys = new String[]{bindingKey, handleKey, accessoryKey};

        synergies = DataHandler.instance.getSynergyData("modules/duplex/synergies");

        basicHammerHeadLeft = new DuplexHeadModule(headLeftKey, "duplex/basic_hammer", leftSuffix);
        basicHammerHeadRight = new DuplexHeadModule(headRightKey, "duplex/basic_hammer", rightSuffix);

        basicAxeLeft = new DuplexHeadModule(headLeftKey, "duplex/basic_axe", leftSuffix);
        basicAxeRight = new DuplexHeadModule(headRightKey, "duplex/basic_axe", rightSuffix);

        basicPickaxeLeft = new DuplexHeadModule(headLeftKey, "duplex/basic_pickaxe", leftSuffix);
        basicPickaxeRight = new DuplexHeadModule(headRightKey, "duplex/basic_pickaxe", rightSuffix);

        butt = new DuplexHeadModule(headRightKey, "duplex/butt", rightSuffix);

        new BasicHandleModule(handleKey);
    }


    @Override
    public void init(PacketPipeline packetPipeline) {
        new ModuleSlotSchema("basic_hammer_schema", basicHammerHeadLeft, this);
        new ModuleSlotSchema("basic_hammer_schema", basicHammerHeadRight, this);

        new ModuleSlotSchema("basic_axe_schema", basicAxeLeft, this);
        new ModuleSlotSchema("basic_axe_schema", basicAxeRight, this);

        new ModuleSlotSchema("basic_pickaxe_schema", basicPickaxeLeft, this);
        new ModuleSlotSchema("basic_pickaxe_schema", basicPickaxeRight, this);

        new ModuleSlotSchema("butt_schema", butt, this);

        new BasicSchema("basic_handle_schema", BasicHandleModule.instance, this);

        new RepairSchema(this);

        ItemUpgradeRegistry.instance.registerPlaceholder(this::replaceTool);
    }

    private ItemStack replaceTool(ItemStack originalStack) {
        Item originalItem = originalStack.getItem();
        ItemStack newStack = new ItemStack(this);

        if (!(originalItem instanceof ItemAxe || originalItem instanceof ItemPickaxe)) {
            return null;
        }

        ItemStack material = getStackFromMaterialString(((ItemTool) originalItem).getToolMaterialName());

        if (originalItem instanceof ItemAxe) {
            basicAxeLeft.addModule(newStack, new ItemStack[]{material}, false, null);
            butt.addModule(newStack, new ItemStack[]{material}, false, null);
        }

        if (originalItem instanceof ItemPickaxe) {
            basicPickaxeLeft.addModule(newStack, new ItemStack[]{material}, false, null);
            basicPickaxeRight.addModule(newStack, new ItemStack[]{material}, false, null);
        }

        BasicHandleModule.instance.addModule(newStack, new ItemStack[]{new ItemStack(Items.STICK)}, false, null);
        newStack.setItemDamage(originalStack.getItemDamage());

        return newStack;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs creativeTabs, NonNullList<ItemStack> itemList) {
        if (isInCreativeTab(creativeTabs)) {
            itemList.add(createHammerStack(new ItemStack(Blocks.LOG), new ItemStack(Items.STICK)));
            itemList.add(createHammerStack(new ItemStack(Blocks.OBSIDIAN), new ItemStack(Items.IRON_INGOT)));
        }
    }

    private ItemStack createHammerStack(ItemStack headMaterial, ItemStack handleMaterial) {
        ItemStack itemStack = new ItemStack(this);

        basicHammerHeadLeft.addModule(itemStack, new ItemStack[]{headMaterial}, false, null);
        basicHammerHeadRight.addModule(itemStack, new ItemStack[]{headMaterial}, false, null);
        BasicHandleModule.instance.addModule(itemStack, new ItemStack[]{handleMaterial}, false, null);
        return itemStack;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public ImmutableList<ResourceLocation> getTextures(ItemStack itemStack) {
        return super.getTextures(itemStack);
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        if (!player.isSneaking() && world.getBlockState(pos).getBlock().equals(Blocks.CRAFTING_TABLE)
                && getCapabilityLevel(player.getHeldItem(hand), Capability.hammer) > 0) {
            return BlockWorkbench.upgradeWorkbench(player, world, pos, hand, side);
        }
        return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
    }
}


