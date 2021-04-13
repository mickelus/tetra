package se.mickelus.tetra.items.modular.impl;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import net.minecraft.block.Blocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.ToolTypes;
import se.mickelus.tetra.blocks.workbench.BasicWorkbenchBlock;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.effect.ChargedAbilityEffect;
import se.mickelus.tetra.gui.GuiModuleOffsets;
import se.mickelus.tetra.items.TetraItemGroup;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.module.SchematicRegistry;
import se.mickelus.tetra.module.data.ToolData;
import se.mickelus.tetra.module.schematic.RemoveSchematic;
import se.mickelus.tetra.module.schematic.RepairSchematic;
import se.mickelus.tetra.network.PacketHandler;
import se.mickelus.tetra.properties.AttributeHelper;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class ModularDoubleHeadedItem extends ItemModularHandheld {
    private static final Logger logger = LogManager.getLogger();

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
        super(new Item.Properties().maxStackSize(1).group(TetraItemGroup.instance).isImmuneToFire());
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
        DataManager.synergyData.onReload(() -> synergies = DataManager.instance.getSynergyData("double/"));
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        if (isInGroup(group)) {
            items.add(setupHammerStack("oak", "stick"));
            items.add(setupHammerStack("stone", "stick"));
            items.add(setupHammerStack("iron", "spruce"));
            items.add(setupHammerStack("blackstone", "spruce"));
            items.add(setupHammerStack("obsidian", "iron"));
            items.add(setupHammerStack("netherite", "forged_beam"));
        }
    }

    private ItemStack setupHammerStack(String headMaterial, String handleMaterial) {
        ItemStack itemStack = new ItemStack(this);

        IModularItem.putModuleInSlot(itemStack, headLeftKey, "double/basic_hammer_left", "double/basic_hammer_left_material", "basic_hammer/" + headMaterial);
        IModularItem.putModuleInSlot(itemStack, headRightKey, "double/basic_hammer_right", "double/basic_hammer_right_material", "basic_hammer/" + headMaterial);
        IModularItem.putModuleInSlot(itemStack, handleKey, "double/basic_handle", "double/basic_handle_material", "basic_handle/" + handleMaterial);

        IModularItem.updateIdentifier(itemStack);

        return itemStack;
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        Hand hand = context.getHand();
        if (player != null
                && !player.isCrouching()
                && world.getBlockState(pos).getBlock().equals(Blocks.CRAFTING_TABLE)
                && getToolLevel(player.getHeldItem(hand), ToolTypes.hammer) > 0) {
            return BasicWorkbenchBlock.upgradeWorkbench(player, world, pos, hand, context.getFace());
        }
        return super.onItemUseFirst(stack, context);
    }

    @Override
    public String getDisplayNamePrefixes(ItemStack itemStack) {
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

    // overridden to not stack the damage attribute between heads
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(ItemStack itemStack) {
        Multimap<Attribute, AttributeModifier> moduleAttributes = Stream.of(getModuleFromSlot(itemStack, headLeftKey), getModuleFromSlot(itemStack, headRightKey))
                .filter(Objects::nonNull)
                .map(module -> module.getAttributeModifiers(itemStack))
                .filter(Objects::nonNull)
//                .peek(modifiers -> modifiers.asMap().entrySet().forEach(entry -> entry.setValue(AttributeHelper.collapseModifiers(entry.getValue()))))
                .map(modifiers -> modifiers.asMap().entrySet().stream().collect(Multimaps.flatteningToMultimap(
                        Map.Entry::getKey,
                        entry -> AttributeHelper.collapse(entry.getValue()).stream(),
                        ArrayListMultimap::create)))
                .map(Multimap::entries)
                .flatMap(Collection::stream)
                .collect(Multimaps.toMultimap(Map.Entry::getKey, Map.Entry::getValue, ArrayListMultimap::create));
        moduleAttributes = AttributeHelper.retainMax(moduleAttributes, Attributes.ATTACK_DAMAGE);

        moduleAttributes = getAllModules(itemStack).stream()
                .filter(itemModule -> !(headLeftKey.equals(itemModule.getSlot()) || headRightKey.equals(itemModule.getSlot())))
                .map(module -> module.getAttributeModifiers(itemStack))
                .reduce(moduleAttributes, AttributeHelper::merge);

        return Arrays.stream(getSynergyData(itemStack))
                .map(synergy -> synergy.attributes)
                .reduce(moduleAttributes, AttributeHelper::merge);
    }

    // overridden to not stack the tool level/efficiency attribute between heads
    @Override
    public ToolData getToolDataRaw(ItemStack itemStack) {
        logger.debug("Gathering tool data for {} ({})", getDisplayName(itemStack).getString(), getDataCacheKey(itemStack));
        ToolData result = ToolData.retainMax(Stream.of(getModuleFromSlot(itemStack, headLeftKey), getModuleFromSlot(itemStack, headRightKey))
                .filter(Objects::nonNull)
                .map(module -> module.getToolData(itemStack))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        return Stream.concat(
                getAllModules(itemStack).stream()
                        .filter(itemModule -> !(headLeftKey.equals(itemModule.getSlot()) || headRightKey.equals(itemModule.getSlot())))
                        .map(module -> module.getToolData(itemStack)),
                Arrays.stream(getSynergyData(itemStack))
                        .map(synergy -> synergy.tools))
                .filter(Objects::nonNull)
                .reduce(result, ToolData::merge);
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

    @Override
    public String getModelCacheKey(ItemStack itemStack, LivingEntity entity) {
        if (entity != null && itemStack.equals(entity.getActiveItemStack())) {
            return Optional.ofNullable(getChargeableAbility(itemStack))
                    .map(ChargedAbilityEffect::getModelTransform)
                    .map(transform -> super.getModelCacheKey(itemStack, entity) + ":" + transform)
                    .orElseGet(() -> super.getModelCacheKey(itemStack, entity));
        }

        return super.getModelCacheKey(itemStack, entity);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public String getTransformVariant(ItemStack itemStack, @Nullable LivingEntity entity) {
        ChargedAbilityEffect ability = getChargeableAbility(itemStack);
        if (entity != null && ability != null && itemStack.equals(entity.getActiveItemStack())) {
            return ability.getModelTransform();
        }

        return null;
    }
}


