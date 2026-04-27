package se.mickelus.tetra;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.mutil.network.PacketHandler;
import se.mickelus.tetra.aspect.TetraEnchantmentHelper;
import se.mickelus.tetra.blocks.multischematic.MultiblockSchematicScrollPacket;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;
import se.mickelus.tetra.client.particle.SpawnParticlesPacket;
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
import se.mickelus.tetra.items.modular.impl.shield.ShieldModuleModel;
import se.mickelus.tetra.items.modular.impl.toolbelt.ToolbeltModule;
import se.mickelus.tetra.module.*;
import se.mickelus.tetra.module.improvement.HonePacket;
import se.mickelus.tetra.module.improvement.SettlePacket;
import se.mickelus.tetra.module.model.FilteredGridTextureModelData;
import se.mickelus.tetra.module.model.GridTextureModelData;
import se.mickelus.tetra.module.model.ModuleModelRegistry;
import se.mickelus.tetra.module.schematic.BookEnchantSchematic;
import se.mickelus.tetra.module.schematic.CleanseSchematic;
import se.mickelus.tetra.module.schematic.RemoveSchematic;
import se.mickelus.tetra.module.schematic.requirement.*;
import se.mickelus.tetra.properties.TetraAttributes;
import se.mickelus.tetra.trades.TradeHandler;
import se.mickelus.tetra.util.TierHelper;
import se.mickelus.tetra.util.ItemAbilityHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.CompletableFuture;

@Mod(TetraMod.MOD_ID)

@ParametersAreNonnullByDefault
public class TetraMod {
    public static final String MOD_ID = "tetra";
    private static final Logger logger = LogManager.getLogger();

    public static TetraMod instance;
    public static PacketHandler packetHandler;

    public TetraMod(ModContainer modContainer) {
        instance = this;

        IEventBus modBus = modContainer.getEventBus();
        if (modBus == null) {
            throw new IllegalStateException("Tetra requires a mod event bus");
        }

        TetraRegistries.init(modBus);
        TetraEnchantmentHelper.init();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientSetup.init(modBus);
        }

        modBus.addListener(this::setup);
        modBus.addListener(this::registerPayloads);
        modBus.addListener(this::onGatherData);
        modBus.addListener(TetraRegistries::registerCapabilities);
        TetraAttributes.registry.register(modBus);
        modBus.addListener(TetraAttributes::onEntityAttributeModification);

        NeoForge.EVENT_BUS.addListener(this::registerCommands);
        NeoForge.EVENT_BUS.register(new ItemEffectHandler());
        NeoForge.EVENT_BUS.register(new TradeHandler());
        NeoForge.EVENT_BUS.register(new DataManager());
        NeoForge.EVENT_BUS.register(new VibrationDebuffer());
        NeoForge.EVENT_BUS.register(GrindstoneMergeHandler.class);
        NeoForge.EVENT_BUS.register(ServerScheduler.class);

        ItemAbilityHelper.init();
        TierHelper.init();

        ConfigHandler.setup(modContainer);

        ModuleModelRegistry.register(GridTextureModelData.TYPE.toString(), GridTextureModelData.class);
        ModuleModelRegistry.register(FilteredGridTextureModelData.TYPE.toString(), FilteredGridTextureModelData.class);
        ModuleModelRegistry.register("tetra:shield", ShieldModuleModel.class);

        new CraftingEffectRegistry();
        // Cross-version compat: this string-keyed class dispatch mirrors upstream 1.20. Do not migrate
        // to typed Codec / MapCodec registries — rewriting forks the codebase from upstream Tetra.
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
        CraftingEffectRegistry.registerEffectType("tetra:explosion", ExplosionOutcome.class);
        CraftingEffectRegistry.registerEffectType("tetra:spawn_effect_cloud", SpawnEffectCloudOutcome.class);
        CraftingEffectRegistry.registerEffectType("tetra:spawn_entity", SpawnEntityOutcome.class);
        CraftingEffectRegistry.registerEffectType("tetra:set_blocks", SetBlocksOutcome.class);
        CraftingEffectRegistry.registerEffectType("tetra:lightning_strike", LightningStrikeOutcome.class);

        new RepairRegistry();

        SchematicRegistry schematicRegistry = new SchematicRegistry();
        schematicRegistry.registerSchematic(new BookEnchantSchematic());

        new ItemUpgradeRegistry();
        ItemUpgradeRegistry.instance.registerReplacementHook(TetraEnchantmentHelper::transferReplacementEnchantments);

        ModuleRegistry moduleRegistry = new ModuleRegistry();
        moduleRegistry.registerModuleType(ResourceLocation.fromNamespaceAndPath(MOD_ID, "basic_module"), BasicModule::new);
        moduleRegistry.registerModuleType(ResourceLocation.fromNamespaceAndPath(MOD_ID, "multi_module"), MultiSlotModule::new);
        moduleRegistry.registerModuleType(ResourceLocation.fromNamespaceAndPath(MOD_ID, "basic_major_module"), BasicMajorModule::new);
        moduleRegistry.registerModuleType(ResourceLocation.fromNamespaceAndPath(MOD_ID, "multi_major_module"), MultiSlotMajorModule::new);
        moduleRegistry.registerModuleType(ResourceLocation.fromNamespaceAndPath(MOD_ID, "toolbelt_module"), ToolbeltModule::new);

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
        ItemEffectOutcome.register("tetra:spawn_entity", SpawnEntityItemEffectOutcome.class);

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

    private void onGatherData(final GatherDataEvent event) {
        DataGenerator dataGenerator = event.getGenerator();
        DataGenerator gen = event.getGenerator();
        PackOutput packOutput = gen.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        if (event.includeServer()) {
            dataGenerator.addProvider(true, new TetraBlockStateProvider(packOutput, MOD_ID, event.getExistingFileHelper()));
            dataGenerator.addProvider(true, new TetraTagsProvider(packOutput, lookupProvider, MOD_ID, event.getExistingFileHelper()));
            dataGenerator.addProvider(true, new TetraLootTableProvider(packOutput, lookupProvider));
        }
        if (event.includeClient()) {
            dataGenerator.addProvider(true, new StatBarProvider(packOutput));
        }
    }

    public void setup(FMLCommonSetupEvent event) {
        WorkbenchTile.init();

        SchematicRegistry.instance.registerSchematic(new CleanseSchematic());
        SchematicRegistry.instance.registerSchematic(new RemoveSchematic());
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        packetHandler.registerClientBoundPacket(HonePacket.class, HonePacket::new);
        packetHandler.registerClientBoundPacket(SettlePacket.class, SettlePacket::new);
        packetHandler.registerClientBoundPacket(UpdateDataPacket.class, UpdateDataPacket::new);
        packetHandler.registerServerBoundPacket(SecondaryAbilityPacket.class, SecondaryAbilityPacket::new);
        packetHandler.registerServerBoundPacket(ChargedAbilityPacket.class, ChargedAbilityPacket::new);
        packetHandler.registerServerBoundPacket(TruesweepPacket.class, TruesweepPacket::new);
        packetHandler.registerServerBoundPacket(HowlingPacket.class, HowlingPacket::new);
        packetHandler.registerClientBoundPacket(ProjectileMotionPacket.class, ProjectileMotionPacket::new);
        packetHandler.registerClientBoundPacket(AddRevengePacket.class, AddRevengePacket::new);
        packetHandler.registerClientBoundPacket(RemoveRevengePacket.class, RemoveRevengePacket::new);
        packetHandler.registerServerBoundPacket(LungeEchoPacket.class, LungeEchoPacket::new);
        packetHandler.registerServerBoundPacket(MultiblockSchematicScrollPacket.class, MultiblockSchematicScrollPacket::new);
        packetHandler.registerServerBoundPacket(SecondaryInteractionPacket.class, SecondaryInteractionPacket::new);
        packetHandler.registerClientBoundPacket(SpawnParticlesPacket.class, SpawnParticlesPacket::new);

        WorkbenchTile.registerPackets(packetHandler);
        TetraRegistries.registerPackets(packetHandler);
        packetHandler.registerPayloads(event);
    }

    private void registerCommands(RegisterCommandsEvent event) {
        ModuleDevCommand.register(event.getDispatcher(), event.getBuildContext());
        TetraCommand.register(event.getDispatcher(), event.getBuildContext());
    }
}
