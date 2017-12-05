package se.mickelus.tetra;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import se.mickelus.tetra.blocks.geode.BlockGeode;
import se.mickelus.tetra.blocks.geode.ItemGeode;
import se.mickelus.tetra.blocks.workbench.BlockWorkbench;
import se.mickelus.tetra.blocks.ITetraBlock;
import se.mickelus.tetra.items.ITetraItem;
import se.mickelus.tetra.items.TetraCreativeTabs;
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

    private ITetraItem[] items;
    private ITetraBlock[] blocks;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ConfigHandler.init(event.getSuggestedConfigurationFile());

        new DataHandler(event.getSourceFile());

        new ItemUpgradeRegistry();

        new TetraCreativeTabs();

        new GuiHandlerRegistry();

        items = new ITetraItem[] {
            new ItemSwordModular(),
            new ItemRocketBoots(),
            new ItemToolbelt(),
            new JournalItem(),
            new ItemHammerModular(),
            new ItemGeode()
        };

        blocks = new ITetraBlock[] {
            new BlockWorkbench(),
            new BlockGeode()
        };


        proxy.preInit(event, items, blocks);
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);

        NetworkRegistry.INSTANCE.registerGuiHandler(instance, GuiHandlerRegistry.instance);

        PacketPipeline packetPipeline = new PacketPipeline();
        packetPipeline.initialize();

        Arrays.stream(items).forEach(item -> item.init(packetPipeline));
        Arrays.stream(blocks).forEach(block -> block.init(packetPipeline));
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
        PacketPipeline.instance.postInitialize();
    }
}
