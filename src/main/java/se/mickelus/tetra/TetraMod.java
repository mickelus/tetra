package se.mickelus.tetra;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.mutil.network.PacketHandler;
import se.mickelus.tetra.aspect.TetraEnchantmentHelper;
import se.mickelus.tetra.blocks.multischematic.MultiblockSchematicScrollPacket;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;
import se.mickelus.tetra.compat.curios.CuriosCompat;
import se.mickelus.tetra.crafting.GrindstoneMergeHandler;
import se.mickelus.tetra.craftingeffect.CraftingEffectRegistry;
import se.mickelus.tetra.craftingeffect.condition.*;
import se.mickelus.tetra.craftingeffect.outcome.*;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.data.UpdateDataPacket;
import se.mickelus.tetra.data.provider.StatBarProvider;
import se.mickelus.tetra.data.provider.TetraBlockStateProvider;
import se.mickelus.tetra.data.provider.TetraLootTableProvider;
import se.mickelus.tetra.data.provider.TetraTagsProvider;
import se.mickelus.tetra.effect.ItemEffectHandler;
import se.mickelus.tetra.effect.TruesweepPacket;
import se.mickelus.tetra.effect.data.condition.*;
import se.mickelus.tetra.effect.data.outcome.*;
import se.mickelus.tetra.effect.data.provider.entity.ContextEntityProvider;
import se.mickelus.tetra.effect.data.provider.entity.EntityProvider;
import se.mickelus.tetra.effect.data.provider.number.*;
import se.mickelus.tetra.effect.data.provider.vector.*;
import se.mickelus.tetra.effect.howling.HowlingPacket;
import se.mickelus.tetra.effect.lunge.LungeEchoPacket;
import se.mickelus.tetra.effect.revenge.AddRevengePacket;
import se.mickelus.tetra.effect.revenge.RemoveRevengePacket;
import se.mickelus.tetra.interactions.SecondaryInteractionPacket;
import se.mickelus.tetra.items.forged.VibrationDebuffer;
import se.mickelus.tetra.items.modular.ChargedAbilityPacket;
import se.mickelus.tetra.items.modular.SecondaryAbilityPacket;
import se.mickelus.tetra.items.modular.impl.bow.ProjectileMotionPacket;
import se.mickelus.tetra.items.modular.impl.toolbelt.ToolbeltModule;
import se.mickelus.tetra.module.*;
import se.mickelus.tetra.module.improvement.HonePacket;
import se.mickelus.tetra.module.improvement.SettlePacket;
import se.mickelus.tetra.module.schematic.BookEnchantSchematic;
import se.mickelus.tetra.module.schematic.CleanseSchematic;
import se.mickelus.tetra.module.schematic.RemoveSchematic;
import se.mickelus.tetra.module.schematic.requirement.*;
import se.mickelus.tetra.properties.TetraAttributes;
import se.mickelus.tetra.trades.TradeHandler;
import se.mickelus.tetra.util.TierHelper;
import se.mickelus.tetra.util.ToolActionHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
@Mod(TetraMod.MOD_ID)

@ParametersAreNonnullByDefault
public class TetraMod {
    public static final String MOD_ID = "tetra";
    private static final Logger logger = LogManager.getLogger();

    public static TetraMod instance;
    public static PacketHandler packetHandler;

    public TetraMod() {
        TetraRegistries.init(FMLJavaModLoadingContext.get().getModEventBus());
        TetraEnchantmentHelper.init();
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientSetup::init);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(CuriosCompat::enqueueIMC);
        TetraAttributes.registry.register(FMLJavaModLoadingContext.get().getModEventBus());

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new ItemEffectHandler());
        MinecraftForge.EVENT_BUS.register(new TradeHandler());
        MinecraftForge.EVENT_BUS.register(new DataManager());
        MinecraftForge.EVENT_BUS.register(new VibrationDebuffer());
        MinecraftForge.EVENT_BUS.register(GrindstoneMergeHandler.class);
        MinecraftForge.EVENT_BUS.register(ServerScheduler.class);
        MinecraftForge.EVENT_BUS.register(ClientScheduler.class);

        ToolActionHelper.init();
        TierHelper.init();

        ConfigHandler.setup();

        new CraftingEffectRegistry();
        CraftingEffectRegistry.registerConditionType("tetra:or", OrCondition.class);
        CraftingEffectRegistry.registerConditionType("tetra:and", AndCondition.class);
        CraftingEffectRegistry.registerConditionType("tetra:not", NotCondition.class);
        CraftingEffectRegistry.registerConditionType("tetra:schematic", SchematicCondition.class);
        CraftingEffectRegistry.registerConditionType("tetra:craft_type", CraftTypeCondition.class);
        CraftingEffectRegistry.registerConditionType("tetra:locked", LockedCondition.class);
        CraftingEffectRegistry.registerConditionType("tetra:material", MaterialCondition.class);
        CraftingEffectRegistry.registerConditionType("tetra:tool", ToolCondition.class);
        CraftingEffectRegistry.registerConditionType("tetra:improvement", ImprovementCondition.class);
        CraftingEffectRegistry.registerConditionType("tetra:module", ModuleCondition.class);
        CraftingEffectRegistry.registerConditionType("tetra:aspect", AspectCondition.class);
        CraftingEffectRegistry.registerConditionType("tetra:slot", SlotCondition.class);

        CraftingEffectRegistry.registerEffectType("tetra:apply_improvements", ApplyImprovementOutcome.class);
        CraftingEffectRegistry.registerEffectType("tetra:apply_enchantments", ApplyEnchantmentOutcome.class);
        CraftingEffectRegistry.registerEffectType("tetra:apply_nbt", ApplyNbtOutcome.class);
        CraftingEffectRegistry.registerEffectType("tetra:apply_list", ApplyListOutcome.class);
        CraftingEffectRegistry.registerEffectType("tetra:remove_improvements", RemoveImprovementOutcome.class);
        CraftingEffectRegistry.registerEffectType("tetra:material_reduction", MaterialReductionOutcome.class);
        CraftingEffectRegistry.registerEffectType("tetra:per_slot", PerSlotOutcome.class);
        CraftingEffectRegistry.registerEffectType("tetra:destabilize", DestabilizeOutcome.class);
        CraftingEffectRegistry.registerEffectType("tetra:spawn_sculk", SpawnSculkOutcome.class);

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

        CraftingRequirementDeserializer.registerSupplier("tetra:and", AndRequirement.class);
        CraftingRequirementDeserializer.registerSupplier("tetra:or", OrRequirement.class);
        CraftingRequirementDeserializer.registerSupplier("tetra:not", NotRequirement.class);
        CraftingRequirementDeserializer.registerSupplier("tetra:never", NeverRequirement.class);
        CraftingRequirementDeserializer.registerSupplier("tetra:feature_flag", FeatureFlagRequirement.class);
        CraftingRequirementDeserializer.registerSupplier("tetra:locked", LockedRequirement.class);
        CraftingRequirementDeserializer.registerSupplier("tetra:improvement", HasImprovementRequirement.class);
        CraftingRequirementDeserializer.registerSupplier("tetra:accepts_improvement", AcceptsImprovementRequirement.class);
        CraftingRequirementDeserializer.registerSupplier("tetra:module", ModuleRequirement.class);
        CraftingRequirementDeserializer.registerSupplier("tetra:aspect", AspectRequirement.class);
        CraftingRequirementDeserializer.registerSupplier("tetra:perk", PerkRequrement.class);
        CraftingRequirementDeserializer.registerSupplier("tetra:slot", SlotRequirement.class);

        ItemEffectCondition.register("tetra:random", RandomItemEffectCondition.class);
        ItemEffectCondition.register("tetra:expression", ExpressionItemEffectCondition::deserialize);
        ItemEffectCondition.register("tetra:and", AndItemEffectCondition.class);
        ItemEffectCondition.register("tetra:or", OrItemEffectCondition.class);
        ItemEffectCondition.register("tetra:not", NotItemEffectCondition.class);
        ItemEffectCondition.register("tetra:block", BlockItemEffectCondition.class);
        ItemEffectCondition.register("tetra:can_harvest", CanHarvestItemEffectCondition.class);
        ItemEffectCondition.register("tetra:entity", EntityItemEffectCondition.class);
        ItemEffectCondition.register("tetra:entities_equals", EntitiesEqualsItemEffectCondition.class);
        ItemEffectCondition.register("tetra:fixed", FixedItemEffectCondition.class);

        ItemEffectOutcome.register("tetra:apply_effect", ApplyEffectItemEffectOutcome.class);
        ItemEffectOutcome.register("tetra:conditioned", ConditionedItemEffectOutcome.class);
        ItemEffectOutcome.register("tetra:multiple", MultipleItemEffectOutcome.class);
        ItemEffectOutcome.register("tetra:function", RunFunctionItemEffectOutcome.class);
        ItemEffectOutcome.register("tetra:command", RunCommandItemEffectOutcome.class);
        ItemEffectOutcome.register("tetra:move_entity", MoveEntityItemEffectOutcome.class);
        ItemEffectOutcome.register("tetra:set_block", SetBlockItemEffectOutcome.class);
        ItemEffectOutcome.register("tetra:find_blocks", FindBlocksItemEffectOutcome.class);
        ItemEffectOutcome.register("tetra:break_block", BreakBlockItemEffectOutcome.class);
        ItemEffectOutcome.register("tetra:damage_entity", DamageEntityItemEffectOutcome.class);
        ItemEffectOutcome.register("tetra:find_entities", FindEntitiesItemEffectOutcome.class);
        ItemEffectOutcome.register("tetra:push_entity", PushEntityItemEffectOutcome.class);
        ItemEffectOutcome.register("tetra:particle", ParticleItemEffectOutcome.class);
        ItemEffectOutcome.register("tetra:sound", SoundItemEffectOutcome.class);
        ItemEffectOutcome.register("tetra:delay", DelayItemEffectOutcome.class);
        ItemEffectOutcome.register("tetra:entity_data", EntityDataItemEffectOutcome.class);
        ItemEffectOutcome.register("tetra:entity_property", EntityPropertyItemEffectOutcome.class);
        ItemEffectOutcome.register("tetra:loop", LoopItemEffectOutcome.class);
        ItemEffectOutcome.register("tetra:imitate", ImitateItemEffectOutcome.class);

        NumberProvider.register("tetra:expression", ExpressionNumberProvider::deserialize);
        NumberProvider.register("tetra:fixed", FixedNumberProvider.class);
        NumberProvider.register("tetra:context", ContextNumberProvider.class);
        NumberProvider.register("tetra:random", RandomNumberProvider.class);
        NumberProvider.register("tetra:sum", SumNumberProvider.class);
        NumberProvider.register("tetra:subtract", SubtractNumberProvider.class);
        NumberProvider.register("tetra:multiply", MultiplyNumberProvider.class);
        NumberProvider.register("tetra:divide", DivideNumberProvider.class);
        NumberProvider.register("tetra:length", LengthNumberProvider.class);
        NumberProvider.register("tetra:effect_level", EffectLevelNumberProvider.class);
        NumberProvider.register("tetra:effect_efficiency", EffectEfficiencyNumberProvider.class);
        NumberProvider.register("tetra:entity_data", EntityDataNumberProvider.class);
        NumberProvider.register("tetra:entity_property", EntityPropertyNumberProvider.class);
        NumberProvider.register("tetra:vector", VectorNumberProvider.class);
        NumberProvider.register("tetra:block_property", BlockPropertyNumberProvider.class);
        NumberProvider.register("tetra:time", TimeNumberProvider.class);

        VectorProvider.register("tetra:entity_position", EntityPositionVectorProvider.class);
        VectorProvider.register("tetra:context", ContextVectorProvider.class);
        VectorProvider.register("tetra:expression", ExpressionVectorProvider.class);
        VectorProvider.register("tetra:normalize", NormalizeVectorProvider.class);
        VectorProvider.register("tetra:number", NumberVectorProvider.class);
        VectorProvider.register("tetra:entity_facing", EntityFacingVectorProvider.class);
        VectorProvider.register("tetra:entity_motion", EntityMotionVectorProvider.class);

        EntityProvider.register("tetra:context", ContextEntityProvider.class);

        packetHandler = new PacketHandler(MOD_ID, "main", "1");
    }

    @SubscribeEvent
    public static void onGatherData(final GatherDataEvent event) {
        DataGenerator dataGenerator = event.getGenerator();
        DataGenerator gen = event.getGenerator();
        PackOutput packOutput = gen.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        if (event.includeServer()) {
            dataGenerator.addProvider(true, new TetraBlockStateProvider(packOutput, MOD_ID, event.getExistingFileHelper()));
            dataGenerator.addProvider(true, new TetraTagsProvider(packOutput, lookupProvider, MOD_ID, event.getExistingFileHelper()));
            dataGenerator.addProvider(true, new TetraLootTableProvider(packOutput));
        }
        if (event.includeClient()) {
            dataGenerator.addProvider(true, new StatBarProvider(packOutput));
        }
    }

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
        packetHandler.registerPacket(MultiblockSchematicScrollPacket.class, MultiblockSchematicScrollPacket::new);
        packetHandler.registerPacket(SecondaryInteractionPacket.class, SecondaryInteractionPacket::new);

        WorkbenchTile.init(packetHandler);

        SchematicRegistry.instance.registerSchematic(new CleanseSchematic());
        SchematicRegistry.instance.registerSchematic(new RemoveSchematic());
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        ModuleDevCommand.register(event.getDispatcher(), event.getBuildContext());
        TetraCommand.register(event.getDispatcher(), event.getBuildContext());
    }
}
