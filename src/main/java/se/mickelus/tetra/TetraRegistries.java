package se.mickelus.tetra;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import se.mickelus.tetra.advancements.BlockInteractionCriterion;
import se.mickelus.tetra.advancements.BlockUseCriterion;
import se.mickelus.tetra.advancements.ImprovementCraftCriterion;
import se.mickelus.tetra.advancements.ModuleCraftCriterion;
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
import se.mickelus.tetra.crafting.ScrollIngredient;
import se.mickelus.tetra.effect.howling.HowlingPotionEffect;
import se.mickelus.tetra.effect.potion.*;
import se.mickelus.tetra.items.InitializableItem;
import se.mickelus.tetra.items.TetraItemGroup;
import se.mickelus.tetra.items.cell.ThermalCellItem;
import se.mickelus.tetra.items.forged.*;
import se.mickelus.tetra.items.loot.DragonSinewItem;
import se.mickelus.tetra.items.modular.ItemPredicateModular;
import se.mickelus.tetra.items.modular.MaterialItemPredicate;
import se.mickelus.tetra.items.modular.ThrownModularItemEntity;
import se.mickelus.tetra.items.modular.impl.ModularBladedItem;
import se.mickelus.tetra.items.modular.impl.ModularDoubleHeadedItem;
import se.mickelus.tetra.items.modular.impl.ModularSingleHeadedItem;
import se.mickelus.tetra.items.modular.impl.bow.ModularBowItem;
import se.mickelus.tetra.items.modular.impl.crossbow.ModularCrossbowItem;
import se.mickelus.tetra.items.modular.impl.crossbow.ShootableDummyItem;
import se.mickelus.tetra.items.modular.impl.holo.ModularHolosphereItem;
import se.mickelus.tetra.items.modular.impl.shield.ModularShieldItem;
import se.mickelus.tetra.items.modular.impl.toolbelt.ModularToolbeltItem;
import se.mickelus.tetra.items.modular.impl.toolbelt.ToolbeltContainer;
import se.mickelus.tetra.items.modular.impl.toolbelt.suspend.SuspendPotionEffect;
import se.mickelus.tetra.levelgen.*;
import se.mickelus.tetra.loot.FortuneBonusCondition;
import se.mickelus.tetra.loot.ReplaceTableModifier;
import se.mickelus.tetra.loot.ScrollDataFunction;

import java.util.List;

public class TetraRegistries {
    public static final DeferredRegister<Block> blocks = DeferredRegister.create(ForgeRegistries.BLOCKS, TetraMod.MOD_ID);
    public static final DeferredRegister<Item> items = DeferredRegister.create(ForgeRegistries.ITEMS, TetraMod.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> blockEntities = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, TetraMod.MOD_ID);
    public static final DeferredRegister<MenuType<?>> containers = DeferredRegister.create(ForgeRegistries.MENU_TYPES, TetraMod.MOD_ID);
    public static final DeferredRegister<EntityType<?>> entities = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, TetraMod.MOD_ID);
    public static final DeferredRegister<ParticleType<?>> particles = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, TetraMod.MOD_ID);
    public static final DeferredRegister<MobEffect> effects = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, TetraMod.MOD_ID);
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> lootModifiers = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, TetraMod.MOD_ID);

    public static final DeferredRegister<LootItemConditionType> lootConditions = DeferredRegister.create(Registry.LOOT_ITEM_REGISTRY, TetraMod.MOD_ID);
    public static final DeferredRegister<LootItemFunctionType> lootFunctions = DeferredRegister.create(Registry.LOOT_FUNCTION_REGISTRY, TetraMod.MOD_ID);
    public static final DeferredRegister<StructureProcessorType<?>> structureProcessors = DeferredRegister.create(Registry.STRUCTURE_PROCESSOR_REGISTRY, TetraMod.MOD_ID);

    public static final TagKey<Block> forgeHammerBreakTag = BlockTags.create(new ResourceLocation("tetra:needs_forge_hammer_tool"));
    public static final Tier forgeHammerTier = TierSortingRegistry.registerTier(new ForgeTier(Tiers.NETHERITE.getLevel() + 1, 0, 0, 0, 0,
            forgeHammerBreakTag, () -> Ingredient.EMPTY), new ResourceLocation("tetra:maxed_forge_hammer"), List.of(Tiers.NETHERITE), List.of());

    private static Item.Properties itemProperties;

    public static void init(IEventBus bus) {
        bus.register(TetraRegistries.class);

        blocks.register(bus);
        items.register(bus);
        blockEntities.register(bus);
        entities.register(bus);
        particles.register(bus);
        containers.register(bus);
        effects.register(bus);
        lootConditions.register(bus);
        lootFunctions.register(bus);
        lootModifiers.register(bus);
        structureProcessors.register(bus);

        new TetraItemGroup();
        itemProperties = new Item.Properties().tab(TetraItemGroup.instance);

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // BLOCKS
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        RegistryObject<BasicWorkbenchBlock> basicWorkbench = blocks.register(BasicWorkbenchBlock.identifier, BasicWorkbenchBlock::new);
        registerBlockItem(basicWorkbench);
        blocks.register(GeodeBlock.identifier, GeodeBlock::new);
        RegistryObject<SeepingBedrockBlock> seepingBedrock = blocks.register(SeepingBedrockBlock.identifier, SeepingBedrockBlock::new);
        registerBlockItem(seepingBedrock);

        RegistryObject<RackBlock> rack = blocks.register(RackBlock.identifier, RackBlock::new);
        registerBlockItem(rack);

        RegistryObject<ChthonicExtractorBlock> chthonicExtractor = blocks.register(ChthonicExtractorBlock.identifier, ChthonicExtractorBlock::new);
        ChthonicExtractorBlock.registerItems(items);
        RegistryObject<FracturedBedrockBlock> fracturedBedrock = blocks.register(FracturedBedrockBlock.identifier, FracturedBedrockBlock::new);
        blocks.register(DepletedBedrockBlock.identifier, DepletedBedrockBlock::new);

        RegistryObject<RolledScrollBlock> rolledScroll = blocks.register(RolledScrollBlock.identifier, RolledScrollBlock::new);
        RegistryObject<WallScrollBlock> wallScroll = blocks.register(WallScrollBlock.identifier, WallScrollBlock::new);
        RegistryObject<OpenScrollBlock> openScroll = blocks.register(OpenScrollBlock.identifier, OpenScrollBlock::new);

        registerBlockItem(blocks.register(ForgedWallBlock.identifier, ForgedWallBlock::new));
        registerBlockItem(blocks.register(ForgedPillarBlock.identifier, ForgedPillarBlock::new));
        registerBlockItem(blocks.register(ForgedPlatformBlock.identifier, ForgedPlatformBlock::new));
        registerBlockItem(blocks.register(ForgedPlatformSlabBlock.identifier, ForgedPlatformSlabBlock::new));
        registerBlockItem(blocks.register(ForgedVentBlock.identifier, ForgedVentBlock::new));
        blocks.register(HammerHeadBlock.identifier, HammerHeadBlock::new);
        registerBlockItem(blocks.register(HammerBaseBlock.identifier, HammerBaseBlock::new));
        RegistryObject<ForgedWorkbenchBlock> forgedWorkbench = blocks.register(ForgedWorkbenchBlock.identifier, ForgedWorkbenchBlock::new);
        registerBlockItem(forgedWorkbench);
        ForgedContainerBlock.instance = blocks.register(ForgedContainerBlock.identifier, ForgedContainerBlock::new);
        registerBlockItem(ForgedContainerBlock.instance);
        registerBlockItem(blocks.register(ForgedCrateBlock.identifier, ForgedCrateBlock::new));
        RegistryObject<TransferUnitBlock> transferUnit = blocks.register(TransferUnitBlock.identifier, TransferUnitBlock::new);
        registerBlockItem(transferUnit);
        CoreExtractorBaseBlock.instance = blocks.register(CoreExtractorBaseBlock.identifier, CoreExtractorBaseBlock::new);
        registerBlockItem(CoreExtractorBaseBlock.instance);
        CoreExtractorPistonBlock.instance = blocks.register(CoreExtractorPistonBlock.identifier, CoreExtractorPistonBlock::new);
        registerBlockItem(blocks.register(CoreExtractorPipeBlock.identifier, CoreExtractorPipeBlock::new));
        HolosphereBlock.instance = blocks.register(HolosphereBlock.identifier, HolosphereBlock::new);

        new MultiblockSchematicBlock.Builder("stonecutter", 3, 2, ForgedBlockCommon.propertiesSolid)
                .build(blocks, items);

        new MultiblockSchematicBlock.Builder("earthpiercer", 2, 2, ForgedBlockCommon.propertiesSolid)
                .build(blocks, items);

        new MultiblockSchematicBlock.Builder("extractor", 3, 3, ForgedBlockCommon.propertiesSolid)
                .build(blocks, items);

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // ITEMS
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        items.register(ModularBladedItem.identifier, ModularBladedItem::new);
        items.register(ModularDoubleHeadedItem.identifier, ModularDoubleHeadedItem::new);
        items.register(ModularBowItem.identifier, ModularBowItem::new);
        RegistryObject<Item> shootableDummy = items.register(ShootableDummyItem.identifier, ShootableDummyItem::new);
        items.register(ModularCrossbowItem.identifier, () -> new ModularCrossbowItem(shootableDummy.get()));
        items.register(ModularSingleHeadedItem.identifier, ModularSingleHeadedItem::new);
        items.register(ModularShieldItem.identifier, ModularShieldItem::new);
        ModularToolbeltItem.instance = items.register(ModularToolbeltItem.identifier, ModularToolbeltItem::new);
        items.register(GeodeItem.identifier, GeodeItem::new);
        items.register(PristineLapisItem.identifier, PristineLapisItem::new);
        items.register(PristineEmeraldItem.unlocalizedName, PristineEmeraldItem::new);
        items.register(PristineDiamondItem.unlocalizedName, PristineDiamondItem::new);


        items.register(BoltItem.unlocalizedName, BoltItem::new);
        items.register(BeamItem.unlocalizedName, BeamItem::new);
        items.register(MeshItem.unlocalizedName, MeshItem::new);
        items.register(QuickLatchItem.unlocalizedName, QuickLatchItem::new);
        MetalScrapItem.instance = items.register(MetalScrapItem.unlocalizedName, MetalScrapItem::new);
        items.register(InsulatedPlateItem.unlocalizedName, InsulatedPlateItem::new);
        items.register(PlanarStabilizerItem.unlocalizedName, PlanarStabilizerItem::new);
        ThermalCellItem.instance = items.register(ThermalCellItem.identifier, ThermalCellItem::new);
        CombustionChamberItem.instance = items.register(CombustionChamberItem.identifier, CombustionChamberItem::new);
        LubricantDispenserItem.instance = items.register(LubricantDispenserItem.identifier, LubricantDispenserItem::new);
        items.register(ModularHolosphereItem.identifier, ModularHolosphereItem::new);
        items.register(EarthpiercerItem.unlocalizedName, EarthpiercerItem::new);
        items.register(StonecutterItem.unlocalizedName, StonecutterItem::new);
        items.register(DragonSinewItem.identifier, DragonSinewItem::new);
//        ReverberatingPearlItem.instance = items.register(ReverberatingPearlItem.identifier, ReverberatingPearlItem::new);

        items.register(ScrollItem.identifier, () -> new ScrollItem(rolledScroll.get()));


        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // BLOCK ENTITIES
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        WorkbenchTile.type = blockEntities.register(WorkbenchTile.identifier,
                () -> BlockEntityType.Builder.of(WorkbenchTile::new, basicWorkbench.get(), forgedWorkbench.get()).build(null));
        ChthonicExtractorTile.type = blockEntities.register(ChthonicExtractorBlock.identifier,
                () -> BlockEntityType.Builder.of(ChthonicExtractorTile::new, chthonicExtractor.get()).build(null));
        blockEntities.register(FracturedBedrockBlock.identifier,
                () -> BlockEntityType.Builder.of(FracturedBedrockTile::new, fracturedBedrock.get()).build(null));
        blockEntities.register(RackBlock.identifier,
                () -> BlockEntityType.Builder.of(RackTile::new, rack.get()).build(null));
        blockEntities.register(ScrollTile.identifier,
                () -> BlockEntityType.Builder.of(ScrollTile::new, openScroll.get(), wallScroll.get(), rolledScroll.get()).build(null));

        HammerBaseBlockEntity.type = blockEntities.register(HammerBaseBlock.identifier,
                () -> BlockEntityType.Builder.of(HammerBaseBlockEntity::new, HammerBaseBlock.instance).build(null));
        HammerHeadBlockEntity.type = blockEntities.register(HammerHeadBlock.identifier,
                () -> BlockEntityType.Builder.of(HammerHeadBlockEntity::new, HammerHeadBlock.instance).build(null));
        TransferUnitBlockEntity.type = blockEntities.register(TransferUnitBlock.identifier,
                () -> BlockEntityType.Builder.of(TransferUnitBlockEntity::new, transferUnit.get()).build(null));
        blockEntities.register(CoreExtractorBaseBlock.identifier,
                () -> BlockEntityType.Builder.of(CoreExtractorBaseBlockEntity::new, CoreExtractorBaseBlock.instance.get()).build(null));
        CoreExtractorPistonBlockEntity.type = blockEntities.register(CoreExtractorPistonBlock.identifier,
                () -> BlockEntityType.Builder.of(CoreExtractorPistonBlockEntity::new, CoreExtractorPistonBlock.instance.get()).build(null));
        ForgedContainerBlockEntity.type = blockEntities.register(ForgedContainerBlock.identifier,
                () -> BlockEntityType.Builder.of(ForgedContainerBlockEntity::new, ForgedContainerBlock.instance.get())
                        .build(null));
        HolosphereBlockEntity.type = blockEntities.register(HolosphereBlock.identifier,
                () -> BlockEntityType.Builder.of(HolosphereBlockEntity::new, HolosphereBlock.instance.get())
                        .build(null));

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // ENTITIES
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        entities.register(ThrownModularItemEntity.unlocalizedName, () ->
                EntityType.Builder.<ThrownModularItemEntity>of(ThrownModularItemEntity::new, MobCategory.MISC)
                        .setCustomClientFactory(ThrownModularItemEntity::new)
                        .sized(0.5F, 0.5F)
                        .build(ThrownModularItemEntity.unlocalizedName)
        );

        entities.register(ExtractorProjectileEntity.unlocalizedName, () ->
                EntityType.Builder.<ExtractorProjectileEntity>of(ExtractorProjectileEntity::new, MobCategory.MISC)
                        .setCustomClientFactory(ExtractorProjectileEntity::new)
                        .sized(0.5F, 0.5F)
                        .build(ExtractorProjectileEntity.unlocalizedName)
        );

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // PARTICLES
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        particles.register(SparkleParticleType.identifier, () -> new SimpleParticleType(false));


        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // CONTAINERS
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // toolbelt
        ToolbeltContainer.type = containers.register(ModularToolbeltItem.identifier,
                () -> IForgeMenuType.create(((windowId, inv, data) -> ToolbeltContainer.create(windowId, inv))));

        // workbench
        WorkbenchContainer.containerType = containers.register(WorkbenchTile.identifier,
                () -> IForgeMenuType.create(((windowId, inv, data) -> WorkbenchContainer.create(windowId, data.readBlockPos(), inv))));

        // forged container
        ForgedContainerMenu.type = containers.register(ForgedContainerBlock.identifier,
                () -> IForgeMenuType.create(((windowId, inv, data) -> ForgedContainerMenu.create(windowId, data.readBlockPos(), inv))));


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

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // LOOT CONDITIONS
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        FortuneBonusCondition.type = lootConditions.register(FortuneBonusCondition.identifier, () -> new LootItemConditionType(new FortuneBonusCondition.ConditionSerializer()));

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // LOOT FUNCTIONS
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ScrollDataFunction.type = lootFunctions.register(ScrollDataFunction.identifier, () -> new LootItemFunctionType(new ScrollDataFunction.Serializer()));

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // LOOT MODIFIERS
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        lootModifiers.register("replace_table", ReplaceTableModifier.codec);

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // STRUCTURE PROCESSORS
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ForgedHammerProcessor.type = registerStructureProcessor("hammer", () -> ForgedHammerProcessor.codec);
        ForgedCrateProcessor.type = registerStructureProcessor("crate", () -> ForgedCrateProcessor.codec);
        ForgedContainerProcessor.type = registerStructureProcessor("container", () -> ForgedContainerProcessor.codec);
        TransferUnitProcessor.type = registerStructureProcessor("transfer_unit", () -> TransferUnitProcessor.codec);
        MultiblockSchematicProcessor.type = registerStructureProcessor("multiblock_schematic", () -> MultiblockSchematicProcessor.codec);

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // INGREDIENT SERIALIZERS
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        CraftingHelper.register(new ResourceLocation(TetraMod.MOD_ID, "scroll"), ScrollIngredient.Serializer.instance);
    }

    public static <B extends Block> RegistryObject<Item> registerBlockItem(RegistryObject<B> block) {
        return items.register(block.getId().getPath(), () -> new BlockItem(block.get(), itemProperties));
    }

    public static <P extends StructureProcessor> RegistryObject<StructureProcessorType<?>> registerStructureProcessor(String id, StructureProcessorType<P> type) {
        return structureProcessors.register(id, () -> type);
    }

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // enqueueWork swallows exceptions without logging
            try {
                //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                // ADVANCEMENT CRITERIA
                //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                CriteriaTriggers.register(BlockUseCriterion.trigger);
                CriteriaTriggers.register(BlockInteractionCriterion.trigger);
                CriteriaTriggers.register(ModuleCraftCriterion.trigger);
                CriteriaTriggers.register(ImprovementCraftCriterion.trigger);

                //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                // ITEM PREDICATES
                //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                ItemPredicate.register(new ResourceLocation("tetra:modular_item"), ItemPredicateModular::new);
                ItemPredicate.register(new ResourceLocation("tetra:material"), MaterialItemPredicate::new);
                ItemPredicate.register(new ResourceLocation("tetra:loose"), LooseItemPredicate::new);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        blocks.getEntries().stream()
                .map(RegistryObject::get)
                .filter(block -> block instanceof InitializableBlock)
                .map(block -> (InitializableBlock) block)
                .forEach(block -> block.commonInit(TetraMod.packetHandler));
        items.getEntries().stream()
                .map(RegistryObject::get)
                .filter(item -> item instanceof InitializableItem)
                .map(item -> (InitializableItem) item)
                .forEach(item -> item.commonInit(TetraMod.packetHandler));
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // enqueueWork swallows exceptions without logging
            try {
                blocks.getEntries().stream()
                        .map(RegistryObject::get)
                        .filter(block -> block instanceof InitializableBlock)
                        .map(block -> (InitializableBlock) block)
                        .forEach(InitializableBlock::clientInit);
                items.getEntries().stream()
                        .map(RegistryObject::get)
                        .filter(item -> item instanceof InitializableItem)
                        .map(item -> (InitializableItem) item)
                        .forEach(InitializableItem::clientInit);

                MinecraftForge.EVENT_BUS.register(new InteractiveBlockOverlay());
//                MinecraftForge.EVENT_BUS.register(MultiblockSchematicScrollHandler.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
