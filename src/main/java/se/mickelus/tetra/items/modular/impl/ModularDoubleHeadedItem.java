package se.mickelus.tetra.items.modular.impl;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.mutil.network.PacketHandler;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.TetraToolActions;
import se.mickelus.tetra.blocks.workbench.BasicWorkbenchBlock;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.effect.ChargedAbilityEffect;
import se.mickelus.tetra.gui.GuiModuleOffsets;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.module.Priority;
import se.mickelus.tetra.module.SchematicRegistry;
import se.mickelus.tetra.module.data.ToolData;
import se.mickelus.tetra.module.schematic.RepairSchematic;
import se.mickelus.tetra.properties.AttributeHelper;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
public class ModularDoubleHeadedItem extends ItemModularHandheld {
    public final static String headLeftKey = "double/head_left";
    public final static String headRightKey = "double/head_right";
    public final static String handleKey = "double/handle";
    public final static String bindingKey = "double/binding";
    public final static String accessoryKey = "double/accessory";
    public final static String leftSuffix = "_left";
    public final static String rightSuffix = "_right";
    public static final String identifier = "modular_double";
    private static final Logger logger = LogManager.getLogger();
    private static final GuiModuleOffsets majorOffsets = new GuiModuleOffsets(-13, -1, 3, 19, -13, 19);
    private static final GuiModuleOffsets minorOffsets = new GuiModuleOffsets(6, 1);

    @ObjectHolder(registryName = "item", value = TetraMod.MOD_ID + ":" + identifier)
    public static ModularDoubleHeadedItem instance;

    public ModularDoubleHeadedItem() {
        super(new Item.Properties().stacksTo(1).fireResistant());

        entityHitDamage = 2;

        majorModuleKeys = new String[] { headLeftKey, headRightKey, handleKey };
        minorModuleKeys = new String[] { bindingKey };

        requiredModules = new String[] { handleKey, headLeftKey, headRightKey };

        updateConfig(ConfigHandler.honedoubleBase.get(), ConfigHandler.honedoubleIntegrityMultiplier.get());


        SchematicRegistry.instance.registerSchematic(new RepairSchematic(this, identifier));
    }

    public void updateConfig(int honeBase, int honeIntegrityMultiplier) {
        this.honeBase = honeBase;
        this.honeIntegrityMultiplier = honeIntegrityMultiplier;
    }

    @Override
    public void commonInit(PacketHandler packetHandler) {
        DataManager.instance.synergyData.onReload(() -> synergies = DataManager.instance.synergyData.getOrdered("double/"));
    }

    public static Collection<ItemStack> getCreativeTabItemStacks() {
        return Lists.newArrayList(
                setupHammerStack("oak", "stick"),
                setupHammerStack("stone", "stick"),
                setupHammerStack("iron", "spruce"),
                setupHammerStack("blackstone", "spruce"),
                setupHammerStack("obsidian", "iron"),
                setupHammerStack("netherite", "forged_beam")
        );
    }

    public static ItemStack setupHammerStack(String headMaterial, String handleMaterial) {
        ItemStack itemStack = new ItemStack(instance);

        IModularItem.putModuleInSlot(itemStack, headLeftKey, "double/basic_hammer_left", "double/basic_hammer_left_material", "basic_hammer/" + headMaterial);
        IModularItem.putModuleInSlot(itemStack, headRightKey, "double/basic_hammer_right", "double/basic_hammer_right_material", "basic_hammer/" + headMaterial);
        IModularItem.putModuleInSlot(itemStack, handleKey, "double/basic_handle", "double/basic_handle_material", "basic_handle/" + handleMaterial);

        IModularItem.updateIdentifier(itemStack);

        return itemStack;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        InteractionHand hand = context.getHand();
        if (player != null
                && !player.isCrouching()
                && world.getBlockState(pos).getBlock().equals(Blocks.CRAFTING_TABLE)
                && getToolLevel(player.getItemInHand(hand), TetraToolActions.hammer) > 0) {
            return BasicWorkbenchBlock.upgradeWorkbench(player, world, pos, hand, context.getClickedFace());
        }
        return super.onItemUseFirst(stack, context);
    }

    @Override
    public String getDisplayNamePrefixes(ItemStack itemStack) {
        return Stream.concat(
                        Arrays.stream(getImprovements(itemStack))
                                .map(improvement -> Pair.of(improvement.prefixPriority, "tetra.improvement." + improvement.key + ".prefix"))
                                .filter(pair -> I18n.exists(pair.getSecond()))
                                .map(pair -> Pair.of(pair.getFirst(), I18n.get(pair.getSecond()))),
                        getAllModules(itemStack).stream()
                                .filter(module -> headLeftKey.equals(module.getSlot()) || module.getItemPrefixPriority(itemStack) != Priority.BASE)
                                .map(module -> Pair.of(module.getItemPrefixPriority(itemStack), module.getItemPrefix(itemStack)))
                                .filter(pair -> pair.getSecond() != null)
                )
                .sorted(Comparator.<Pair<Priority, String>, Priority>comparing(Pair::getFirst).reversed())
                .limit(2)
                .map(Pair::getSecond)
                .reduce("", (result, prefix) -> result + prefix + " ");
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
        logger.debug("Gathering tool data for {} ({})", getName(itemStack).getString(), getDataCacheKey(itemStack));
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
    public GuiModuleOffsets getMajorGuiOffsets(ItemStack itemStack) {
        return majorOffsets;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public GuiModuleOffsets getMinorGuiOffsets(ItemStack itemStack) {
        return minorOffsets;
    }

    @Override
    public String getModelCacheKey(ItemStack itemStack, LivingEntity entity) {
        if (entity != null && itemStack.equals(entity.getUseItem())) {
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
        if (entity != null && ability != null && itemStack.equals(entity.getUseItem())) {
            return ability.getModelTransform();
        }

        return null;
    }
}


