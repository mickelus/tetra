package se.mickelus.tetra;

import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.advancements.critereon.ItemPredicates;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.blocks.forgehammer.BlockPowerHammer;
import se.mickelus.tetra.blocks.geode.BlockGeode;
import se.mickelus.tetra.blocks.geode.ItemGeode;
import se.mickelus.tetra.blocks.workbench.BlockWorkbench;
import se.mickelus.tetra.items.ITetraItem;
import se.mickelus.tetra.items.ItemModularPredicate;
import se.mickelus.tetra.items.TetraCreativeTabs;
import se.mickelus.tetra.items.duplex_tool.ItemDuplexToolModular;
import se.mickelus.tetra.items.sword.ItemSwordModular;
import se.mickelus.tetra.items.toolbelt.ItemToolbeltModular;
import se.mickelus.tetra.module.ItemEffectHandler;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.network.GuiHandlerRegistry;
import se.mickelus.tetra.network.PacketHandler;
import se.mickelus.tetra.proxy.IProxy;

import java.util.Arrays;

@Mod(useMetadata = true, modid = TetraMod.MOD_ID, version = "#VERSION")
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
        ItemPredicates.register(new ResourceLocation("tetra:modular_item"), ItemModularPredicate::new);

        new DataHandler(event.getSourceFile());

        new ItemUpgradeRegistry();

        new TetraCreativeTabs();

        new GuiHandlerRegistry();

        MinecraftForge.EVENT_BUS.register(new ItemEffectHandler());

        blocks = new TetraBlock[] {
                new BlockWorkbench(),
                new BlockGeode(),
                new BlockPowerHammer()
        };

        items = new Item[] {
            new ItemSwordModular(),
            new ItemGeode(),
            new ItemToolbeltModular(),
            new ItemDuplexToolModular()
        };


        ForgeRegistries.ITEMS.registerAll(items);
        ForgeRegistries.BLOCKS.registerAll(blocks);
        ForgeRegistries.POTIONS.registerAll(new PotionBleeding());

        ForgeRegistries.ITEMS.register(new ItemBlock(BlockWorkbench.instance).setRegistryName(BlockWorkbench.instance.getRegistryName()));
        ForgeRegistries.ITEMS.register(new ItemBlock(BlockPowerHammer.instance).setRegistryName(BlockPowerHammer.instance.getRegistryName()));

        proxy.preInit(event,
                Arrays.stream(items)
                    .filter(item -> item instanceof ITetraItem)
                    .map(item -> (ITetraItem) item).toArray(ITetraItem[]::new),
                blocks);
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);

        NetworkRegistry.INSTANCE.registerGuiHandler(instance, GuiHandlerRegistry.instance);

        PacketHandler packetHandler = new PacketHandler();

        Arrays.stream(items)
                .filter(item -> item instanceof ITetraItem)
                .map(item -> (ITetraItem) item)
                .forEach(item -> item.init(packetHandler));
        Arrays.stream(blocks).forEach(block -> block.init(packetHandler));
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }
}
