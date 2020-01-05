package se.mickelus.tetra;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.potion.Effect;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.tetra.advancements.*;
import se.mickelus.tetra.blocks.ITetraBlock;
import se.mickelus.tetra.blocks.forged.*;
import se.mickelus.tetra.blocks.forged.extractor.*;
import se.mickelus.tetra.blocks.forged.transfer.TransferUnitBlock;
import se.mickelus.tetra.blocks.forged.transfer.TransferUnitTile;
import se.mickelus.tetra.blocks.geode.*;
import se.mickelus.tetra.blocks.forged.hammer.HammerBaseBlock;
import se.mickelus.tetra.blocks.forged.hammer.HammerBaseTile;
import se.mickelus.tetra.blocks.forged.hammer.HammerHeadBlock;
import se.mickelus.tetra.blocks.forged.hammer.HammerHeadTile;
import se.mickelus.tetra.blocks.workbench.WorkbenchBlock;
import se.mickelus.tetra.blocks.workbench.WorkbenchContainer;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;
import se.mickelus.tetra.client.model.ModularModelLoader;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.data.UpdateDataPacket;
import se.mickelus.tetra.generation.FeatureEntry;
import se.mickelus.tetra.generation.TGenCommand;
import se.mickelus.tetra.items.ITetraItem;
import se.mickelus.tetra.items.ItemPredicateModular;
import se.mickelus.tetra.items.TetraItemGroup;
import se.mickelus.tetra.items.cell.ItemCellMagmatic;
import se.mickelus.tetra.items.duplex_tool.ItemDuplexToolModular;
import se.mickelus.tetra.items.forged.*;
import se.mickelus.tetra.items.journal.ItemJournal;
import se.mickelus.tetra.items.sword.ItemSwordModular;
import se.mickelus.tetra.items.toolbelt.ItemToolbeltModular;
import se.mickelus.tetra.items.toolbelt.ToolbeltContainer;
import se.mickelus.tetra.loot.FortuneBonusCondition;
import se.mickelus.tetra.module.ItemEffectHandler;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.improvement.DestabilizationEffect;
import se.mickelus.tetra.module.improvement.HonePacket;
import se.mickelus.tetra.module.improvement.SettlePacket;
import se.mickelus.tetra.module.schema.CleanseSchema;
import se.mickelus.tetra.network.GuiHandlerRegistry;
import se.mickelus.tetra.network.PacketHandler;
import se.mickelus.tetra.proxy.ClientProxy;
import se.mickelus.tetra.proxy.IProxy;
import se.mickelus.tetra.proxy.ServerProxy;

import java.util.Arrays;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
@Mod(TetraMod.MOD_ID)
public class TetraMod {
    private static final Logger logger = LogManager.getLogger();

    public static final String MOD_ID = "tetra";

    public static IProxy proxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    public static TetraMod instance;

    private static Item[] items;
    private static Block[] blocks;

    public TetraMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new ItemEffectHandler());
        MinecraftForge.EVENT_BUS.register(new DataManager());
        MinecraftForge.EVENT_BUS.register(TetraMod.proxy);
        MinecraftForge.EVENT_BUS.register(BlockLookTrigger.instance);

        ConfigHandler.setup();

        ItemPredicate.register(new ResourceLocation("tetra:modular_item"), ItemPredicateModular::new);

        LootConditionManager.registerCondition(new FortuneBonusCondition.Serializer());

        new ItemUpgradeRegistry();

        new TetraItemGroup();

        new GuiHandlerRegistry();

        CriteriaTriggers.register(BlockLookTrigger.instance);
        CriteriaTriggers.register(BlockUseCriterion.trigger);
        CriteriaTriggers.register(BlockInteractionCriterion.trigger);
        CriteriaTriggers.register(ModuleCraftCriterion.trigger);
        CriteriaTriggers.register(ImprovementCraftCriterion.trigger);

        blocks = new Block[] {
                new WorkbenchBlock(),
                new GeodeBlock(),
        };

        if (ConfigHandler.generateFeatures.get()) {
            blocks = ArrayUtils.addAll(blocks,
                    new HammerHeadBlock(),
                    new HammerBaseBlock(),
                    new BlockForgedWall(),
                    new BlockForgedPillar(),
                    new BlockForgedPlatform(),
                    new BlockForgedPlatformSlab(),
                    new BlockForgedVent(),
//                    new BlockForgedContainer(),
//                    new BlockForgedCrate(),
                    new TransferUnitBlock(),
                    new CoreExtractorBaseBlock(),
                    new CoreExtractorPistonBlock(),
                    new CoreExtractorPipeBlock(),
                    new SeepingBedrockBlock()
            );
        }

        items = new Item[] {
                new ItemSwordModular(),
                new GeodeItem(),
                new PristineLapisItem(),
                new PristineEmeraldItem(),
                new PristineDiamondItem(),
                new ItemToolbeltModular(),
                new ItemDuplexToolModular(),
                new ItemCellMagmatic(),
                new ItemBolt(),
                new ItemBeam(),
                new ItemMesh(),
                new ItemQuickLatch(),
                new ItemMetalScrap(),
                new ItemVentPlate(),
                new ItemJournal()
        };

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


        PacketHandler packetHandler = new PacketHandler();

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

        proxy.postInit();

        DestabilizationEffect.init();
        new CleanseSchema();
    }

    @SubscribeEvent
    public void serverStarting(FMLServerAboutToStartEvent event) {
        if (ConfigHandler.generateFeatures.get()) {
            FeatureEntry.instance.setup(event.getServer());
        }
    }

    @SubscribeEvent
    public void serverStarting(FMLServerStartingEvent event) {
        ModuleDevCommand.register(event.getCommandDispatcher());
        TGenCommand.register(event.getCommandDispatcher());
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void loadModels(final ModelBakeEvent event) {
        ModularModelLoader.loadModels(event);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void provideTextures(final TextureStitchEvent.Pre event) {
        if ("textures".equals(event.getMap().getBasePath())) {
            Minecraft.getInstance().getResourceManager().getAllResourceLocations("textures/items/module", s -> s.endsWith(".png")).stream()
                    .filter(resourceLocation -> MOD_ID.equals(resourceLocation.getNamespace()))
                    // 9 is the length of "textures/" & 4 is the length of ".png"
                    .map(rl -> new ResourceLocation(rl.getNamespace(), rl.getPath().substring(9, rl.getPath().length() - 4)))
                    .forEach(event::addSprite);
        }
    }

    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void registerFeatures(final RegistryEvent.Register<Feature<?>> event) {
            if (ConfigHandler.generateFeatures.get()) {
                event.getRegistry().register(new FeatureEntry());
            }
        }

        @SubscribeEvent
        public static void registerEffects(final RegistryEvent.Register<Effect> event) {
            event.getRegistry().register(new BleedingEffect());
            event.getRegistry().register(new EarthboundEffect());
        }

        @SubscribeEvent
        public static void registerContainerTypes(final RegistryEvent.Register<ContainerType<?>> event) {
//        event.getRegistry().register(IForgeContainerType.create(((windowId, inv, data) -> {
//            BlockPos pos = data.readBlockPos();
//            TileEntityForgedContainer te = (TileEntityForgedContainer) Minecraft.getInstance().world.getTileEntity(pos);
//            return new ForgedContainerContainer(windowId, te, inv, Minecraft.getInstance().player);
//        }))
//                .setRegistryName(MOD_ID, BlockForgedContainer.unlocalizedName));

            // toolbelt
            ContainerType toolbeltContainerType = IForgeContainerType.create(((windowId, inv, data) -> {
                return ToolbeltContainer.create(windowId, inv);
            })).setRegistryName(MOD_ID, ItemToolbeltModular.unlocalizedName);
            event.getRegistry().register(toolbeltContainerType);

            // workbench
            ContainerType workbenchContainerType = IForgeContainerType.create(((windowId, inv, data) -> {
                BlockPos pos = data.readBlockPos();
                return WorkbenchContainer.create(windowId, pos, inv);
            })).setRegistryName(MOD_ID, WorkbenchBlock.unlocalizedName);
            event.getRegistry().register(workbenchContainerType);
        }

        @SubscribeEvent
        public static void registerBlocks(final RegistryEvent.Register<Block> event) {
            event.getRegistry().registerAll(blocks);
        }

        @SubscribeEvent
        public static void registerItems(final RegistryEvent.Register<Item> event) {
            event.getRegistry().registerAll(items);

            // todo 1.14: this is supposedly not needed, item rendering works?
//        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
//            Arrays.stream(items)
//                    .forEach(item -> {
//                        ModelLoader.setCustomModelResourceLocation(item, 0,
//                                new ModelResourceLocation(item.getRegistryName(), "inventory"));
//                    });
//        }

            Arrays.stream(blocks)
                    .filter(block -> block instanceof ITetraBlock)
                    .map(block -> (ITetraBlock) block)
                    .filter(ITetraBlock::hasItem)
                    .forEach(block -> block.registerItem(event.getRegistry()));
        }

        @SubscribeEvent
        public static void registerTileEntities(final RegistryEvent.Register<TileEntityType<?>> event) {
            // todo 1.14: workbench TE registry, do we really pass null here? (from mcjty tutorial)
            event.getRegistry().register(TileEntityType.Builder.create(WorkbenchTile::new, WorkbenchBlock.instance)
                    .build(null)
                    .setRegistryName(MOD_ID, WorkbenchBlock.unlocalizedName));

            event.getRegistry().register(TileEntityType.Builder.create(HammerBaseTile::new, HammerBaseBlock.instance)
                    .build(null)
                    .setRegistryName(MOD_ID, HammerBaseBlock.unlocalizedName));

            event.getRegistry().register(TileEntityType.Builder.create(HammerHeadTile::new, HammerHeadBlock.instance)
                    .build(null)
                    .setRegistryName(MOD_ID, HammerHeadBlock.unlocalizedName));

            event.getRegistry().register(TileEntityType.Builder.create(TransferUnitTile::new, TransferUnitBlock.instance)
                    .build(null)
                    .setRegistryName(MOD_ID, TransferUnitBlock.unlocalizedName));

            event.getRegistry().register(TileEntityType.Builder.create(CoreExtractorBaseTile::new, CoreExtractorBaseBlock.instance)
                    .build(null)
                    .setRegistryName(MOD_ID, CoreExtractorBaseBlock.unlocalizedName));

            event.getRegistry().register(TileEntityType.Builder.create(CoreExtractorPistonTile::new, CoreExtractorPistonBlock.instance)
                    .build(null)
                    .setRegistryName(MOD_ID, CoreExtractorPistonBlock.unlocalizedName));
        }
    }
}
