package se.mickelus.tetra;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.mutil.network.PacketHandler;
import se.mickelus.tetra.aspect.TetraEnchantmentHelper;
import se.mickelus.tetra.blocks.geode.GeodeBlock;
import se.mickelus.tetra.blocks.scroll.ScrollRenderer;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;
import se.mickelus.tetra.compat.curios.CuriosCompat;
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
import se.mickelus.tetra.effect.ItemEffectHandler;
import se.mickelus.tetra.effect.LungeEchoPacket;
import se.mickelus.tetra.effect.TruesweepPacket;
import se.mickelus.tetra.effect.howling.HowlingPacket;
import se.mickelus.tetra.effect.revenge.AddRevengePacket;
import se.mickelus.tetra.effect.revenge.RemoveRevengePacket;
import se.mickelus.tetra.items.TetraItemGroup;
import se.mickelus.tetra.items.forged.VibrationDebuffer;
import se.mickelus.tetra.items.modular.ChargedAbilityPacket;
import se.mickelus.tetra.items.modular.SecondaryAbilityPacket;
import se.mickelus.tetra.items.modular.impl.bow.ProjectileMotionPacket;
import se.mickelus.tetra.items.modular.impl.toolbelt.ToolbeltModule;
import se.mickelus.tetra.module.*;
import se.mickelus.tetra.module.improvement.DestabilizationEffect;
import se.mickelus.tetra.module.improvement.HonePacket;
import se.mickelus.tetra.module.improvement.SettlePacket;
import se.mickelus.tetra.module.schematic.BookEnchantSchematic;
import se.mickelus.tetra.module.schematic.CleanseSchematic;
import se.mickelus.tetra.properties.TetraAttributes;
import se.mickelus.tetra.proxy.ClientProxy;
import se.mickelus.tetra.proxy.IProxy;
import se.mickelus.tetra.proxy.ServerProxy;
import se.mickelus.tetra.trades.TradeHandler;
import se.mickelus.tetra.util.TierHelper;
import se.mickelus.tetra.util.ToolActionHelper;

import javax.annotation.ParametersAreNonnullByDefault;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
@Mod(TetraMod.MOD_ID)

@ParametersAreNonnullByDefault
public class TetraMod {
    public static final String MOD_ID = "tetra";
    private static final Logger logger = LogManager.getLogger();
    public static IProxy proxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    public static TetraMod instance;
    public static PacketHandler packetHandler;

    public TetraMod() {
        TetraRegistries.init(FMLJavaModLoadingContext.get().getModEventBus());

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

        ToolActionHelper.init();
        TierHelper.init();

        ConfigHandler.setup();

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
        ItemUpgradeRegistry.instance.registerReplacementHook(TetraEnchantmentHelper::transferReplacementEnchantments);

        ModuleRegistry moduleRegistry = new ModuleRegistry();
        moduleRegistry.registerModuleType(new ResourceLocation(MOD_ID, "basic_module"), BasicModule::new);
        moduleRegistry.registerModuleType(new ResourceLocation(MOD_ID, "multi_module"), MultiSlotModule::new);
        moduleRegistry.registerModuleType(new ResourceLocation(MOD_ID, "basic_major_module"), BasicMajorModule::new);
        moduleRegistry.registerModuleType(new ResourceLocation(MOD_ID, "multi_major_module"), MultiSlotMajorModule::new);
        moduleRegistry.registerModuleType(new ResourceLocation(MOD_ID, "toolbelt_module"), ToolbeltModule::new);

        new TetraItemGroup();

        packetHandler = new PacketHandler(MOD_ID, "main", "1");
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void provideTextures(final TextureStitchEvent.Pre event) {
        // todo 1.15: Move this to ModularItemModel.getTextures?
        if (TextureAtlas.LOCATION_BLOCKS.equals(event.getAtlas().location())) {
            Minecraft.getInstance().getResourceManager().listResources("textures/items/module", s -> s.endsWith(".png")).stream()
                    .filter(resourceLocation -> MOD_ID.equals(resourceLocation.getNamespace()))
                    // 9 is the length of "textures/" & 4 is the length of ".png"
                    .map(rl -> new ResourceLocation(rl.getNamespace(), rl.getPath().substring(9, rl.getPath().length() - 4)))
                    .forEach(event::addSprite);

//            event.addSprite(ForgedContainerRenderer.material.texture());
//            event.addSprite(HammerBaseRenderer.material.texture());
            event.addSprite(ScrollRenderer.material.texture());
        }
    }

//    @SubscribeEvent
//    public static void onGatherData(final GatherDataEvent event) {
//        DataGenerator dataGenerator = event.getGenerator();
//        if (event.includeServer()) {
//            dataGenerator.addProvider(new BlockstateProvider(dataGenerator, MOD_ID, event.getExistingFileHelper()));
//        }
//    }

    public void setup(FMLCommonSetupEvent event) {
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

        DestabilizationEffect.init();
        SchematicRegistry.instance.registerSchematic(new CleanseSchematic());
    }

    @SubscribeEvent
    public void serverStarting(ServerStartingEvent event) {
        ModuleDevCommand.register(event.getServer().getCommands().getDispatcher());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onBiomeLoading(BiomeLoadingEvent event) {
        GeodeBlock.registerFeature(event);
    }
}
