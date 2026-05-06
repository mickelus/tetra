package se.mickelus.tetra;

import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.SimpleTier;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import se.mickelus.tetra.advancements.*;
import se.mickelus.tetra.blocks.ArcaneFireBlock;
import se.mickelus.tetra.blocks.InitializableBlock;
import se.mickelus.tetra.blocks.forged.*;
import se.mickelus.tetra.blocks.forged.chthonic.*;
import se.mickelus.tetra.blocks.forged.container.ForgedContainerBlock;
import se.mickelus.tetra.blocks.forged.container.ForgedContainerBlockEntity;
import se.mickelus.tetra.blocks.forged.container.ForgedContainerMenu;
import se.mickelus.tetra.blocks.forged.extractor.*;
import se.mickelus.tetra.blocks.forged.hammer.HammerBaseBlock;
import se.mickelus.tetra.blocks.forged.hammer.HammerBaseBlockEntity;
import se.mickelus.tetra.blocks.forged.hammer.HammerHeadBlock;
import se.mickelus.tetra.blocks.forged.hammer.HammerHeadBlockEntity;
import se.mickelus.tetra.blocks.forged.transfer.TransferUnitBlock;
import se.mickelus.tetra.blocks.forged.transfer.TransferUnitBlockEntity;
import se.mickelus.tetra.blocks.geode.*;
import se.mickelus.tetra.blocks.geode.particle.SparkleParticleType;
import se.mickelus.tetra.blocks.holo.HolosphereBlock;
import se.mickelus.tetra.blocks.holo.HolosphereBlockEntity;
import se.mickelus.tetra.blocks.multischematic.MultiblockSchematicBlock;
import se.mickelus.tetra.blocks.rack.RackBlock;
import se.mickelus.tetra.blocks.rack.RackTile;
import se.mickelus.tetra.blocks.salvage.InteractiveBlockOverlay;
import se.mickelus.tetra.blocks.scroll.*;
import se.mickelus.tetra.blocks.workbench.BasicWorkbenchBlock;
import se.mickelus.tetra.blocks.workbench.WorkbenchContainer;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;
import se.mickelus.tetra.blocks.workbench.gui.WorkbenchStatsGui;
import se.mickelus.tetra.client.particle.DripParticles;
import se.mickelus.tetra.client.particle.Particles;
import se.mickelus.tetra.client.particle.PlainParticleType;
import se.mickelus.tetra.client.particle.SweepingStrikeParticleType;
import se.mickelus.tetra.crafting.ScrollIngredient;
import se.mickelus.tetra.crafting.ItemAbilityIngredient;
import se.mickelus.tetra.effect.howling.HowlingPotionEffect;
import se.mickelus.tetra.effect.potion.*;
import se.mickelus.tetra.gui.stats.sorting.StatSorters;
import se.mickelus.tetra.items.InitializableItem;
import se.mickelus.tetra.items.cell.ThermalCellItem;
import se.mickelus.tetra.items.forged.*;
import se.mickelus.tetra.items.loot.DragonSinewItem;
import se.mickelus.tetra.items.modular.ItemPredicateModular;
import se.mickelus.tetra.items.modular.ThrownModularItemEntity;
import se.mickelus.tetra.items.modular.impl.ModularBladedItem;
import se.mickelus.tetra.items.modular.impl.ModularDoubleHeadedItem;
import se.mickelus.tetra.items.modular.impl.ModularSingleHeadedItem;
import se.mickelus.tetra.items.modular.impl.bow.ModularBowItem;
import se.mickelus.tetra.items.modular.impl.crossbow.ModularCrossbowItemImpl;
import se.mickelus.tetra.items.modular.impl.crossbow.ShootableDummyItem;
import se.mickelus.tetra.items.modular.impl.dynamic.DynamicModularItem;
import se.mickelus.tetra.items.modular.impl.holo.ModularHolosphereItem;
import se.mickelus.tetra.items.modular.impl.holo.gui.craft.HoloStatsGui;
import se.mickelus.tetra.items.modular.impl.shield.ModularShieldItem;
import se.mickelus.tetra.items.modular.impl.toolbelt.ModularToolbeltItem;
import se.mickelus.tetra.items.modular.impl.toolbelt.ToolbeltContainer;
import se.mickelus.tetra.items.modular.impl.toolbelt.suspend.SuspendPotionEffect;
import se.mickelus.tetra.levelgen.*;
import se.mickelus.tetra.loot.FortuneBonusCondition;
import se.mickelus.tetra.loot.ReplaceTableModifier;
import se.mickelus.tetra.loot.ScrollDataFunction;
import se.mickelus.tetra.tools.HarvestTierRegistry;
import se.mickelus.mutil.network.PacketHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TetraRegistries {
    public static final DeferredRegister<Block> blocks = DeferredRegister.create(BuiltInRegistries.BLOCK, TetraMod.MOD_ID);
    public static final DeferredRegister<Item> items = DeferredRegister.create(BuiltInRegistries.ITEM, TetraMod.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> blockEntities = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE,
            TetraMod.MOD_ID);
    public static final DeferredRegister<MenuType<?>> containers = DeferredRegister.create(BuiltInRegistries.MENU, TetraMod.MOD_ID);
    public static final DeferredRegister<EntityType<?>> entities = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, TetraMod.MOD_ID);
    public static final DeferredRegister<ParticleType<?>> particles = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, TetraMod.MOD_ID);
    public static final DeferredRegister<MobEffect> effects = DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, TetraMod.MOD_ID);
    public static final DeferredRegister.DataComponents dataComponents = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, TetraMod.MOD_ID);
    public static final DeferredRegister<ItemSubPredicate.Type<?>> itemSubPredicates = DeferredRegister.create(Registries.ITEM_SUB_PREDICATE_TYPE, TetraMod.MOD_ID);
    public static final DeferredRegister<IngredientType<?>> ingredientTypes = DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, TetraMod.MOD_ID);
    public static final DeferredRegister<CriterionTrigger<?>> triggerTypes = DeferredRegister.create(Registries.TRIGGER_TYPE, TetraMod.MOD_ID);

    public static final DeferredRegister<SoundEvent> sounds = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, TetraMod.MOD_ID);
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> lootModifiers =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, TetraMod.MOD_ID);

    public static final DeferredRegister<LootItemConditionType> lootConditions = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE,
            TetraMod.MOD_ID);
    public static final DeferredRegister<LootItemFunctionType<?>> lootFunctions = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE,
            TetraMod.MOD_ID);
    public static final DeferredRegister<StructureProcessorType<?>> structureProcessors = DeferredRegister.create(BuiltInRegistries.STRUCTURE_PROCESSOR,
            TetraMod.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> creativeTabs = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TetraMod.MOD_ID);

    public static final TagKey<Block> forgeHammerIncorrectTag = BlockTags.create(ResourceLocation.fromNamespaceAndPath(TetraMod.MOD_ID, "incorrect_for_maxed_forge_hammer"));
    public static final Tier forgeHammerTier = HarvestTierRegistry.register(
            new SimpleTier(forgeHammerIncorrectTag, 0, 0, 0, 0, () -> Ingredient.EMPTY),
            ResourceLocation.fromNamespaceAndPath(TetraMod.MOD_ID, "maxed_forge_hammer"),
            List.of(Tiers.NETHERITE),
            List.of()
    );

    static {
        validateTierOrdering();
    }

    private static Item.Properties itemProperties;
    private static DeferredHolder<CreativeModeTab, CreativeModeTab> defaultCreativeTabs;
    private static DeferredHolder<Block, BasicWorkbenchBlock> basicWorkbench;
    private static DeferredHolder<Block, SeepingBedrockBlock> seepingBedrock;
    private static DeferredHolder<Block, RackBlock> rack;
    private static DeferredHolder<Item, BlockItem> chthonicExtractorItem;
    private static DeferredHolder<Block, FracturedBedrockBlock> fracturedBedrock;
    private static DeferredHolder<Block, ForgedWallBlock> forgedWall;
    private static DeferredHolder<Block, ForgedPillarBlock> forgedPillar;
    private static DeferredHolder<Block, ForgedPlatformBlock> forgedPlatform;
    private static DeferredHolder<Block, ForgedPlatformSlabBlock> forgedPlatformSlab;
    private static DeferredHolder<Block, ForgedVentBlock> forgedVent;
    private static DeferredHolder<Block, HammerBaseBlock> forgeHammer;
    private static DeferredHolder<Block, ForgedWorkbenchBlock> forgedWorkbench;
    private static DeferredHolder<Block, ForgedCrateBlock> forgedCrate;
    private static DeferredHolder<Block, TransferUnitBlock> transferUnit;
    private static DeferredHolder<Item, BoltItem> bolt;
    private static DeferredHolder<Item, DragonSinewItem> dragonSinew;
    private static DeferredHolder<Item, StonecutterItem> stonecutter;
    private static DeferredHolder<Item, EarthpiercerItem> earthpiercer;
    private static DeferredHolder<Item, ModularHolosphereItem> modularHolosphere;
    private static DeferredHolder<Item, PlanarStabilizerItem> planarStabilizer;
    private static DeferredHolder<Item, InsulatedPlateItem> insulatedPlate;
    private static DeferredHolder<Item, QuickLatchItem> quickLatch;
    private static DeferredHolder<Item, MeshItem> mesh;
    private static DeferredHolder<Item, BeamItem> beam;
    private static DeferredHolder<Item, PristineDiamondItem> pristineDiamond;
    private static DeferredHolder<Item, PristineEmeraldItem> pristineEmerald;
    private static DeferredHolder<Item, PristineLapisItem> pristineLapis;
    private static DeferredHolder<Item, PristineAmethystItem> pristineAmethyst;
    private static DeferredHolder<Item, PristineQuartzItem> pristineQuartz;
    private static DeferredHolder<Item, GeodeItem> geode;
    public static Supplier<DataComponentType<ScrollData>> scrollData;

    public static void init(IEventBus bus) {
        bus.register(TetraRegistries.class);

        blocks.register(bus);
        items.register(bus);
        blockEntities.register(bus);
        entities.register(bus);
        particles.register(bus);
        containers.register(bus);
        effects.register(bus);
        dataComponents.register(bus);
        itemSubPredicates.register(bus);
        ingredientTypes.register(bus);
        triggerTypes.register(bus);
        sounds.register(bus);
        lootConditions.register(bus);
        lootFunctions.register(bus);
        lootModifiers.register(bus);
        structureProcessors.register(bus);
        creativeTabs.register(bus);

        itemProperties = new Item.Properties();

        triggerTypes.register("block_use", () -> BlockUseCriterion.trigger);
        triggerTypes.register("block_interaction", () -> BlockInteractionCriterion.trigger);
        triggerTypes.register("craft_module", () -> ModuleCraftCriterion.trigger);
        triggerTypes.register("craft_improvement", () -> ImprovementCraftCriterion.trigger);
        triggerTypes.register("destabilize", () -> DestabilizeCriterion.trigger);

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // CREATIVE TABS
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        defaultCreativeTabs = register(TetraRegistries.creativeTabs, "default", () -> CreativeModeTab.builder()
                .icon(() -> new ItemStack(GeodeItem.instance))
                .title(Component.translatable("itemGroup.tetra"))
                .build());

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // BLOCKS
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        // crafting
        basicWorkbench = register(blocks, BasicWorkbenchBlock.identifier, BasicWorkbenchBlock::new,
                value -> BasicWorkbenchBlock.instance = value);
        registerBlockItem(basicWorkbench);
        HolosphereBlock.instance = register(blocks, HolosphereBlock.identifier, HolosphereBlock::new);
        rack = register(blocks, RackBlock.identifier, RackBlock::new, value -> RackBlock.instance = value);
        registerBlockItem(rack);

        // arcane fire - no block item needed
        ArcaneFireBlock.instance = register(blocks, ArcaneFireBlock.identifier, ArcaneFireBlock::new);

        // scrolls
        var rolledScroll = register(blocks, RolledScrollBlock.identifier, RolledScrollBlock::new,
                value -> RolledScrollBlock.instance = value);
        var wallScroll = register(blocks, WallScrollBlock.identifier, WallScrollBlock::new,
                value -> WallScrollBlock.instance = value);
        var openScroll = register(blocks, OpenScrollBlock.identifier, OpenScrollBlock::new,
                value -> OpenScrollBlock.instance = value);

        // base ruins
        forgedWall = register(blocks, ForgedWallBlock.identifier, ForgedWallBlock::new);
        registerBlockItem(forgedWall);
        forgedPillar = register(blocks, ForgedPillarBlock.identifier, ForgedPillarBlock::new);
        registerBlockItem(forgedPillar);
        forgedPlatform = register(blocks, ForgedPlatformBlock.identifier, ForgedPlatformBlock::new,
                value -> ForgedPlatformBlock.instance = value);
        registerBlockItem(forgedPlatform);
        forgedPlatformSlab = register(blocks, ForgedPlatformSlabBlock.identifier, ForgedPlatformSlabBlock::new);
        registerBlockItem(forgedPlatformSlab);
        forgedVent = register(blocks, ForgedVentBlock.identifier, ForgedVentBlock::new, value -> ForgedVentBlock.instance = value);
        registerBlockItem(forgedVent);
        blocks.register(HammerHeadBlock.identifier, HammerHeadBlock::new);
        forgeHammer = register(blocks, HammerBaseBlock.identifier, HammerBaseBlock::new);
        registerBlockItem(forgeHammer);
        forgedWorkbench = register(blocks, ForgedWorkbenchBlock.identifier, ForgedWorkbenchBlock::new);
        registerBlockItem(forgedWorkbench);
        var forgedContainer = register(blocks, ForgedContainerBlock.identifier, ForgedContainerBlock::new);
        ForgedContainerBlock.instance = forgedContainer;
        registerBlockItem(forgedContainer);
        forgedCrate = register(blocks, ForgedCrateBlock.identifier, ForgedCrateBlock::new);
        registerBlockItem(forgedCrate);
        transferUnit = register(blocks, TransferUnitBlock.identifier, TransferUnitBlock::new,
                value -> TransferUnitBlock.instance = value);
        registerBlockItem(transferUnit);

        // chthonic extractor
        var chthonicExtractor = register(blocks, ChthonicExtractorBlock.identifier, ChthonicExtractorBlock::new,
                value -> ChthonicExtractorBlock.instance = value);
        chthonicExtractorItem = ChthonicExtractorBlock.registerItems(items);
        fracturedBedrock = register(blocks, FracturedBedrockBlock.identifier, FracturedBedrockBlock::new,
                value -> FracturedBedrockBlock.instance = value);
        register(blocks, DepletedBedrockBlock.identifier, DepletedBedrockBlock::new, value -> DepletedBedrockBlock.instance = value);

        // thermal extractor
        var coreExtractorBase = register(blocks, CoreExtractorBaseBlock.identifier, CoreExtractorBaseBlock::new);
        CoreExtractorBaseBlock.instance = coreExtractorBase;
        registerBlockItem(coreExtractorBase);
        CoreExtractorPistonBlock.instance = register(blocks, CoreExtractorPistonBlock.identifier, CoreExtractorPistonBlock::new);
        registerBlockItem(register(blocks, CoreExtractorPipeBlock.identifier, CoreExtractorPipeBlock::new,
                value -> CoreExtractorPipeBlock.instance = value));
        seepingBedrock = register(blocks, SeepingBedrockBlock.identifier, SeepingBedrockBlock::new,
                value -> SeepingBedrockBlock.instance = value);
        registerBlockItem(seepingBedrock);

        // multiblock schematics
        new MultiblockSchematicBlock.Builder("stonecutter", 3, 2, ForgedBlockCommon.propertiesSolid)
                .build(blocks, items);

        new MultiblockSchematicBlock.Builder("earthpiercer", 2, 2, ForgedBlockCommon.propertiesSolid)
                .build(blocks, items);

        new MultiblockSchematicBlock.Builder("extractor", 3, 3, ForgedBlockCommon.propertiesSolid)
                .build(blocks, items);

        // misc
        register(blocks, GeodeBlock.identifier, GeodeBlock::new, value -> GeodeBlock.instance = value);

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // ITEMS
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        // modular items
        items.register(ModularBladedItem.identifier, ModularBladedItem::new);
        items.register(ModularDoubleHeadedItem.identifier, ModularDoubleHeadedItem::new);
        items.register(ModularBowItem.identifier, ModularBowItem::new);
        var shootableDummy = register(items, ShootableDummyItem.identifier, ShootableDummyItem::new);
        items.register(ModularCrossbowItemImpl.identifier, () -> new ModularCrossbowItemImpl(shootableDummy.get()));
        items.register(ModularSingleHeadedItem.identifier, ModularSingleHeadedItem::new);
        items.register(ModularShieldItem.identifier, ModularShieldItem::new);
        ModularToolbeltItem.instance = register(items, ModularToolbeltItem.identifier, ModularToolbeltItem::new);
        modularHolosphere = register(items, ModularHolosphereItem.identifier, ModularHolosphereItem::new,
                value -> ModularHolosphereItem.instance = value);
        items.register(DynamicModularItem.identifier, DynamicModularItem::new);

        // random loot
        geode = register(items, GeodeItem.identifier, GeodeItem::new, value -> GeodeItem.instance = value);
        pristineLapis = register(items, PristineLapisItem.identifier, PristineLapisItem::new, value -> PristineLapisItem.instance = value);
        pristineEmerald = register(items, PristineEmeraldItem.identifier, PristineEmeraldItem::new,
                value -> PristineEmeraldItem.instance = value);
        pristineDiamond = register(items, PristineDiamondItem.identifier, PristineDiamondItem::new,
                value -> PristineDiamondItem.instance = value);
        pristineAmethyst = register(items, PristineAmethystItem.identifier, PristineAmethystItem::new,
                value -> PristineAmethystItem.instance = value);
        pristineQuartz = register(items, PristineQuartzItem.identifier, PristineQuartzItem::new,
                value -> PristineQuartzItem.instance = value);
        dragonSinew = register(items, DragonSinewItem.identifier, DragonSinewItem::new);

        // ruins loot
        bolt = register(items, BoltItem.identifier, BoltItem::new, value -> BoltItem.instance = value);
        beam = register(items, BeamItem.identifier, BeamItem::new, value -> BeamItem.instance = value);
        mesh = register(items, MeshItem.identifier, MeshItem::new, value -> MeshItem.instance = value);
        quickLatch = register(items, QuickLatchItem.identifier, QuickLatchItem::new, value -> QuickLatchItem.instance = value);
        MetalScrapItem.instance = register(items, MetalScrapItem.identifier, MetalScrapItem::new);
        insulatedPlate = register(items, InsulatedPlateItem.identifier, InsulatedPlateItem::new,
                value -> InsulatedPlateItem.instance = value);
        planarStabilizer = register(items, PlanarStabilizerItem.identifier, PlanarStabilizerItem::new,
                value -> PlanarStabilizerItem.instance = value);
        ThermalCellItem.instance = register(items, ThermalCellItem.identifier, ThermalCellItem::new);
        CombustionChamberItem.instance = register(items, CombustionChamberItem.identifier, CombustionChamberItem::new);
        LubricantDispenserItem.instance = register(items, LubricantDispenserItem.identifier, LubricantDispenserItem::new);
        earthpiercer = register(items, EarthpiercerItem.identifier, EarthpiercerItem::new, value -> EarthpiercerItem.instance = value);
        stonecutter = register(items, StonecutterItem.identifier, StonecutterItem::new, value -> StonecutterItem.instance = value);

        register(items, ScrollItem.identifier, () -> new ScrollItem(rolledScroll.get()), value -> ScrollItem.instance = value);

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // BLOCK ENTITIES
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        WorkbenchTile.type = register(blockEntities, WorkbenchTile.identifier,
                () -> BlockEntityType.Builder.of(WorkbenchTile::new, basicWorkbench.get(), forgedWorkbench.get()).build(null));
        ChthonicExtractorTile.type = register(blockEntities, ChthonicExtractorBlock.identifier,
                () -> BlockEntityType.Builder.of(ChthonicExtractorTile::new, chthonicExtractor.get()).build(null));
        register(blockEntities, FracturedBedrockBlock.identifier,
                () -> BlockEntityType.Builder.of(FracturedBedrockTile::new, fracturedBedrock.get()).build(null),
                value -> FracturedBedrockTile.type = value);
        register(blockEntities, RackBlock.identifier,
                () -> BlockEntityType.Builder.of(RackTile::new, rack.get()).build(null),
                value -> RackTile.type = value);
        register(blockEntities, ScrollTile.identifier,
                () -> BlockEntityType.Builder.of(ScrollTile::new, openScroll.get(), wallScroll.get(), rolledScroll.get()).build(null),
                value -> ScrollTile.type = value);

        HammerBaseBlockEntity.type = register(blockEntities, HammerBaseBlock.identifier,
                () -> BlockEntityType.Builder.of(HammerBaseBlockEntity::new, HammerBaseBlock.instance).build(null));
        HammerHeadBlockEntity.type = register(blockEntities, HammerHeadBlock.identifier,
                () -> BlockEntityType.Builder.of(HammerHeadBlockEntity::new, HammerHeadBlock.instance).build(null));
        TransferUnitBlockEntity.type = register(blockEntities, TransferUnitBlock.identifier,
                () -> BlockEntityType.Builder.of(TransferUnitBlockEntity::new, transferUnit.get()).build(null));
        register(blockEntities, CoreExtractorBaseBlock.identifier,
                () -> BlockEntityType.Builder.of(CoreExtractorBaseBlockEntity::new, CoreExtractorBaseBlock.instance.get()).build(null),
                value -> CoreExtractorBaseBlockEntity.type = value);
        CoreExtractorPistonBlockEntity.type = register(blockEntities, CoreExtractorPistonBlock.identifier,
                () -> BlockEntityType.Builder.of(CoreExtractorPistonBlockEntity::new, CoreExtractorPistonBlock.instance.get()).build(null));
        ForgedContainerBlockEntity.type = register(blockEntities, ForgedContainerBlock.identifier,
                () -> BlockEntityType.Builder.of(ForgedContainerBlockEntity::new, ForgedContainerBlock.instance.get())
                        .build(null));
        HolosphereBlockEntity.type = register(blockEntities, HolosphereBlock.identifier,
                () -> BlockEntityType.Builder.of(HolosphereBlockEntity::new, HolosphereBlock.instance.get())
                        .build(null));

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // ENTITIES
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        register(entities, ThrownModularItemEntity.unlocalizedName, () ->
                EntityType.Builder.<ThrownModularItemEntity>of(ThrownModularItemEntity::new, MobCategory.MISC)
                        .sized(0.5F, 0.5F)
                        .build(ThrownModularItemEntity.unlocalizedName),
                value -> ThrownModularItemEntity.type = value
        );

        register(entities, ExtractorProjectileEntity.unlocalizedName, () ->
                EntityType.Builder.<ExtractorProjectileEntity>of(ExtractorProjectileEntity::new, MobCategory.MISC)
                        .sized(0.5F, 0.5F)
                        .build(ExtractorProjectileEntity.unlocalizedName),
                value -> ExtractorProjectileEntity.type = value
        );

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // PARTICLES
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        register(particles, SparkleParticleType.identifier, () -> new SimpleParticleType(false), value -> SparkleParticleType.instance = value);
        register(particles, SweepingStrikeParticleType.identifier, SweepingStrikeParticleType::new,
                value -> SweepingStrikeParticleType.instance = value);
        register(particles, PlainParticleType.identifier, PlainParticleType::new, value -> PlainParticleType.instance = value);
        Particles.arcaneFire = register(particles, "arcane_fire", () -> new SimpleParticleType(false));
        Particles.splinteredPower = register(particles, "splintered_power", () -> new SimpleParticleType(false));
        Particles.sputteringPower = register(particles, "sputtering_power", () -> new SimpleParticleType(false));
        DripParticles.fallingBlood = register(particles, "falling_blood", () -> new SimpleParticleType(true));
        DripParticles.landingBlood = register(particles, "landing_blood", () -> new SimpleParticleType(true));
        DripParticles.fallingSlime = register(particles, "falling_slime", () -> new SimpleParticleType(true));
        DripParticles.landingSlime = register(particles, "landing_slime", () -> new SimpleParticleType(true));


        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // CONTAINERS
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // toolbelt
        ToolbeltContainer.type = register(containers, ModularToolbeltItem.identifier,
                () -> IMenuTypeExtension.create((windowId, inv, data) -> ToolbeltContainer.create(windowId, inv)));

        // workbench
        WorkbenchContainer.containerType = register(containers, WorkbenchTile.identifier,
                () -> IMenuTypeExtension.create((windowId, inv, data) -> WorkbenchContainer.create(windowId, data.readBlockPos(), inv)));

        // forged container
        ForgedContainerMenu.type = register(containers, ForgedContainerBlock.identifier,
                () -> IMenuTypeExtension.create((windowId, inv, data) -> ForgedContainerMenu.create(windowId, data.readBlockPos(), inv)));


        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // EFFECTS
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        effects.register(BleedingPotionEffect.identifier, BleedingPotionEffect::new);
        effects.register(EarthboundPotionEffect.identifier, EarthboundPotionEffect::new);
        effects.register(StunPotionEffect.identifier, StunPotionEffect::new);
        effects.register(HowlingPotionEffect.identifier, HowlingPotionEffect::new);
        effects.register(SeveredPotionEffect.identifier, SeveredPotionEffect::new);
        effects.register(PuncturedPotionEffect.identifier, PuncturedPotionEffect::new);
        effects.register(PriedPotionEffect.identifier, PriedPotionEffect::new);
        effects.register(ExhaustedPotionEffect.identifier, ExhaustedPotionEffect::new);
        effects.register(SteeledPotionEffect.identifier, SteeledPotionEffect::new);
        effects.register(SmallStrengthPotionEffect.identifier, SmallStrengthPotionEffect::new);
        effects.register(UnwaveringPotionEffect.identifier, UnwaveringPotionEffect::new);
        effects.register(SmallHealthPotionEffect.identifier, SmallHealthPotionEffect::new);
        effects.register(SmallAbsorbPotionEffect.identifier, SmallAbsorbPotionEffect::new);
        effects.register(SuspendPotionEffect.identifier, SuspendPotionEffect::new);
        effects.register(MiningSpeedPotionEffect.identifier, MiningSpeedPotionEffect::new);
        effects.register(UnstablePowerMobEffect.identifier, UnstablePowerMobEffect::new);
        effects.register(SatiatedPotionEffect.identifier, SatiatedPotionEffect::new);

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // SOUNDS
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        sounds.register(TetraSounds.scanHit.getLocation().getPath(), () -> TetraSounds.scanHit);
        sounds.register(TetraSounds.scanMiss.getLocation().getPath(), () -> TetraSounds.scanMiss);

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // LOOT CONDITIONS
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        FortuneBonusCondition.type = register(lootConditions, FortuneBonusCondition.identifier,
                () -> new LootItemConditionType(FortuneBonusCondition.CODEC));

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // LOOT FUNCTIONS
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ScrollDataFunction.type = register(lootFunctions, ScrollDataFunction.identifier,
                () -> new LootItemFunctionType<>(ScrollDataFunction.CODEC));

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // LOOT MODIFIERS
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        lootModifiers.register("replace_table", () -> ReplaceTableModifier.CODEC);

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // STRUCTURE PROCESSORS
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ForgedHammerProcessor.type = registerStructureProcessor("hammer", () -> ForgedHammerProcessor.codec)::get;
        ForgedCrateProcessor.type = registerStructureProcessor("crate", () -> ForgedCrateProcessor.codec)::get;
        ForgedContainerProcessor.type = registerStructureProcessor("container", () -> ForgedContainerProcessor.codec)::get;
        TransferUnitProcessor.type = registerStructureProcessor("transfer_unit", () -> TransferUnitProcessor.codec)::get;
        MultiblockSchematicProcessor.type = registerStructureProcessor("multiblock_schematic", () -> MultiblockSchematicProcessor.codec)::get;

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // ITEM SUB PREDICATES
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        scrollData = dataComponents.registerComponentType("scroll_data", builder -> builder
                .persistent(ScrollData.CODEC)
                .networkSynchronized(ScrollData.STREAM_CODEC));
        itemSubPredicates.register("modular_item", () -> ItemPredicateModular.TYPE);
        itemSubPredicates.register("scroll_data", () -> ScrollDataPredicate.TYPE);

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // INGREDIENT TYPES
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ScrollIngredient.type = register(ingredientTypes, "scroll", () -> new IngredientType<>(ScrollIngredient.CODEC, ScrollIngredient.STREAM_CODEC));
        ItemAbilityIngredient.type = register(ingredientTypes, "tool_action", () -> new IngredientType<>(ItemAbilityIngredient.CODEC, ItemAbilityIngredient.STREAM_CODEC));
    }

    public static <B extends Block> DeferredHolder<Item, BlockItem> registerBlockItem(DeferredHolder<Block, B> block) {
        return register(items, block.getId().getPath(), () -> new BlockItem(block.get(), itemProperties));
    }

    public static <P extends StructureProcessor> DeferredHolder<StructureProcessorType<?>, StructureProcessorType<P>> registerStructureProcessor(
            String id, StructureProcessorType<P> type) {
        return register(structureProcessors, id, () -> type);
    }

    public static void registerPackets(PacketHandler packetHandler) {
        blocks.getEntries().stream()
                .map(Supplier::get)
                .filter(block -> block instanceof InitializableBlock)
                .map(block -> (InitializableBlock) block)
                .forEach(block -> block.registerPackets(packetHandler));

        items.getEntries().stream()
                .map(Supplier::get)
                .filter(item -> item instanceof InitializableItem)
                .map(item -> (InitializableItem) item)
                .forEach(item -> item.registerPackets(packetHandler));
    }

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event) {
        blocks.getEntries().stream()
                .map(Supplier::get)
                .filter(block -> block instanceof InitializableBlock)
                .map(block -> (InitializableBlock) block)
                .forEach(block -> block.commonInit(TetraMod.packetHandler));
        items.getEntries().stream()
                .map(Supplier::get)
                .filter(item -> item instanceof InitializableItem)
                .map(item -> (InitializableItem) item)
                .forEach(item -> item.commonInit(TetraMod.packetHandler));
    }

    @SubscribeEvent
    public static void buildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == defaultCreativeTabs.getKey()) {
            event.accept(basicWorkbench.get());
            event.accept(ModularHolosphereItem.getCreativeItemStack());
            event.accept(rack.get());
            event.acceptAll(ModularDoubleHeadedItem.getCreativeTabItemStacks());
            event.acceptAll(ModularBladedItem.getCreativeTabItemStacks());
            event.acceptAll(ModularToolbeltItem.getCreativeTabItemStacks());

            event.accept(geode.get());
            event.accept(pristineLapis.get());
            event.accept(pristineEmerald.get());
            event.accept(pristineDiamond.get());
            event.accept(pristineAmethyst.get());
//            event.accept(pristineQuartz);
            event.accept(dragonSinew.get());

            event.acceptAll(ScrollItem.instance.getCreativeTabItems());

            event.accept(bolt.get());
            event.accept(beam.get());
            event.accept(mesh.get());
            event.accept(quickLatch.get());
            event.accept(MetalScrapItem.instance.get());
            event.accept(insulatedPlate.get());
            event.accept(planarStabilizer.get());
            event.accept(CombustionChamberItem.instance.get());
            event.accept(LubricantDispenserItem.instance.get());
            event.accept(ThermalCellItem.instance.get());
            event.accept(earthpiercer.get());
            event.accept(stonecutter.get());
            event.accept(chthonicExtractorItem.get());
            event.accept(forgedWall.get());
            event.accept(forgedPillar.get());
            event.accept(forgedPlatform.get());
            event.accept(forgedPlatformSlab.get());
            event.accept(forgedVent.get());
            event.accept(forgeHammer.get());
            event.accept(forgedWorkbench.get());
            event.accept(ForgedContainerBlock.instance.get());
            event.accept(forgedCrate.get());
            event.accept(transferUnit.get());
            event.accept(CoreExtractorBaseBlock.instance.get());
            event.accept(CoreExtractorPipeBlock.instance);
            event.accept(seepingBedrock.get());
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // enqueueWork swallows exceptions without logging
            try {
                blocks.getEntries().stream()
                        .map(Supplier::get)
                        .filter(block -> block instanceof InitializableBlock)
                        .map(block -> (InitializableBlock) block)
                        .forEach(InitializableBlock::clientInit);
                items.getEntries().stream()
                        .map(Supplier::get)
                        .filter(item -> item instanceof InitializableItem)
                        .map(item -> (InitializableItem) item)
                        .forEach(InitializableItem::clientInit);

                NeoForge.EVENT_BUS.register(new InteractiveBlockOverlay());
//                NeoForge.EVENT_BUS.register(MultiblockSchematicScrollHandler.class);

                HoloStatsGui.initializeStaticBars();
                WorkbenchStatsGui.initializeStaticBars();
                StatSorters.initializeStaticSorters();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static <T> Supplier<T> assigning(Supplier<T> factory, Consumer<? super T> assignment) {
        return () -> {
            T value = factory.get();
            assignment.accept(value);
            return value;
        };
    }

    private static <R, T extends R> DeferredHolder<R, T> register(DeferredRegister<R> registry, String id, Supplier<T> supplier) {
        return registry.register(id, supplier);
    }

    private static <R, T extends R> DeferredHolder<R, T> register(DeferredRegister<R> registry, String id, Supplier<T> supplier,
            Consumer<? super T> assignment) {
        return registry.register(id, assigning(supplier, assignment));
    }

    private static void validateTierOrdering() {
        List<ResourceLocation> expectedOrder = List.of(
                ResourceLocation.withDefaultNamespace("wood"),
                ResourceLocation.withDefaultNamespace("gold"),
                ResourceLocation.withDefaultNamespace("stone"),
                ResourceLocation.withDefaultNamespace("iron"),
                ResourceLocation.withDefaultNamespace("diamond"),
                ResourceLocation.withDefaultNamespace("netherite"),
                ResourceLocation.fromNamespaceAndPath(TetraMod.MOD_ID, "maxed_forge_hammer")
        );

        Map<ResourceLocation, Integer> indexes = new HashMap<>();
        List<ResourceLocation> resolvedOrder = HarvestTierRegistry.ordered().stream()
                .map(HarvestTierRegistry::nameOf)
                .toList();

        for (int i = 0; i < resolvedOrder.size(); i++) {
            ResourceLocation name = resolvedOrder.get(i);
            if (name != null) {
                indexes.put(name, i);
            }
        }

        ResourceLocation previous = null;
        int previousIndex = -1;
        for (ResourceLocation expected : expectedOrder) {
            Integer index = indexes.get(expected);
            if (index == null) {
                throw new IllegalStateException("Missing expected tier " + expected + " in resolved order " + resolvedOrder);
            }
            if (index <= previousIndex) {
                throw new IllegalStateException("Unexpected tier order, expected " + previous + " before " + expected + ": " + resolvedOrder);
            }
            previous = expected;
            previousIndex = index;
        }
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, WorkbenchTile.type.get(), WorkbenchTile::getItemHandler);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, RackTile.type, RackTile::getItemHandler);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ForgedContainerBlockEntity.type.get(), ForgedContainerBlockEntity::getItemHandler);
    }
}
