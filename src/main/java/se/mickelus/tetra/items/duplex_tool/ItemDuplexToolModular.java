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
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.items.BasicMajorModule;
import se.mickelus.tetra.items.BasicModule;
import se.mickelus.tetra.items.ItemModularHandheld;
import se.mickelus.tetra.items.TetraItemGroup;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.MultiSlotModule;
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

    public static MultiSlotModule basicHammerHeadLeft;
    public static MultiSlotModule basicHammerHeadRight;

    public static MultiSlotModule basicAxeLeft;
    public static MultiSlotModule basicAxeRight;

    public static MultiSlotModule basicPickaxeLeft;
    public static MultiSlotModule basicPickaxeRight;

    public static MultiSlotModule hoeLeft;
    public static MultiSlotModule hoeRight;

    public static MultiSlotModule adzeLeft;
    public static MultiSlotModule adzeRight;

    public static MultiSlotModule sickleLeft;
    public static MultiSlotModule sickleRight;

    public static MultiSlotModule clawLeft;
    public static MultiSlotModule clawRight;

    public static MultiSlotModule butt;

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
        DataManager.synergyData.onReload(() -> synergies = DataManager.instance.getSynergyData("duplex"));


        basicHammerHeadLeft = new MultiSlotModule(headLeftKey, "duplex/basic_hammer", leftSuffix,
                "duplex/basic_hammer", "duplex/shared_head_hone", "settling_improvements");
        basicHammerHeadRight = new MultiSlotModule(headRightKey, "duplex/basic_hammer", rightSuffix,
                "duplex/basic_hammer", "duplex/shared_head_hone", "settling_improvements");
        new BookEnchantSchema(basicHammerHeadLeft);
        new BookEnchantSchema(basicHammerHeadRight);

        basicAxeLeft = new MultiSlotModule(headLeftKey, "duplex/basic_axe", leftSuffix,
                "duplex/basic_axe", "duplex/basic_axe_hone", "duplex/shared_head_hone",
                "settling_improvements");
        basicAxeRight = new MultiSlotModule(headRightKey, "duplex/basic_axe", rightSuffix,
                "duplex/basic_axe", "duplex/basic_axe_hone", "duplex/shared_head_hone",
                "settling_improvements");
        new BookEnchantSchema(basicAxeLeft);
        new BookEnchantSchema(basicAxeRight);

        basicPickaxeLeft = new MultiSlotModule(headLeftKey, "duplex/basic_pickaxe", leftSuffix,
                "duplex/basic_pickaxe", "duplex/basic_pickaxe_hone", "duplex/shared_head_hone",
                "settling_improvements");
        basicPickaxeRight = new MultiSlotModule(headRightKey, "duplex/basic_pickaxe", rightSuffix,
                "duplex/basic_pickaxe", "duplex/basic_pickaxe_hone", "duplex/shared_head_hone",
                "settling_improvements");
        new BookEnchantSchema(basicPickaxeLeft);
        new BookEnchantSchema(basicPickaxeRight);

        hoeLeft = new MultiSlotModule(headLeftKey, "duplex/hoe", leftSuffix,
                "duplex/hoe", "duplex/hoe_hone", "duplex/shared_head_hone",
                "settling_improvements");
        hoeRight = new MultiSlotModule(headRightKey, "duplex/hoe", rightSuffix,
                "duplex/hoe", "duplex/hoe_hone", "duplex/shared_head_hone",
                "settling_improvements");
        new BookEnchantSchema(hoeLeft);
        new BookEnchantSchema(hoeRight);

        adzeLeft = new MultiSlotModule(headLeftKey, "duplex/adze", leftSuffix,
                "duplex/adze", "duplex/adze_hone", "duplex/shared_head_hone",
                "settling_improvements");
        adzeRight = new MultiSlotModule(headRightKey, "duplex/adze", rightSuffix,
                "duplex/adze", "duplex/adze_hone", "duplex/shared_head_hone",
                "settling_improvements");
        new BookEnchantSchema(adzeLeft);
        new BookEnchantSchema(adzeRight);

        sickleLeft = new MultiSlotModule(headLeftKey, "duplex/sickle", leftSuffix,
                "duplex/sickle", "duplex/sickle_hone", "duplex/shared_head_hone",
                "settling_improvements");
        sickleRight = new MultiSlotModule(headRightKey, "duplex/sickle", rightSuffix,
                "duplex/sickle", "duplex/sickle_hone", "duplex/shared_head_hone",
                "settling_improvements");
        new BookEnchantSchema(sickleLeft);
        new BookEnchantSchema(sickleRight);

        clawLeft = new MultiSlotModule(headLeftKey, "duplex/claw", leftSuffix,
                "duplex/claw", "duplex/claw_hone", "duplex/shared_head_hone",
                "settling_improvements");
        clawRight = new MultiSlotModule(headRightKey, "duplex/claw", rightSuffix,
                "duplex/claw", "duplex/claw_hone", "duplex/shared_head_hone",
                "settling_improvements");
        new BookEnchantSchema(clawLeft);
        new BookEnchantSchema(clawRight);

        butt = new MultiSlotModule(headRightKey, "duplex/butt", rightSuffix,
                "duplex/butt", "duplex/butt_hone", "duplex/shared_head_hone",
                "settling_improvements");
        new BookEnchantSchema(butt);

        handle = new BasicMajorModule(handleKey, "duplex/basic_handle",
                "duplex/basic_handle", "duplex/basic_handle_hone", "duplex/shared_head_hone",
                "settling_improvements")
                .withRenderLayer(Priority.LOWER);
        new BookEnchantSchema(handle);

        new BasicModule(bindingKey, "duplex/binding", "duplex/binding");

        new BasicModule(bindingKey, "duplex/socket");

        new RepairSchema(this);
        RemoveSchema.registerRemoveSchemas(this);

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


