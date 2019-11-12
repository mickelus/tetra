package se.mickelus.tetra.items.duplex_tool;

import net.minecraft.block.Blocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.workbench.WorkbenchBlock;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.data.DataHandler;
import se.mickelus.tetra.items.BasicMajorModule;
import se.mickelus.tetra.items.ItemModularHandheld;
import se.mickelus.tetra.items.TetraItemGroup;
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

    // hacky, but the creative tabs are filled before init so we need to setup itemstacks and populate them with modules later
    private ItemStack creativeWoodHammer;
    private ItemStack creativeObsidianHammer;

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ItemDuplexToolModular instance;

    public ItemDuplexToolModular() {
        super(new Properties().maxStackSize(1).group(TetraItemGroup.instance));
        setRegistryName(unlocalizedName);

        entityHitDamage = 2;

        majorModuleKeys = new String[] { headLeftKey, headRightKey, handleKey };
        minorModuleKeys = new String[] { bindingKey };

        requiredModules = new String[] { handleKey, headLeftKey, headRightKey };

        creativeWoodHammer = new ItemStack(this);
        creativeObsidianHammer = new ItemStack(this);

        updateConfig(ConfigHandler.honeDuplexBase, ConfigHandler.honeDuplexIntegrityMultiplier);
    }

    public void updateConfig(int honeBase, int honeIntegrityMultiplier) {
        this.honeBase = honeBase;
        this.honeIntegrityMultiplier = honeIntegrityMultiplier;
    }

    @Override
    public void init(PacketHandler packetHandler) {
        synergies = DataHandler.instance.getSynergyData("modules/duplex/synergies");

        basicHammerHeadLeft = new DuplexHeadModule(headLeftKey, "basic_hammer", leftSuffix,
                "duplex/improvements/basic_hammer", "duplex/improvements/shared_head_hone", "settling_improvements");
        basicHammerHeadRight = new DuplexHeadModule(headRightKey, "basic_hammer", rightSuffix,
                "duplex/improvements/basic_hammer", "duplex/improvements/shared_head_hone", "settling_improvements");
        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/basic_hammer");
        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/basic_hammer_hone");
        new BookEnchantSchema(basicHammerHeadLeft);
        new BookEnchantSchema(basicHammerHeadRight);

//        basicAxeLeft = new DuplexHeadModule(headLeftKey, "basic_axe", leftSuffix,
//                "duplex/improvements/basic_axe", "duplex/improvements/basic_axe_hone", "duplex/improvements/shared_head_hone",
//                "settling_improvements");
//        basicAxeRight = new DuplexHeadModule(headRightKey, "basic_axe", rightSuffix,
//                "duplex/improvements/basic_axe", "duplex/improvements/basic_axe_hone", "duplex/improvements/shared_head_hone",
//                "settling_improvements");
//        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/basic_axe");
//        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/basic_axe_hone");
//        new BookEnchantSchema(basicAxeLeft);
//        new BookEnchantSchema(basicAxeRight);
//
//        basicPickaxeLeft = new DuplexHeadModule(headLeftKey, "basic_pickaxe", leftSuffix,
//                "duplex/improvements/basic_pickaxe", "duplex/improvements/basic_pickaxe_hone", "duplex/improvements/shared_head_hone",
//                "settling_improvements");
//        basicPickaxeRight = new DuplexHeadModule(headRightKey, "basic_pickaxe", rightSuffix,
//                "duplex/improvements/basic_pickaxe", "duplex/improvements/basic_pickaxe_hone", "duplex/improvements/shared_head_hone",
//                "settling_improvements");
//        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/basic_pickaxe");
//        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/basic_pickaxe_hone");
//        new BookEnchantSchema(basicPickaxeLeft);
//        new BookEnchantSchema(basicPickaxeRight);
//
//        hoeLeft = new DuplexHeadModule(headLeftKey, "hoe", leftSuffix,
//                "duplex/improvements/hoe", "duplex/improvements/hoe_hone", "duplex/improvements/shared_head_hone",
//                "settling_improvements");
//        hoeRight = new DuplexHeadModule(headRightKey, "hoe", rightSuffix,
//                "duplex/improvements/hoe", "duplex/improvements/hoe_hone", "duplex/improvements/shared_head_hone",
//                "settling_improvements");
//        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/hoe");
//        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/hoe_hone");
//        new BookEnchantSchema(hoeLeft);
//        new BookEnchantSchema(hoeRight);
//
//        adzeLeft = new DuplexHeadModule(headLeftKey, "adze", leftSuffix,
//                "duplex/improvements/adze", "duplex/improvements/adze_hone", "duplex/improvements/shared_head_hone",
//                "settling_improvements");
//        adzeRight = new DuplexHeadModule(headRightKey, "adze", rightSuffix,
//                "duplex/improvements/adze", "duplex/improvements/adze_hone", "duplex/improvements/shared_head_hone",
//                "settling_improvements");
//        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/adze");
//        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/adze_hone");
//        new BookEnchantSchema(adzeLeft);
//        new BookEnchantSchema(adzeRight);
//
//        sickleLeft = new DuplexHeadModule(headLeftKey, "sickle", leftSuffix,
//                "duplex/improvements/sickle", "duplex/improvements/sickle_hone", "duplex/improvements/shared_head_hone",
//                "settling_improvements");
//        sickleRight = new DuplexHeadModule(headRightKey, "sickle", rightSuffix,
//                "duplex/improvements/sickle", "duplex/improvements/sickle_hone", "duplex/improvements/shared_head_hone",
//                "settling_improvements");
//        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/sickle");
//        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/sickle_hone");
//        new BookEnchantSchema(sickleLeft);
//        new BookEnchantSchema(sickleRight);
//
//        clawLeft = new DuplexHeadModule(headLeftKey, "claw", leftSuffix,
//                "duplex/improvements/claw", "duplex/improvements/claw_hone", "duplex/improvements/shared_head_hone",
//                "settling_improvements");
//        clawRight = new DuplexHeadModule(headRightKey, "claw", rightSuffix,
//                "duplex/improvements/claw", "duplex/improvements/claw_hone", "duplex/improvements/shared_head_hone",
//                "settling_improvements");
//        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/claw");
//        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/claw_hone");
//        new BookEnchantSchema(clawLeft);
//        new BookEnchantSchema(clawRight);
//
//        butt = new DuplexHeadModule(headRightKey, "butt", rightSuffix,
//                "duplex/improvements/butt", "duplex/improvements/butt_hone", "duplex/improvements/shared_head_hone",
//                "settling_improvements");
//        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/butt");
//        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/butt_hone");
//        new BookEnchantSchema(butt);

        handle = new BasicMajorModule(handleKey, "duplex/basic_handle",
                "duplex/improvements/basic_handle", "duplex/improvements/basic_handle_hone", "duplex/improvements/shared_head_hone",
                "settling_improvements")
                .withRenderLayer(Priority.LOWER);
        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/basic_handle");
        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/basic_handle_hone");
        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/basic_handle_improvements");
        new BookEnchantSchema(handle);

//        new BasicModule(bindingKey, "duplex/binding", "duplex/tweaks/binding");
//        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/binding");
//
//        new BasicModule(bindingKey, "duplex/socket");
//        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/binding");

        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/shared_head_hone");

        new RepairSchema(this);
        RemoveSchema.registerRemoveSchemas(this);

//        ItemUpgradeRegistry.instance.registerReplacementDefinition("axe");
//        ItemUpgradeRegistry.instance.registerReplacementDefinition("pickaxe");
//        ItemUpgradeRegistry.instance.registerReplacementDefinition("hoe");

        ItemUpgradeRegistry.instance.registerConfigSchema("duplex/socket");

        setupHammerStack(creativeWoodHammer, "log", "stick");
        setupHammerStack(creativeObsidianHammer, "obsidian", "iron");
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        if (isInGroup(group)) {
            items.add(creativeWoodHammer);
            items.add(creativeObsidianHammer);
        }
    }

    private void setupHammerStack(ItemStack itemStack, String headMaterial, String handleMaterial) {
        basicHammerHeadLeft.addModule(itemStack, "basic_hammer/" + headMaterial, null);
        basicHammerHeadRight.addModule(itemStack, "basic_hammer/" + headMaterial, null);
        handle.addModule(itemStack, "basic_handle/" + handleMaterial, null);
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        Hand hand = context.getHand();
        if (player != null && !player.isSneaking() && world.getBlockState(pos).getBlock().equals(Blocks.CRAFTING_TABLE)
                && getCapabilityLevel(player.getHeldItem(hand), Capability.hammer) > 0) {
            return WorkbenchBlock.upgradeWorkbench(player, world, pos, hand, context.getFace());
        }
        return super.onItemUseFirst(stack, context);
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


