package se.mickelus.tetra;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.blocks.geode.BlockGeode;
import se.mickelus.tetra.blocks.geode.ItemGeode;
import se.mickelus.tetra.blocks.workbench.BlockWorkbench;
import se.mickelus.tetra.blocks.ITetraBlock;
import se.mickelus.tetra.items.ITetraItem;
import se.mickelus.tetra.items.TetraCreativeTabs;
import se.mickelus.tetra.items.TetraItem;
import se.mickelus.tetra.items.hammer.ItemHammerModular;
import se.mickelus.tetra.items.rocketBoots.ItemRocketBoots;
import se.mickelus.tetra.items.sword.ItemSwordModular;
import se.mickelus.tetra.items.JournalItem;
import se.mickelus.tetra.items.toolbelt.ItemToolbelt;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.network.GuiHandlerRegistry;
import se.mickelus.tetra.network.PacketPipeline;
import se.mickelus.tetra.proxy.IProxy;

import java.util.Arrays;

@Mod(useMetadata = true, modid = TetraMod.MOD_ID)
public class TetraMod {
    public static final String MOD_ID = "tetra";

    @SidedProxy(clientSide = "se.mickelus.tetra.proxy.ClientProxy", serverSide = "se.mickelus.tetra.proxy.ServerProxy")
    public static IProxy proxy;

    @Mod.Instance(TetraMod.MOD_ID)
    public static TetraMod instance;

    private Item[] items;
    private TetraBlock[] blocks;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ConfigHandler.init(event.getSuggestedConfigurationFile());

        new DataHandler(event.getSourceFile());

        new ItemUpgradeRegistry();

        new TetraCreativeTabs();

        new GuiHandlerRegistry();

        blocks = new TetraBlock[] {
                new BlockWorkbench(),
                new BlockGeode()
        };

        items = new Item[] {
//            new ItemSwordModular(),
            new ItemRocketBoots(),
            new ItemGeode(),
            new ItemToolbelt(),
//            new JournalItem(),
//            new ItemHammerModular(),
//            new ItemBlock(BlockGeode.instance)
        };

//        Arrays.stream(items)
//                .forEach(MinecraftForge.EVENT_BUS::register);

        proxy.preInit(event,
                Arrays.stream(items)
                    .filter(item -> item instanceof ITetraItem)
                    .map(item -> (ITetraItem) item).toArray(ITetraItem[]::new),
                blocks);

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(blocks);
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(items);
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);

        NetworkRegistry.INSTANCE.registerGuiHandler(instance, GuiHandlerRegistry.instance);

        PacketPipeline packetPipeline = new PacketPipeline();
        packetPipeline.initialize();

        Arrays.stream(items)
                .filter(item -> item instanceof ITetraItem)
                .map(item -> (ITetraItem) item)
                .forEach(item -> item.init(packetPipeline));
        Arrays.stream(blocks).forEach(block -> block.init(packetPipeline));
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
        PacketPipeline.instance.postInitialize();
    }
}
