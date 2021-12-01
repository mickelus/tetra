package se.mickelus.tetra;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.tetra.advancements.*;
import se.mickelus.tetra.blocks.ITetraBlock;
import se.mickelus.tetra.blocks.forged.*;
import se.mickelus.tetra.blocks.forged.chthonic.*;
import se.mickelus.tetra.blocks.forged.container.ForgedContainerBlock;
import se.mickelus.tetra.blocks.forged.container.ForgedContainerContainer;
import se.mickelus.tetra.blocks.forged.container.ForgedContainerRenderer;
import se.mickelus.tetra.blocks.forged.container.ForgedContainerTile;
import se.mickelus.tetra.blocks.forged.extractor.*;
import se.mickelus.tetra.blocks.forged.hammer.*;
import se.mickelus.tetra.blocks.forged.transfer.TransferUnitBlock;
import se.mickelus.tetra.blocks.forged.transfer.TransferUnitTile;
import se.mickelus.tetra.blocks.geode.*;
import se.mickelus.tetra.blocks.rack.RackBlock;
import se.mickelus.tetra.blocks.rack.RackTile;
import se.mickelus.tetra.blocks.scroll.*;
import se.mickelus.tetra.blocks.workbench.BasicWorkbenchBlock;
import se.mickelus.tetra.blocks.workbench.WorkbenchContainer;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;
import se.mickelus.tetra.client.model.ModularModelLoader;
import se.mickelus.tetra.compat.curios.CuriosCompat;
import se.mickelus.tetra.crafting.ScrollIngredient;
import se.mickelus.tetra.craftingeffect.CraftingEffectRegistry;
import se.mickelus.tetra.craftingeffect.condition.CraftTypeCondition;
import se.mickelus.tetra.craftingeffect.condition.LockedCondition;
import se.mickelus.tetra.craftingeffect.condition.MaterialCondition;
import se.mickelus.tetra.craftingeffect.condition.ToolCondition;
import se.mickelus.tetra.craftingeffect.outcome.ApplyImprovementOutcome;
import se.mickelus.tetra.craftingeffect.outcome.MaterialReductionOutcome;
import se.mickelus.tetra.craftingeffect.outcome.RemoveImprovementOutcome;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.data.UpdateDataPacket;
import se.mickelus.tetra.data.provider.ModuleProvider;
import se.mickelus.tetra.effect.ItemEffectHandler;
import se.mickelus.tetra.effect.LungeEchoPacket;
import se.mickelus.tetra.effect.TruesweepPacket;
import se.mickelus.tetra.effect.howling.HowlingPacket;
import se.mickelus.tetra.effect.howling.HowlingPotionEffect;
import se.mickelus.tetra.effect.potion.*;
import se.mickelus.tetra.effect.revenge.AddRevengePacket;
import se.mickelus.tetra.effect.revenge.RemoveRevengePacket;
import se.mickelus.tetra.generation.FeatureEntry;
import se.mickelus.tetra.generation.TGenCommand;
import se.mickelus.tetra.items.ITetraItem;
import se.mickelus.tetra.items.TetraItemGroup;
import se.mickelus.tetra.items.cell.ItemCellMagmatic;
import se.mickelus.tetra.items.forged.*;
import se.mickelus.tetra.items.loot.DragonSinewItem;
import se.mickelus.tetra.items.modular.*;
import se.mickelus.tetra.items.modular.impl.ModularBladedItem;
import se.mickelus.tetra.items.modular.impl.ModularDoubleHeadedItem;
import se.mickelus.tetra.items.modular.impl.ModularSingleHeadedItem;
import se.mickelus.tetra.items.modular.impl.bow.ModularBowItem;
import se.mickelus.tetra.items.modular.impl.bow.ProjectileMotionPacket;
import se.mickelus.tetra.items.modular.impl.crossbow.ModularCrossbowItem;
import se.mickelus.tetra.items.modular.impl.holo.ModularHolosphereItem;
import se.mickelus.tetra.items.modular.impl.shield.ModularShieldItem;
import se.mickelus.tetra.items.modular.impl.toolbelt.ModularToolbeltItem;
import se.mickelus.tetra.items.modular.impl.toolbelt.ToolbeltContainer;
import se.mickelus.tetra.items.modular.impl.toolbelt.ToolbeltModule;
import se.mickelus.tetra.items.modular.impl.toolbelt.suspend.SuspendPotionEffect;
import se.mickelus.tetra.loot.FortuneBonusCondition;
import se.mickelus.tetra.loot.ReplaceTableModifier;
import se.mickelus.tetra.loot.ScrollDataFunction;
import se.mickelus.tetra.module.*;
import se.mickelus.tetra.module.improvement.DestabilizationEffect;
import se.mickelus.tetra.module.improvement.HonePacket;
import se.mickelus.tetra.module.improvement.SettlePacket;
import se.mickelus.tetra.module.schematic.BookEnchantSchematic;
import se.mickelus.tetra.module.schematic.CleanseSchematic;
import se.mickelus.tetra.network.PacketHandler;
import se.mickelus.tetra.properties.TetraAttributes;
import se.mickelus.tetra.proxy.ClientProxy;
import se.mickelus.tetra.proxy.IProxy;
import se.mickelus.tetra.proxy.ServerProxy;
import se.mickelus.tetra.trades.TradeHandler;

import java.util.Arrays;
import javax.annotation.ParametersAreNonnullByDefault;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
@Mod(TetraMod.MOD_ID)

@ParametersAreNonnullByDefault
public class TetraMod {
    private static final Logger logger = LogManager.getLogger();

    public static final String MOD_ID = "tetra";

    public static IProxy proxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    public static TetraMod instance;

    private static Item[] items;
    private static Block[] blocks;

    public static PacketHandler packetHandler;

    public TetraMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(CuriosCompat::enqueueIMC);
        TetraAttributes.registry.register(FMLJavaModLoadingContext.get().getModEventBus());

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new ItemEffectHandler());
        MinecraftForge.EVENT_BUS.register(new TradeHandler());
        MinecraftForge.EVENT_BUS.register(new DataManager());
        MinecraftForge.EVENT_BUS.register(TetraMod.proxy);
        MinecraftForge.EVENT_BUS.register(new VibrationDebuffer());
        MinecraftForge.EVENT_BUS.register(ServerScheduler.class);
        MinecraftForge.EVENT_BUS.register(ClientScheduler.class);

        if (ConfigHandler.enableLookTrigger.get()) {
            MinecraftForge.EVENT_BUS.register(BlockLookTrigger.instance);
        }

        ConfigHandler.setup();

        ItemPredicate.register(new ResourceLocation("tetra:modular_item"), ItemPredicateModular::new);
        ItemPredicate.register(new ResourceLocation("tetra:material"), MaterialItemPredicate::new);
        ItemPredicate.register(new ResourceLocation("tetra:loose"), LooseItemPredicate::new);

        Registry.register(Registry.LOOT_CONDITION_TYPE, FortuneBonusCondition.identifier, FortuneBonusCondition.type);
        Registry.register(Registry.LOOT_FUNCTION_TYPE, ScrollDataFunction.identifier, ScrollDataFunction.type);

        new CraftingEffectRegistry();
        CraftingEffectRegistry.registerConditionType("tetra:craft_type", CraftTypeCondition.class);
        CraftingEffectRegistry.registerConditionType("tetra:locked", LockedCondition.class);
        CraftingEffectRegistry.registerConditionType("tetra:material", MaterialCondition.class);
        CraftingEffectRegistry.registerConditionType("tetra:tool", ToolCondition.class);

        CraftingEffectRegistry.registerEffectType("tetra:apply_improvements", ApplyImprovementOutcome.class);
        CraftingEffectRegistry.registerEffectType("tetra:remove_improvements", RemoveImprovementOutcome.class);
        CraftingEffectRegistry.registerEffectType("tetra:material_reduction", MaterialReductionOutcome.class);

        new RepairRegistry();

        SchematicRegistry schematicRegistry = new SchematicRegistry();
        schematicRegistry.registerSchematic(new BookEnchantSchematic());

        new ItemUpgradeRegistry();

        ModuleRegistry moduleRegistry = new ModuleRegistry();
        moduleRegistry.registerModuleType(new ResourceLocation(MOD_ID, "basic_module"), BasicModule::new);
        moduleRegistry.registerModuleType(new ResourceLocation(MOD_ID, "multi_module"), MultiSlotModule::new);
        moduleRegistry.registerModuleType(new ResourceLocation(MOD_ID, "basic_major_module"), BasicMajorModule::new);
        moduleRegistry.registerModuleType(new ResourceLocation(MOD_ID, "multi_major_module"), MultiSlotMajorModule::new);
        moduleRegistry.registerModuleType(new ResourceLocation(MOD_ID, "toolbelt_module"), ToolbeltModule::new);

        new TetraItemGroup();

        CriteriaTriggers.register(BlockLookTrigger.instance);
        CriteriaTriggers.register(BlockUseCriterion.trigger);
        CriteriaTriggers.register(BlockInteractionCriterion.trigger);
        CriteriaTriggers.register(ModuleCraftCriterion.trigger);
        CriteriaTriggers.register(ImprovementCraftCriterion.trigger);

        ScrollBlock scrollRolled = new RolledScrollBlock();

        blocks = new Block[] {
                new BasicWorkbenchBlock(),
                new GeodeBlock(),
                new HammerHeadBlock(),
                new HammerBaseBlock(),
                new BlockForgedWall(),
                new BlockForgedPillar(),
                new BlockForgedPlatform(),
                new BlockForgedPlatformSlab(),
                new ForgedVentBlock(),
                new ForgedWorkbenchBlock(),
                new ForgedContainerBlock(),
                new ForgedCrateBlock(),
                new TransferUnitBlock(),
                new CoreExtractorBaseBlock(),
                new CoreExtractorPistonBlock(),
                new CoreExtractorPipeBlock(),
                new SeepingBedrockBlock(),
                new RackBlock(),
                new ChthonicExtractorBlock(),
                new FracturedBedrockBlock(),
                new DepletedBedrockBlock(),
                scrollRolled,
                new WallScrollBlock(),
                new OpenScrollBlock()
        };

        items = new Item[] {
                new ModularBladedItem(),
                new ModularDoubleHeadedItem(),
                new GeodeItem(),
                new PristineLapisItem(),
                new PristineEmeraldItem(),
                new PristineDiamondItem(),
                new ModularToolbeltItem(),
                new ItemCellMagmatic(),
                new ItemBolt(),
                new ItemBeam(),
                new ItemMesh(),
                new ItemQuickLatch(),
                new ItemMetalScrap(),
                new InsulatedPlateItem(),
                new CombustionChamberItem(),
                new PlanarStabilizerItem(),
                new LubricantDispenser(),
                new ModularHolosphereItem(),
                new EarthpiercerItem(),
                new DragonSinewItem(),
                new ScrollItem(scrollRolled)
//                new ReverberatingPearlItem()
        };

        if (ConfigHandler.enableBow.get()) {
            items = ArrayUtils.addAll(items, new ModularBowItem());
        }

        if (ConfigHandler.enableCrossbow.get()) {
            items = ArrayUtils.addAll(items, new ModularCrossbowItem());
        }

        if (ConfigHandler.enableSingle.get()) {
            items = ArrayUtils.addAll(items, new ModularSingleHeadedItem());
        }

        if (ConfigHandler.enableShield.get()) {
            items = ArrayUtils.addAll(items, new ModularShieldItem());
        }

        if (ConfigHandler.enableStonecutter.get()) {
            items = ArrayUtils.addAll(items, new StonecutterItem());
        }

        proxy.preInit(
                Arrays.stream(items)
                        .filter(item -> item instanceof ITetraItem)
                        .map(item -> (ITetraItem) item).toArray(ITetraItem[]::new),
                Arrays.stream(blocks)
                        .filter(block -> block instanceof ITetraBlock)
                        .map(block -> (ITetraBlock) block).toArray(ITetraBlock[]::new));
    }

    public void setup(FMLCommonSetupEvent event) {
        proxy.init(event,
                Arrays.stream(items)
                        .filter(item -> item instanceof ITetraItem)
                        .map(item -> (ITetraItem) item).toArray(ITetraItem[]::new),
                Arrays.stream(blocks)
                        .filter(block -> block instanceof ITetraBlock)
                        .map(block -> (ITetraBlock) block).toArray(ITetraBlock[]::new));


        packetHandler = new PacketHandler(MOD_ID, "main");

        Arrays.stream(items)
                .filter(item -> item instanceof ITetraItem)
                .map(item -> (ITetraItem) item)
                .forEach(item -> item.init(packetHandler));
        Arrays.stream(blocks)
                .filter(block -> block instanceof ITetraBlock)
                .map(block -> (ITetraBlock) block)
                .forEach(block -> block.init(packetHandler));

        packetHandler.registerPacket(HonePacket.class, HonePacket::new);
        packetHandler.registerPacket(SettlePacket.class, SettlePacket::new);
        packetHandler.registerPacket(UpdateDataPacket.class, UpdateDataPacket::new);
        packetHandler.registerPacket(SecondaryAbilityPacket.class, SecondaryAbilityPacket::new);
        packetHandler.registerPacket(ChargedAbilityPacket.class, ChargedAbilityPacket::new);
        packetHandler.registerPacket(TruesweepPacket.class, TruesweepPacket::new);
        packetHandler.registerPacket(HowlingPacket.class, HowlingPacket::new);
        packetHandler.registerPacket(ProjectileMotionPacket.class, ProjectileMotionPacket::new);
        packetHandler.registerPacket(AddRevengePacket.class, AddRevengePacket::new);
        packetHandler.registerPacket(RemoveRevengePacket.class, RemoveRevengePacket::new);
        packetHandler.registerPacket(LungeEchoPacket.class, LungeEchoPacket::new);

        WorkbenchTile.init(packetHandler);

        proxy.postInit();

        DestabilizationEffect.init();
        SchematicRegistry.instance.registerSchematic(new CleanseSchematic());

        Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, new ResourceLocation(MOD_ID, FeatureEntry.key), FeatureEntry.configuredInstance);
    }

    @SubscribeEvent
    public void serverStarting(ServerAboutToStartEvent event) {
        FeatureEntry.instance.setup(event.getServer());
    }

    @SubscribeEvent
    public void serverStarting(ServerStartingEvent event) {
        ModuleDevCommand.register(event.getServer().getCommands().getDispatcher());
        TGenCommand.register(event.getServer().getCommands().getDispatcher());
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void provideTextures(final TextureStitchEvent.Pre event) {
        // todo 1.15: Move this to ModularItemModel.getTextures?
        if (TextureAtlas.LOCATION_BLOCKS.equals(event.getMap().location())) {
            Minecraft.getInstance().getResourceManager().listResources("textures/items/module", s -> s.endsWith(".png")).stream()
                    .filter(resourceLocation -> MOD_ID.equals(resourceLocation.getNamespace()))
                    // 9 is the length of "textures/" & 4 is the length of ".png"
                    .map(rl -> new ResourceLocation(rl.getNamespace(), rl.getPath().substring(9, rl.getPath().length() - 4)))
                    .forEach(event::addSprite);

            event.addSprite(ForgedContainerRenderer.material.texture());
            event.addSprite(HammerBaseRenderer.material.texture());
            event.addSprite(ScrollRenderer.material.texture());
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void modelRegistryReady(ModelRegistryEvent event) {
        ModelLoaderRegistry.registerLoader(new ResourceLocation(TetraMod.MOD_ID, "modular_loader"), new ModularModelLoader());
    }

    @SubscribeEvent
    public static void onGatherData(final GatherDataEvent event) {
        DataGenerator dataGenerator = event.getGenerator();
        if(event.includeServer()) {
//            dataGenerator.addProvider(new BlockstateProvider(dataGenerator, MOD_ID, event.getExistingFileHelper()));
//            dataGenerator.addProvider(new EnchantmentProvider(dataGenerator, event.getExistingFileHelper()));
            dataGenerator.addProvider(new ModuleProvider(dataGenerator, event.getExistingFileHelper()));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onBiomeLoading(BiomeLoadingEvent event) {
        GeodeBlock.registerFeature(event.getGeneration());

        if (ConfigHandler.generateFeatures.get()) {
            FeatureEntry.instance.registerFeatures(event);
        }
    }

    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void registerFeatures(final RegistryEvent.Register<Feature<?>> event) {
            event.getRegistry().register(FeatureEntry.instance);
        }

        @SubscribeEvent
        public static void registerModifierSerializers(final RegistryEvent.Register<GlobalLootModifierSerializer<?>> event) {
            event.getRegistry().register(new ReplaceTableModifier.Serializer().setRegistryName(new ResourceLocation(MOD_ID,"replace_table")));
        }

        @SubscribeEvent
        public static void registerRecipeSerializers(final RegistryEvent.Register<RecipeSerializer<?>> event) {
            CraftingHelper.register(new ResourceLocation(MOD_ID, "scroll"), ScrollIngredient.Serializer.INSTANCE);
        }

        @SubscribeEvent
        public static void registerEffects(final RegistryEvent.Register<MobEffect> event) {
            event.getRegistry().register(new BleedingPotionEffect());
            event.getRegistry().register(new EarthboundPotionEffect());
            event.getRegistry().register(new StunPotionEffect());
            event.getRegistry().register(new HowlingPotionEffect());
            event.getRegistry().register(new SeveredPotionEffect());
            event.getRegistry().register(new PuncturedPotionEffect());
            event.getRegistry().register(new PriedPotionEffect());
            event.getRegistry().register(new ExhaustedPotionEffect());
            event.getRegistry().register(new SteeledPotionEffect());
            event.getRegistry().register(new SmallStrengthPotionEffect());
            event.getRegistry().register(new UnwaveringPotionEffect());
            event.getRegistry().register(new SmallHealthPotionEffect());
            event.getRegistry().register(new SmallAbsorbPotionEffect());
            event.getRegistry().register(new SuspendPotionEffect());
//            event.getRegistry().register(new MiningSpeedPotionEffect());
        }

        @SubscribeEvent
        public static void registerContainerTypes(final RegistryEvent.Register<MenuType<?>> event) {
            // toolbelt
            MenuType toolbeltContainerType = IForgeContainerType.create(((windowId, inv, data) -> {
                return ToolbeltContainer.create(windowId, inv);
            })).setRegistryName(MOD_ID, ModularToolbeltItem.unlocalizedName);
            event.getRegistry().register(toolbeltContainerType);

            // workbench
            MenuType workbenchContainerType = IForgeContainerType.create(((windowId, inv, data) -> {
                BlockPos pos = data.readBlockPos();
                return WorkbenchContainer.create(windowId, pos, inv);
            })).setRegistryName(MOD_ID, WorkbenchTile.unlocalizedName);
            event.getRegistry().register(workbenchContainerType);

            // forged container
            MenuType forgedContainerContainerType = IForgeContainerType.create(((windowId, inv, data) -> {
                BlockPos pos = data.readBlockPos();
                return ForgedContainerContainer.create(windowId, pos, inv);
            })).setRegistryName(MOD_ID, ForgedContainerBlock.unlocalizedName);
            event.getRegistry().register(forgedContainerContainerType);
        }

        @SubscribeEvent
        public static void registerBlocks(final RegistryEvent.Register<Block> event) {
            event.getRegistry().registerAll(blocks);
        }

        @SubscribeEvent
        public static void registerItems(final RegistryEvent.Register<Item> event) {
            event.getRegistry().registerAll(items);

            Arrays.stream(blocks)
                    .filter(block -> block instanceof ITetraBlock)
                    .map(block -> (ITetraBlock) block)
                    .filter(ITetraBlock::hasItem)
                    .forEach(block -> block.registerItem(event.getRegistry()));
        }

        @SubscribeEvent
        public static void registerTileEntities(final RegistryEvent.Register<BlockEntityType<?>> event) {
            event.getRegistry().register(BlockEntityType.Builder.of(WorkbenchTile::new,
                    BasicWorkbenchBlock.instance, ForgedWorkbenchBlock.instance)
                    .build(null)
                    .setRegistryName(MOD_ID, WorkbenchTile.unlocalizedName));

            event.getRegistry().register(BlockEntityType.Builder.of(HammerBaseTile::new, HammerBaseBlock.instance)
                    .build(null)
                    .setRegistryName(MOD_ID, HammerBaseBlock.unlocalizedName));

            event.getRegistry().register(BlockEntityType.Builder.of(HammerHeadTile::new, HammerHeadBlock.instance)
                    .build(null)
                    .setRegistryName(MOD_ID, HammerHeadBlock.unlocalizedName));

            event.getRegistry().register(BlockEntityType.Builder.of(TransferUnitTile::new, TransferUnitBlock.instance)
                    .build(null)
                    .setRegistryName(MOD_ID, TransferUnitBlock.unlocalizedName));

            event.getRegistry().register(BlockEntityType.Builder.of(CoreExtractorBaseTile::new, CoreExtractorBaseBlock.instance)
                    .build(null)
                    .setRegistryName(MOD_ID, CoreExtractorBaseBlock.unlocalizedName));

            event.getRegistry().register(BlockEntityType.Builder.of(CoreExtractorPistonTile::new, CoreExtractorPistonBlock.instance)
                    .build(null)
                    .setRegistryName(MOD_ID, CoreExtractorPistonBlock.unlocalizedName));

            event.getRegistry().register(BlockEntityType.Builder.of(ForgedContainerTile::new, ForgedContainerBlock.instance)
                    .build(null)
                    .setRegistryName(MOD_ID, ForgedContainerBlock.unlocalizedName));

            event.getRegistry().register(BlockEntityType.Builder.of(ChthonicExtractorTile::new, ChthonicExtractorBlock.instance)
                    .build(null)
                    .setRegistryName(MOD_ID, ChthonicExtractorBlock.unlocalizedName));

            event.getRegistry().register(BlockEntityType.Builder.of(FracturedBedrockTile::new, FracturedBedrockBlock.instance)
                    .build(null)
                    .setRegistryName(MOD_ID, FracturedBedrockBlock.unlocalizedName));

            event.getRegistry().register(BlockEntityType.Builder.of(RackTile::new, RackBlock.instance)
                    .build(null)
                    .setRegistryName(MOD_ID, RackBlock.unlocalizedName));

            event.getRegistry().register(BlockEntityType.Builder.of(ScrollTile::new,
                    OpenScrollBlock.instance, WallScrollBlock.instance, RolledScrollBlock.instance)
                    .build(null)
                    .setRegistryName(MOD_ID, ScrollTile.unlocalizedName));
        }

        @SubscribeEvent
        public static void registerEntities(final RegistryEvent.Register<EntityType<?>> event) {
            event.getRegistry().registerAll(
                    EntityType.Builder.<ThrownModularItemEntity>of(ThrownModularItemEntity::new, MobCategory.MISC)
                            .setCustomClientFactory(ThrownModularItemEntity::new)
                            .sized(0.5F, 0.5F)
                            .build(ThrownModularItemEntity.unlocalizedName)
                            .setRegistryName(MOD_ID, ThrownModularItemEntity.unlocalizedName)
            );

            event.getRegistry().registerAll(
                    EntityType.Builder.<ExtractorProjectileEntity>of(ExtractorProjectileEntity::new, MobCategory.MISC)
                            .setCustomClientFactory(ExtractorProjectileEntity::new)
                            .sized(0.5F, 0.5F)
                            .build(ExtractorProjectileEntity.unlocalizedName)
                            .setRegistryName(MOD_ID, ExtractorProjectileEntity.unlocalizedName)
            );
        }
    }
}
