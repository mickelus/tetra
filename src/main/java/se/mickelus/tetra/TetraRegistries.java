package se.mickelus.tetra;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
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
import se.mickelus.tetra.blocks.forged.chthonic.*;
import se.mickelus.tetra.blocks.forged.extractor.SeepingBedrockBlock;
import se.mickelus.tetra.blocks.geode.*;
import se.mickelus.tetra.blocks.geode.particle.SparkleParticle;
import se.mickelus.tetra.blocks.geode.particle.SparkleParticleType;
import se.mickelus.tetra.blocks.rack.RackBlock;
import se.mickelus.tetra.blocks.rack.RackTile;
import se.mickelus.tetra.blocks.salvage.InteractiveBlockOverlay;
import se.mickelus.tetra.blocks.scroll.*;
import se.mickelus.tetra.blocks.workbench.BasicWorkbenchBlock;
import se.mickelus.tetra.blocks.workbench.WorkbenchContainer;
import se.mickelus.tetra.blocks.workbench.WorkbenchTESR;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;
import se.mickelus.tetra.blocks.workbench.gui.WorkbenchScreen;
import se.mickelus.tetra.client.model.ModularModelLoader;
import se.mickelus.tetra.compat.botania.BotaniaCompat;
import se.mickelus.tetra.crafting.ScrollIngredient;
import se.mickelus.tetra.effect.gui.AbilityOverlays;
import se.mickelus.tetra.effect.howling.HowlingOverlay;
import se.mickelus.tetra.effect.howling.HowlingPotionEffect;
import se.mickelus.tetra.effect.potion.*;
import se.mickelus.tetra.generation.ExtendedStructureRenderer;
import se.mickelus.tetra.items.InitializableItem;
import se.mickelus.tetra.items.TetraItemGroup;
import se.mickelus.tetra.items.forged.*;
import se.mickelus.tetra.items.loot.DragonSinewItem;
import se.mickelus.tetra.items.modular.ItemPredicateModular;
import se.mickelus.tetra.items.modular.MaterialItemPredicate;
import se.mickelus.tetra.items.modular.ThrownModularItemEntity;
import se.mickelus.tetra.items.modular.ThrownModularItemRenderer;
import se.mickelus.tetra.items.modular.impl.ModularBladedItem;
import se.mickelus.tetra.items.modular.impl.ModularDoubleHeadedItem;
import se.mickelus.tetra.items.modular.impl.ModularSingleHeadedItem;
import se.mickelus.tetra.items.modular.impl.bow.ModularBowItem;
import se.mickelus.tetra.items.modular.impl.crossbow.ModularCrossbowItem;
import se.mickelus.tetra.items.modular.impl.crossbow.ShootableDummyItem;
import se.mickelus.tetra.items.modular.impl.holo.ModularHolosphereItem;
import se.mickelus.tetra.items.modular.impl.shield.ModularShieldBannerModel;
import se.mickelus.tetra.items.modular.impl.shield.ModularShieldItem;
import se.mickelus.tetra.items.modular.impl.shield.ModularShieldModel;
import se.mickelus.tetra.items.modular.impl.shield.ModularShieldRenderer;
import se.mickelus.tetra.items.modular.impl.toolbelt.ModularToolbeltItem;
import se.mickelus.tetra.items.modular.impl.toolbelt.ToolbeltContainer;
import se.mickelus.tetra.items.modular.impl.toolbelt.suspend.SuspendPotionEffect;
import se.mickelus.tetra.loot.FortuneBonusCondition;
import se.mickelus.tetra.loot.ReplaceTableModifier;
import se.mickelus.tetra.loot.ScrollDataFunction;
import se.mickelus.tetra.properties.ReachEntityFix;

public class TetraRegistries {
    public static final DeferredRegister<Block> blocks = DeferredRegister.create(ForgeRegistries.BLOCKS, TetraMod.MOD_ID);
    public static final DeferredRegister<Item> items = DeferredRegister.create(ForgeRegistries.ITEMS, TetraMod.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> blockEntities = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, TetraMod.MOD_ID);
    public static final DeferredRegister<MenuType<?>> containers = DeferredRegister.create(ForgeRegistries.CONTAINERS, TetraMod.MOD_ID);
    public static final DeferredRegister<EntityType<?>> entities = DeferredRegister.create(ForgeRegistries.ENTITIES, TetraMod.MOD_ID);
    public static final DeferredRegister<StructureFeature<?>> structures = DeferredRegister.create(ForgeRegistries.STRUCTURE_FEATURES, TetraMod.MOD_ID);
    public static final DeferredRegister<ParticleType<?>> particles = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, TetraMod.MOD_ID);
    public static final DeferredRegister<MobEffect> effects = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, TetraMod.MOD_ID);
    public static final DeferredRegister<GlobalLootModifierSerializer<?>> lootModifiers = DeferredRegister.create(ForgeRegistries.Keys.LOOT_MODIFIER_SERIALIZERS, TetraMod.MOD_ID);

    public static final DeferredRegister<LootItemConditionType> lootConditions = DeferredRegister.create(Registry.LOOT_ITEM_REGISTRY, TetraMod.MOD_ID);
    public static final DeferredRegister<LootItemFunctionType> lootFunctions = DeferredRegister.create(Registry.LOOT_FUNCTION_REGISTRY, TetraMod.MOD_ID);

    private static final Item.Properties itemProperties = new Item.Properties().tab(TetraItemGroup.instance);

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
        structures.register(bus);

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

//                new HammerHeadBlock(),
//                new HammerBaseBlock(),
//                new BlockForgedWall(),
//                new BlockForgedPillar(),
//                new BlockForgedPlatform(),
//                new BlockForgedPlatformSlab(),
//                new ForgedVentBlock(),
//                new ForgedWorkbenchBlock(),
//                new ForgedContainerBlock(),
//                new ForgedCrateBlock(),
//                new TransferUnitBlock(),
//                new CoreExtractorBaseBlock(),
//                new CoreExtractorPistonBlock(),
//                new CoreExtractorPipeBlock(),

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
        items.register(ModularToolbeltItem.identifier, ModularToolbeltItem::new);
        items.register(GeodeItem.identifier, GeodeItem::new);
        items.register(PristineLapisItem.identifier, PristineLapisItem::new);
        items.register(PristineEmeraldItem.unlocalizedName, PristineEmeraldItem::new);
        items.register(PristineDiamondItem.unlocalizedName, PristineDiamondItem::new);


        items.register(ItemBolt.unlocalizedName, ItemBolt::new);
        items.register(ItemBeam.unlocalizedName, ItemBeam::new);
        items.register(ItemMesh.unlocalizedName, ItemMesh::new);
        items.register(ItemQuickLatch.unlocalizedName, ItemQuickLatch::new);
        items.register(ItemMetalScrap.unlocalizedName, ItemMetalScrap::new);
        items.register(InsulatedPlateItem.unlocalizedName, InsulatedPlateItem::new);
        items.register(PlanarStabilizerItem.unlocalizedName, PlanarStabilizerItem::new);
        items.register(ModularHolosphereItem.identifier, ModularHolosphereItem::new);
        items.register(EarthpiercerItem.unlocalizedName, EarthpiercerItem::new);
        items.register(StonecutterItem.unlocalizedName, StonecutterItem::new);
        items.register(DragonSinewItem.unlocalizedName, DragonSinewItem::new);
        items.register(ScrollItem.identifier, () -> new ScrollItem(rolledScroll.get()));

//      new CombustionChamberItem()
//      new LubricantDispenser()
//      new ItemCellMagmatic()
//      new ReverberatingPearlItem()


        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // BLOCK ENTITIES
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        blockEntities.register(WorkbenchTile.identifier,
                () -> BlockEntityType.Builder.of(WorkbenchTile::new, basicWorkbench.get()).build(null));
        blockEntities.register(ChthonicExtractorBlock.identifier,
                () -> BlockEntityType.Builder.of(ChthonicExtractorTile::new, chthonicExtractor.get()).build(null));
        blockEntities.register(FracturedBedrockBlock.identifier,
                () -> BlockEntityType.Builder.of(FracturedBedrockTile::new, fracturedBedrock.get()).build(null));
        blockEntities.register(RackBlock.identifier,
                () -> BlockEntityType.Builder.of(RackTile::new, rack.get()).build(null));
        blockEntities.register(ScrollTile.identifier,
                () -> BlockEntityType.Builder.of(ScrollTile::new, openScroll.get(), wallScroll.get(), rolledScroll.get()).build(null));

//        blockEntities.register(HammerBaseBlock.identifier, () -> BlockEntityType.Builder.of(HammerBaseTile::new, HammerBaseBlock.instance)
//                .build(null));
//
//        blockEntities.register(HammerHeadBlock.identifier, () -> BlockEntityType.Builder.of(HammerHeadTile::new, HammerHeadBlock.instance)
//                .build(null));
//
//        blockEntities.register(TransferUnitBlock.identifier, () -> BlockEntityType.Builder.of(TransferUnitTile::new, TransferUnitBlock.instance)
//                .build(null));
//
//        blockEntities.register(CoreExtractorBaseBlock.identifier, () -> BlockEntityType.Builder.of(CoreExtractorBaseTile::new, CoreExtractorBaseBlock.instance)
//                .build(null));
//
//        blockEntities.register(CoreExtractorPistonBlock.identifier, () -> BlockEntityType.Builder.of(CoreExtractorPistonTile::new, CoreExtractorPistonBlock.instance)
//                .build(null));
//
//        blockEntities.register(ForgedContainerBlock.identifier, () -> BlockEntityType.Builder.of(ForgedContainerTile::new, ForgedContainerBlock.instance)
//                .build(null));


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
        containers.register(ModularToolbeltItem.identifier,
                () -> IForgeMenuType.create(((windowId, inv, data) -> ToolbeltContainer.create(windowId, inv))));

        // workbench
        containers.register(WorkbenchTile.identifier,
                () -> IForgeMenuType.create(((windowId, inv, data) -> WorkbenchContainer.create(windowId, data.readBlockPos(), inv))));

//        // forged container
//        containers.register(ForgedContainerBlock.identifier,
//                () -> IForgeMenuType.create(((windowId, inv, data) -> ForgedContainerContainer.create(windowId, data.readBlockPos(), inv))));


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
        lootModifiers.register("replace_table", ReplaceTableModifier.Serializer::new);

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // STRUCTURES
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // INGREDIENT SERIALIZERS
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        CraftingHelper.register(new ResourceLocation(TetraMod.MOD_ID, "scroll"), ScrollIngredient.Serializer.instance);
    }

    public static <B extends Block> RegistryObject<Item> registerBlockItem(RegistryObject<B> block) {
        return items.register(block.getId().getPath(), () -> new BlockItem(block.get(), itemProperties));
    }

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
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
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
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

            // these are registered here as there are multiple instances of workbench blocks
            MenuScreens.register(WorkbenchTile.containerType, WorkbenchScreen::new);

            MinecraftForge.EVENT_BUS.register(new HowlingOverlay(Minecraft.getInstance()));
            MinecraftForge.EVENT_BUS.register(new AbilityOverlays(Minecraft.getInstance()));
            // todo 1.18.2: was in postInit, still works?
            MinecraftForge.EVENT_BUS.register(new InteractiveBlockOverlay());

            BotaniaCompat.clientInit();

            MinecraftForge.EVENT_BUS.register(ReachEntityFix.class);
        });
    }

    @SubscribeEvent
    public static void registerParticleFactory(ParticleFactoryRegisterEvent event) {
        Minecraft.getInstance().particleEngine.register(SparkleParticleType.instance, SparkleParticle.Provider::new);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void modelRegistryReady(ModelRegistryEvent event) {
        ModelLoaderRegistry.registerLoader(new ResourceLocation(TetraMod.MOD_ID, "modular_loader"), new ModularModelLoader());
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void registerEntityLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
//        event.registerLayerDefinition(ForgedContainerRenderer.layer, ForgedContainerRenderer::createLayer);
//        event.registerLayerDefinition(HammerBaseRenderer.layer, HammerBaseRenderer::createLayer);

        event.registerLayerDefinition(ScrollRenderer.layer, ScrollRenderer::createLayer);
        event.registerLayerDefinition(ModularShieldRenderer.layer, ModularShieldModel::createLayer);
        event.registerLayerDefinition(ModularShieldRenderer.bannerLayer, ModularShieldBannerModel::createLayer);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ExtractorProjectileEntity.type, ExtractorProjectileRenderer::new);
        event.registerEntityRenderer(ThrownModularItemEntity.type, ThrownModularItemRenderer::new);

        event.registerBlockEntityRenderer(WorkbenchTile.type, WorkbenchTESR::new);
        event.registerBlockEntityRenderer(ScrollTile.type, ScrollRenderer::new);

//        event.registerBlockEntityRenderer(ForgedContainerTile.type, ForgedContainerRenderer::new);
//        event.registerBlockEntityRenderer(CoreExtractorPistonTile.type, CoreExtractorPistonRenderer::new);
//        event.registerBlockEntityRenderer(HammerBaseTile.type, HammerBaseRenderer::new);
//        event.registerBlockEntityRenderer(HammerHeadTile.type, HammerHeadTESR::new);

        if (ConfigHandler.development.get()) {
            event.registerBlockEntityRenderer(BlockEntityType.STRUCTURE_BLOCK, ExtendedStructureRenderer::new);
        }
    }
}
