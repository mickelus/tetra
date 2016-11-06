package se.mickelus.tetra;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import se.mickelus.tetra.items.rocketBoots.ItemRocketBoots;
import se.mickelus.tetra.items.rocketBoots.JumpHandlerRocketBoots;
import se.mickelus.tetra.items.rocketBoots.UpdateBoostPacket;
import se.mickelus.tetra.items.toolbelt.EquipToolbeltItemPacket;
import se.mickelus.tetra.items.toolbelt.GuiHandlerToolbelt;
import se.mickelus.tetra.items.JournalItem;
import se.mickelus.tetra.items.toolbelt.ItemToolbelt;
import se.mickelus.tetra.items.toolbelt.TickHandlerToolbelt;
import se.mickelus.tetra.network.PacketPipeline;
import se.mickelus.tetra.proxy.IProxy;

@Mod(useMetadata = true, modid = TetraMod.MOD_ID)
public class TetraMod {
    public static final String MOD_ID = "tetra";

    @SidedProxy(clientSide = "se.mickelus.tetra.proxy.ClientProxy", serverSide = "se.mickelus.tetra.proxy.ServerProxy")
    public static IProxy proxy;

    @Mod.Instance(TetraMod.MOD_ID)
    public static TetraMod instance;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ConfigHandler.init(event.getSuggestedConfigurationFile());

        new TetraCreativeTabs();

        new ItemRocketBoots();
        new ItemToolbelt();
        new JournalItem();

        proxy.preInit(event);
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event) {

        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandlerToolbelt());
        proxy.init(event);

        PacketPipeline packetPipeline = new PacketPipeline();

        packetPipeline.initialize();
        packetPipeline.registerPacket(EquipToolbeltItemPacket.class);
        packetPipeline.registerPacket(UpdateBoostPacket.class);

        MinecraftForge.EVENT_BUS.register(new TickHandlerToolbelt());
        MinecraftForge.EVENT_BUS.register(new JumpHandlerRocketBoots(Minecraft.getMinecraft()));
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
        PacketPipeline.instance.postInitialize();
    }
}
