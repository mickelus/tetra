package se.mickelus.tetra;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;
import net.minecraftforge.advancements.critereon.ItemPredicates;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.ArrayUtils;
import se.mickelus.tetra.advancements.*;
import se.mickelus.tetra.blocks.ITetraBlock;
import se.mickelus.tetra.blocks.forged.*;
import se.mickelus.tetra.blocks.forged.container.BlockForgedContainer;
import se.mickelus.tetra.blocks.forged.extractor.BlockCoreExtractorBase;
import se.mickelus.tetra.blocks.forged.extractor.BlockCoreExtractorPipe;
import se.mickelus.tetra.blocks.forged.extractor.BlockCoreExtractorPiston;
import se.mickelus.tetra.blocks.forged.extractor.BlockSeepingBedrock;
import se.mickelus.tetra.blocks.forged.transfer.BlockTransferUnit;
import se.mickelus.tetra.blocks.geode.*;
import se.mickelus.tetra.blocks.hammer.BlockHammerBase;
import se.mickelus.tetra.blocks.hammer.BlockHammerHead;
import se.mickelus.tetra.blocks.workbench.BlockWorkbench;
import se.mickelus.tetra.data.DataHandler;
import se.mickelus.tetra.generation.TGenCommand;
import se.mickelus.tetra.generation.WorldGenFeatures;
import se.mickelus.tetra.items.ITetraItem;
import se.mickelus.tetra.items.ItemPredicateModular;
import se.mickelus.tetra.items.TetraCreativeTabs;
import se.mickelus.tetra.items.cell.ItemCellMagmatic;
import se.mickelus.tetra.items.duplex_tool.ItemDuplexToolModular;
import se.mickelus.tetra.items.forged.*;
import se.mickelus.tetra.items.journal.ItemJournal;
import se.mickelus.tetra.items.sword.ItemSwordModular;
import se.mickelus.tetra.items.toolbelt.ItemToolbeltModular;
import se.mickelus.tetra.loot.FortuneBonusCondition;
import se.mickelus.tetra.loot.FortuneBonusFunction;
import se.mickelus.tetra.loot.SetMetadataFunction;
import se.mickelus.tetra.module.ItemEffectHandler;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.improvement.DestabilizationEffect;
import se.mickelus.tetra.module.improvement.HonePacket;
import se.mickelus.tetra.module.improvement.SettlePacket;
import se.mickelus.tetra.module.schema.CleanseSchema;
import se.mickelus.tetra.network.GuiHandlerRegistry;
import se.mickelus.tetra.network.PacketHandler;
import se.mickelus.tetra.proxy.IProxy;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

@Mod(useMetadata = true, modid = TetraMod.MOD_ID, version = "#VERSION")
public class TetraMod {
    public static final String MOD_ID = "tetra";

    @SidedProxy(clientSide = "se.mickelus.tetra.proxy.ClientProxy", serverSide = "se.mickelus.tetra.proxy.ServerProxy")
    public static IProxy proxy;

    @Mod.Instance(TetraMod.MOD_ID)
    public static TetraMod instance;

    private Item[] items;
    private Block[] blocks;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ItemPredicates.register(new ResourceLocation("tetra:modular_item"), ItemPredicateModular::new);

        LootConditionManager.registerCondition(new FortuneBonusCondition.Serializer());
        LootFunctionManager.registerFunction(new FortuneBonusFunction.Serializer());
        LootFunctionManager.registerFunction(new SetMetadataFunction.Serializer());

        new DataHandler(event.getSourceFile());

        new ItemUpgradeRegistry();

        new TetraCreativeTabs();

        new GuiHandlerRegistry();

        CriteriaTriggers.register(BlockLookTrigger.instance);
        CriteriaTriggers.register(BlockUseCriterion.trigger);
        CriteriaTriggers.register(BlockInteractionCriterion.trigger);
        CriteriaTriggers.register(ModuleCraftCriterion.trigger);
        CriteriaTriggers.register(ImprovementCraftCriterion.trigger);

        MinecraftForge.EVENT_BUS.register(new ItemEffectHandler());
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(TetraMod.proxy);
        MinecraftForge.EVENT_BUS.register(BlockLookTrigger.instance);

        blocks = new Block[] {
                new BlockWorkbench(),
                new BlockGeode(),
        };

        if (ConfigHandler.generateFeatures) {
            blocks = ArrayUtils.addAll(blocks,
                    new BlockHammerHead(),
                    new BlockHammerBase(),
                    new BlockForgedWall(),
                    new BlockForgedPillar(),
                    new BlockForgedPlatform(),
                    new BlockForgedPlatformSlab(),
                    new BlockForgedVent(),
                    new BlockForgedContainer(),
                    new BlockForgedCrate(),
                    new BlockTransferUnit(),
                    new BlockCoreExtractorBase(),
                    new BlockCoreExtractorPiston(),
                    new BlockCoreExtractorPipe(),
                    new BlockSeepingBedrock()
            );
        }

        items = new Item[] {
                new ItemSwordModular(),
                new ItemGeode(),
                new ItemPristineLapis(),
                new ItemPristineEmerald(),
                new ItemPristineDiamond(),
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

        ForgeRegistries.POTIONS.registerAll(new PotionBleeding());
        ForgeRegistries.POTIONS.registerAll(new PotionEarthbound());

        proxy.preInit(event,
                Arrays.stream(items)
                        .filter(item -> item instanceof ITetraItem)
                        .map(item -> (ITetraItem) item).toArray(ITetraItem[]::new),
                Arrays.stream(blocks)
                        .filter(block -> block instanceof ITetraBlock)
                        .map(block -> (ITetraBlock) block).toArray(ITetraBlock[]::new));
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);

        if (ConfigHandler.generateFeatures) {
            WorldGenFeatures worldGenFeatures = new WorldGenFeatures();
            GameRegistry.registerWorldGenerator(worldGenFeatures, 11);
        }

        NetworkRegistry.INSTANCE.registerGuiHandler(instance, GuiHandlerRegistry.instance);

        PacketHandler packetHandler = new PacketHandler();

        Arrays.stream(items)
                .filter(item -> item instanceof ITetraItem)
                .map(item -> (ITetraItem) item)
                .forEach(item -> item.init(packetHandler));
        Arrays.stream(blocks)
                .filter(block -> block instanceof ITetraBlock)
                .map(block -> (ITetraBlock) block)
                .forEach(block -> block.init(packetHandler));

        packetHandler.registerPacket(HonePacket.class, Side.CLIENT);
        packetHandler.registerPacket(SettlePacket.class, Side.CLIENT);

        DestabilizationEffect.LoadEffects();
        new CleanseSchema();
    }

    @SubscribeEvent
    public void lootTableLoad(LootTableLoadEvent event) {
        if (TetraMod.MOD_ID.equals(event.getName().getResourceDomain())) {
            LootTable lootTable = event.getTable();
            LootPool[] extendedPools = DataHandler.instance.getExtendedLootPools(event.getName());
            Optional.ofNullable(extendedPools)
                    .map(Arrays::stream)
                    .orElseGet(Stream::empty)
                    .forEach(lootTable::addPool);
        }
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new TGenCommand());
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(blocks);
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(items);

        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            Arrays.stream(items)
                    .forEach(item -> {
                        ModelLoader.setCustomModelResourceLocation(item, 0,
                                new ModelResourceLocation(item.getRegistryName(), "inventory"));
                    });
        }

        Arrays.stream(blocks)
                .filter(block -> block instanceof ITetraBlock)
                .map(block -> (ITetraBlock) block)
                .filter(ITetraBlock::hasItem)
                .forEach(block -> block.registerItem(event.getRegistry()));
    }
}
