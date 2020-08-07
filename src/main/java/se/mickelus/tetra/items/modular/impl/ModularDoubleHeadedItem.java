package se.mickelus.tetra.items.modular.impl;

import net.minecraft.block.Blocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.workbench.BasicWorkbenchBlock;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.gui.GuiModuleOffsets;
import se.mickelus.tetra.items.TetraItemGroup;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.module.SchematicRegistry;
import se.mickelus.tetra.module.schematic.RemoveSchematic;
import se.mickelus.tetra.module.schematic.RepairSchematic;
import se.mickelus.tetra.network.PacketHandler;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;


public class ModularDoubleHeadedItem extends ItemModularHandheld {

    public final static String headLeftKey = "double/head_left";
    public final static String headRightKey = "double/head_right";

    public final static String handleKey = "double/handle";
    public final static String bindingKey = "double/binding";
    public final static String accessoryKey = "double/accessory";

    public final static String leftSuffix = "_left";
    public final static String rightSuffix = "_right";

    private static final String unlocalizedName = "modular_double";

    private static final GuiModuleOffsets majorOffsets = new GuiModuleOffsets(-13, -1, 3, 19, -13, 19);
    private static final GuiModuleOffsets minorOffsets = new GuiModuleOffsets(6, 1);

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ModularDoubleHeadedItem instance;

    public ModularDoubleHeadedItem() {
        super(new Item.Properties().maxStackSize(1).group(TetraItemGroup.instance));
        setRegistryName(unlocalizedName);

        entityHitDamage = 2;

        majorModuleKeys = new String[] { headLeftKey, headRightKey, handleKey };
        minorModuleKeys = new String[] { bindingKey };

        requiredModules = new String[] { handleKey, headLeftKey, headRightKey };

        updateConfig(ConfigHandler.honedoubleBase.get(), ConfigHandler.honedoubleIntegrityMultiplier.get());


        SchematicRegistry.instance.registerSchematic(new RepairSchematic(this));
        RemoveSchematic.registerRemoveSchematics(this);
    }

    public void updateConfig(int honeBase, int honeIntegrityMultiplier) {
        this.honeBase = honeBase;
        this.honeIntegrityMultiplier = honeIntegrityMultiplier;
    }

    @Override
    public void init(PacketHandler packetHandler) {
        DataManager.synergyData.onReload(() -> synergies = DataManager.instance.getSynergyData("double"));
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        if (isInGroup(group)) {
            items.add(setupHammerStack("log", "stick"));
            items.add(setupHammerStack("obsidian", "iron"));
        }
    }

    private ItemStack setupHammerStack(String headMaterial, String handleMaterial) {
        ItemStack itemStack = new ItemStack(this);

        putModuleInSlot(itemStack, headLeftKey, "double/basic_hammer_left", "double/basic_hammer_left_material", "basic_hammer/" + headMaterial);
        putModuleInSlot(itemStack, headRightKey, "double/basic_hammer_right", "double/basic_hammer_right_material", "basic_hammer/" + headMaterial);
        putModuleInSlot(itemStack, handleKey, "double/basic_handle", "double/basic_handle_material", "basic_handle/" + handleMaterial);

        return itemStack;
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        Hand hand = context.getHand();
        if (player != null && !player.isCrouching() && world.getBlockState(pos).getBlock().equals(Blocks.CRAFTING_TABLE)
                && getCapabilityLevel(player.getHeldItem(hand), Capability.hammer) > 0) {
            return BasicWorkbenchBlock.upgradeWorkbench(player, world, pos, hand, context.getFace());
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

    @Override
    @OnlyIn(Dist.CLIENT)
    public GuiModuleOffsets getMajorGuiOffsets() {
        return majorOffsets;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public GuiModuleOffsets getMinorGuiOffsets() {
        return minorOffsets;
    }
}


