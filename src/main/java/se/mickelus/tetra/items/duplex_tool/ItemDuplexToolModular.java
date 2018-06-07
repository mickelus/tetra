package se.mickelus.tetra.items.duplex_tool;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
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
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.schema.BookEnchantSchema;
import se.mickelus.tetra.module.schema.RepairSchema;
import se.mickelus.tetra.network.PacketHandler;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;


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

    public static ItemDuplexToolModular instance;

    public ItemDuplexToolModular() {

        setUnlocalizedName(unlocalizedName);
        setRegistryName(unlocalizedName);
        setMaxStackSize(1);
        setCreativeTab(TetraCreativeTabs.getInstance());

        majorModuleNames = new String[]{"Head §7(left)", "Head §7(right)"};
        majorModuleKeys = new String[]{headLeftKey,headRightKey};
        minorModuleNames = new String[]{"Binding", "Handle", "Accessory"};
        minorModuleKeys = new String[]{bindingKey, handleKey, accessoryKey};

        synergies = DataHandler.instance.getSynergyData("modules/duplex/synergies");

        basicHammerHeadLeft = new DuplexHeadModule(headLeftKey, "basic_hammer", leftSuffix);
        basicHammerHeadRight = new DuplexHeadModule(headRightKey, "basic_hammer", rightSuffix);

        basicAxeLeft = new DuplexHeadModule(headLeftKey, "basic_axe", leftSuffix);
        basicAxeRight = new DuplexHeadModule(headRightKey, "basic_axe", rightSuffix);

        basicPickaxeLeft = new DuplexHeadModule(headLeftKey, "basic_pickaxe", leftSuffix);
        basicPickaxeRight = new DuplexHeadModule(headRightKey, "basic_pickaxe", rightSuffix);

        butt = new DuplexHeadModule(headRightKey, "butt", rightSuffix);

        new BasicHandleModule(handleKey);

        instance = this;
    }


    @Override
    public void init(PacketHandler packetHandler) {
        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/basic_hammer");
        new BookEnchantSchema(basicHammerHeadLeft);
        new BookEnchantSchema(basicHammerHeadRight);

        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/basic_axe");
        new BookEnchantSchema(basicAxeLeft);
        new BookEnchantSchema(basicAxeRight);

        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/basic_pickaxe");
        new BookEnchantSchema(basicPickaxeLeft);
        new BookEnchantSchema(basicPickaxeRight);

        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/butt");
        new BookEnchantSchema(butt);

        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/basic_handle");

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
            transferAxeEnchantments(originalStack, newStack);
        }

        if (originalItem instanceof ItemPickaxe) {
            basicPickaxeLeft.addModule(newStack, new ItemStack[]{material}, false, null);
            basicPickaxeRight.addModule(newStack, new ItemStack[]{material}, false, null);
            transferPickaxeEnchantments(originalStack, newStack);
        }

        BasicHandleModule.instance.addModule(newStack, new ItemStack[]{new ItemStack(Items.STICK)}, false, null);
        newStack.setItemDamage(originalStack.getItemDamage());

        return newStack;
    }

    private void transferPickaxeEnchantments(ItemStack sourceStack, ItemStack modularStack) {
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(sourceStack);
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            String improvement = ItemUpgradeRegistry.instance.getImprovementFromEnchantment(entry.getKey());
            if (basicPickaxeLeft.acceptsImprovement(improvement)) {
                if (entry.getKey().equals(Enchantments.EFFICIENCY)) {
                    basicPickaxeLeft.addImprovement(modularStack, improvement, entry.getValue());
                    basicPickaxeRight.addImprovement(modularStack, improvement, entry.getValue());
                } else {
                    basicPickaxeLeft.addImprovement(modularStack, improvement, (int) Math.ceil(entry.getValue() / 2f));
                    basicPickaxeRight.addImprovement(modularStack, improvement, (int) Math.floor(entry.getValue() / 2f));
                }
            }
        }
    }

    private void transferAxeEnchantments(ItemStack sourceStack, ItemStack modularStack) {
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(sourceStack);
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            String improvement = ItemUpgradeRegistry.instance.getImprovementFromEnchantment(entry.getKey());
            if (basicAxeLeft.acceptsImprovement(improvement)) {
                if (butt.acceptsImprovement(improvement)) {
                    basicAxeLeft.addImprovement(modularStack, improvement, (int) Math.floor(entry.getValue() / 2f));
                    butt.addImprovement(modularStack, improvement, (int) Math.ceil(entry.getValue() / 2f));
                } else {
                    basicAxeLeft.addImprovement(modularStack, improvement, entry.getValue());
                }
            } else if (butt.acceptsImprovement(improvement)) {
                butt.addImprovement(modularStack, improvement, entry.getValue());
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs creativeTabs, NonNullList<ItemStack> itemList) {
        if (isInCreativeTab(creativeTabs)) {
            itemList.add(createHammerStack(new ItemStack(Blocks.LOG), new ItemStack(Items.STICK)));
            itemList.add(createHammerStack(new ItemStack(Blocks.OBSIDIAN), new ItemStack(Items.IRON_INGOT)));
        }
    }

    public ItemStack createHammerStack(ItemStack headMaterial, ItemStack handleMaterial) {
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

    @Override
    protected String getDisplayNamePrefixes(ItemStack itemStack) {
        String modulePrefix = Optional.ofNullable(getModuleFromSlot(itemStack, headLeftKey))
                .map(module -> module.getItemPrefix(itemStack))
                .map(prefix -> prefix + " ")
                .orElse("");
        return Arrays.stream(getImprovements(itemStack))
                .map(improvement -> improvement.key + ".prefix")
                .filter(I18n::hasKey)
                .map(I18n::format)
                .findFirst()
                .map(prefix -> prefix + " " + modulePrefix)
                .orElse(modulePrefix);
    }
}


