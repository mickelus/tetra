package se.mickelus.tetra.items.duplex_tool;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import se.mickelus.tetra.data.DataHandler;
import se.mickelus.tetra.blocks.workbench.BlockWorkbench;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.items.ItemModularHandheld;
import se.mickelus.tetra.items.TetraCreativeTabs;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.schema.BookEnchantSchema;
import se.mickelus.tetra.module.schema.RemoveSchema;
import se.mickelus.tetra.module.schema.RepairSchema;
import se.mickelus.tetra.network.PacketHandler;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;


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

    public static DuplexHeadModule hoeLeft;
    public static DuplexHeadModule hoeRight;

    public static DuplexHeadModule adzeLeft;
    public static DuplexHeadModule adzeRight;

    public static DuplexHeadModule sickleLeft;
    public static DuplexHeadModule sickleRight;

    public static DuplexHeadModule butt;

    public static ItemDuplexToolModular instance;

    public ItemDuplexToolModular() {

        setUnlocalizedName(unlocalizedName);
        setRegistryName(unlocalizedName);
        setMaxStackSize(1);
        setCreativeTab(TetraCreativeTabs.getInstance());

        entityHitDamage = 2;

        majorModuleKeys = new String[]{headLeftKey, headRightKey};
        minorModuleKeys = new String[]{bindingKey, handleKey, accessoryKey};

        requiredModules = new String[]{headLeftKey, headRightKey, handleKey};

        synergies = DataHandler.instance.getSynergyData("modules/duplex/synergies");

        basicHammerHeadLeft = new DuplexHeadModule(headLeftKey, "basic_hammer", leftSuffix);
        basicHammerHeadRight = new DuplexHeadModule(headRightKey, "basic_hammer", rightSuffix);

        basicAxeLeft = new DuplexHeadModule(headLeftKey, "basic_axe", leftSuffix);
        basicAxeRight = new DuplexHeadModule(headRightKey, "basic_axe", rightSuffix);

        basicPickaxeLeft = new DuplexHeadModule(headLeftKey, "basic_pickaxe", leftSuffix);
        basicPickaxeRight = new DuplexHeadModule(headRightKey, "basic_pickaxe", rightSuffix);

        hoeLeft = new DuplexHeadModule(headLeftKey, "hoe", leftSuffix);
        hoeRight = new DuplexHeadModule(headRightKey, "hoe", rightSuffix);

        adzeLeft = new DuplexHeadModule(headLeftKey, "adze", leftSuffix);
        adzeRight = new DuplexHeadModule(headRightKey, "adze", rightSuffix);

        sickleLeft = new DuplexHeadModule(headLeftKey, "sickle", leftSuffix);
        sickleRight = new DuplexHeadModule(headRightKey, "sickle", rightSuffix);

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

        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/hoe");
        new BookEnchantSchema(hoeLeft);
        new BookEnchantSchema(hoeRight);

        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/adze");
        new BookEnchantSchema(adzeLeft);
        new BookEnchantSchema(adzeRight);

        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/sickle");
        new BookEnchantSchema(sickleLeft);
        new BookEnchantSchema(sickleRight);

        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/butt");
        new BookEnchantSchema(butt);

        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/basic_handle");

        new RepairSchema(this);
        RemoveSchema.registerRemoveSchemas(this);

        ItemUpgradeRegistry.instance.registerReplacementDefinition("axe");
        ItemUpgradeRegistry.instance.registerReplacementDefinition("pickaxe");
        ItemUpgradeRegistry.instance.registerReplacementDefinition("hoe");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs creativeTabs, NonNullList<ItemStack> itemList) {
        if (isInCreativeTab(creativeTabs)) {
            itemList.add(createHammerStack("log", "stick"));
            itemList.add(createHammerStack("obsidian", "iron"));
        }
    }

    public ItemStack createHammerStack(String headMaterial, String handleMaterial) {
        ItemStack itemStack = new ItemStack(this);

        basicHammerHeadLeft.addModule(itemStack, "basic_hammer/" + headMaterial, null);
        basicHammerHeadRight.addModule(itemStack, "basic_hammer/" + headMaterial, null);
        BasicHandleModule.instance.addModule(itemStack, "basic_handle/" + handleMaterial, null);
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

    @Override
    public double getDamageModifier(ItemStack itemStack) {
        if (isBroken(itemStack)) {
            return 0;
        }

        // only use the damage from the highest damaging head
        double damageModifier = Stream.of(getModuleFromSlot(itemStack, headLeftKey), getModuleFromSlot(itemStack, headRightKey))
                .filter(Objects::nonNull)
                .mapToDouble(module -> module.getDamageModifier(itemStack))
                .max()
                .orElse(0);

        damageModifier = getAllModules(itemStack).stream()
                .filter(itemModule -> !(headLeftKey.equals(itemModule.getSlot()) || headRightKey.equals(itemModule.getSlot())))
                .map(itemModule -> itemModule.getDamageModifier(itemStack))
                .reduce(damageModifier, Double::sum);

        damageModifier = Arrays.stream(getSynergyData(itemStack))
                .mapToDouble(synergyData -> synergyData.damage)
                .reduce(damageModifier, Double::sum);

        damageModifier = Arrays.stream(getSynergyData(itemStack))
                .mapToDouble(synergyData -> synergyData.damageMultiplier)
                .reduce(damageModifier, (a, b) -> a * b);

        return getAllModules(itemStack).stream()
                .map(itemModule -> itemModule.getDamageMultiplierModifier(itemStack))
                .reduce(damageModifier, (a, b) -> a * b);
    }
}


