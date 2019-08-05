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
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.data.DataHandler;
import se.mickelus.tetra.blocks.workbench.BlockWorkbench;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.items.BasicMajorModule;
import se.mickelus.tetra.items.BasicModule;
import se.mickelus.tetra.items.ItemModularHandheld;
import se.mickelus.tetra.items.TetraCreativeTabs;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.Priority;
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

    public static DuplexHeadModule clawLeft;
    public static DuplexHeadModule clawRight;

    public static DuplexHeadModule butt;

    public static BasicMajorModule handle;

    @GameRegistry.ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ItemDuplexToolModular instance;

    public ItemDuplexToolModular() {

        setUnlocalizedName(unlocalizedName);
        setRegistryName(unlocalizedName);
        setMaxStackSize(1);
        setCreativeTab(TetraCreativeTabs.getInstance());

        entityHitDamage = 2;

        majorModuleKeys = new String[] { headLeftKey, headRightKey, handleKey };
        minorModuleKeys = new String[] { bindingKey };

        requiredModules = new String[] { handleKey, headLeftKey, headRightKey };

        synergies = DataHandler.instance.getSynergyData("modules/duplex/synergies");

        basicHammerHeadLeft = new DuplexHeadModule(headLeftKey, "basic_hammer", leftSuffix,
                "duplex/improvements/basic_hammer", "duplex/improvements/shared_head_hone", "settling_improvements");
        basicHammerHeadRight = new DuplexHeadModule(headRightKey, "basic_hammer", rightSuffix,
                "duplex/improvements/basic_hammer", "duplex/improvements/shared_head_hone", "settling_improvements");

        basicAxeLeft = new DuplexHeadModule(headLeftKey, "basic_axe", leftSuffix,
                "duplex/improvements/basic_axe", "duplex/improvements/basic_axe_hone", "duplex/improvements/shared_head_hone", "settling_improvements");
        basicAxeRight = new DuplexHeadModule(headRightKey, "basic_axe", rightSuffix,
                "duplex/improvements/basic_axe", "duplex/improvements/basic_axe_hone", "duplex/improvements/shared_head_hone", "settling_improvements");

        basicPickaxeLeft = new DuplexHeadModule(headLeftKey, "basic_pickaxe", leftSuffix,
                "duplex/improvements/basic_pickaxe", "duplex/improvements/basic_pickaxe_hone", "duplex/improvements/shared_head_hone", "settling_improvements");
        basicPickaxeRight = new DuplexHeadModule(headRightKey, "basic_pickaxe", rightSuffix,
                "duplex/improvements/basic_pickaxe", "duplex/improvements/basic_pickaxe_hone", "duplex/improvements/shared_head_hone", "settling_improvements");

        hoeLeft = new DuplexHeadModule(headLeftKey, "hoe", leftSuffix,
                "duplex/improvements/hoe", "duplex/improvements/hoe_hone", "duplex/improvements/shared_head_hone", "settling_improvements");
        hoeRight = new DuplexHeadModule(headRightKey, "hoe", rightSuffix,
                "duplex/improvements/hoe", "duplex/improvements/hoe_hone", "duplex/improvements/shared_head_hone", "settling_improvements");

        adzeLeft = new DuplexHeadModule(headLeftKey, "adze", leftSuffix,
                "duplex/improvements/adze", "duplex/improvements/adze_hone", "duplex/improvements/shared_head_hone", "settling_improvements");
        adzeRight = new DuplexHeadModule(headRightKey, "adze", rightSuffix,
                "duplex/improvements/adze", "duplex/improvements/adze_hone", "duplex/improvements/shared_head_hone", "settling_improvements");

        sickleLeft = new DuplexHeadModule(headLeftKey, "sickle", leftSuffix,
                "duplex/improvements/sickle", "duplex/improvements/sickle_hone", "duplex/improvements/shared_head_hone", "settling_improvements");
        sickleRight = new DuplexHeadModule(headRightKey, "sickle", rightSuffix,
                "duplex/improvements/sickle", "duplex/improvements/sickle_hone", "duplex/improvements/shared_head_hone", "settling_improvements");

        clawLeft = new DuplexHeadModule(headLeftKey, "claw", leftSuffix,
                "duplex/improvements/claw", "duplex/improvements/claw_hone", "duplex/improvements/shared_head_hone", "settling_improvements");
        clawRight = new DuplexHeadModule(headRightKey, "claw", rightSuffix,
                "duplex/improvements/claw", "duplex/improvements/claw_hone", "duplex/improvements/shared_head_hone", "settling_improvements");

        butt = new DuplexHeadModule(headRightKey, "butt", rightSuffix,
                "duplex/improvements/butt", "duplex/improvements/butt_hone", "duplex/improvements/shared_head_hone", "settling_improvements");

        handle = new BasicMajorModule(handleKey, "duplex/basic_handle",
                "duplex/improvements/basic_handle", "duplex/improvements/basic_handle_hone", "duplex/improvements/shared_head_hone", "settling_improvements")
                .withRenderLayer(Priority.LOWER);


        new BasicModule(bindingKey, "duplex/socket");

        updateConfig(ConfigHandler.honeDuplexBase, ConfigHandler.honeDuplexIntegrityMultiplier);
    }

    public void updateConfig(int honeBase, int honeIntegrityMultiplier) {
        this.honeBase = honeBase;
        this.honeIntegrityMultiplier = honeIntegrityMultiplier;
    }

    @Override
    public void init(PacketHandler packetHandler) {
        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/basic_hammer");
        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/basic_hammer_hone");
        new BookEnchantSchema(basicHammerHeadLeft);
        new BookEnchantSchema(basicHammerHeadRight);

        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/basic_axe");
        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/basic_axe_hone");
        new BookEnchantSchema(basicAxeLeft);
        new BookEnchantSchema(basicAxeRight);

        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/basic_pickaxe");
        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/basic_pickaxe_hone");
        new BookEnchantSchema(basicPickaxeLeft);
        new BookEnchantSchema(basicPickaxeRight);

        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/hoe");
        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/hoe_hone");
        new BookEnchantSchema(hoeLeft);
        new BookEnchantSchema(hoeRight);

        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/adze");
        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/adze_hone");
        new BookEnchantSchema(adzeLeft);
        new BookEnchantSchema(adzeRight);

        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/sickle");
        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/sickle_hone");
        new BookEnchantSchema(sickleLeft);
        new BookEnchantSchema(sickleRight);

        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/claw");
        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/claw_hone");
        new BookEnchantSchema(clawLeft);
        new BookEnchantSchema(clawRight);

        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/butt");
        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/butt_hone");
        new BookEnchantSchema(butt);

        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/basic_handle");
        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/basic_handle_hone");
        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/basic_handle_improvements");
        new BookEnchantSchema(handle);

        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/shared_head_hone");

        new RepairSchema(this);
        RemoveSchema.registerRemoveSchemas(this);

        ItemUpgradeRegistry.instance.registerReplacementDefinition("axe");
        ItemUpgradeRegistry.instance.registerReplacementDefinition("pickaxe");
        ItemUpgradeRegistry.instance.registerReplacementDefinition("hoe");

        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/socket");
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
        handle.addModule(itemStack, "basic_handle/" + handleMaterial, null);
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


