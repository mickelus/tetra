package se.mickelus.tetra.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.math.Transformation;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import se.mickelus.mutil.data.DataDistributor;
import se.mickelus.mutil.data.DataStore;
import se.mickelus.mutil.data.deserializer.BlockDeserializer;
import se.mickelus.mutil.data.deserializer.BlockPosDeserializer;
import se.mickelus.mutil.data.deserializer.ItemDeserializer;
import se.mickelus.mutil.data.deserializer.ResourceLocationDeserializer;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.aspect.ItemAspect;
import se.mickelus.tetra.blocks.PropertyMatcher;
import se.mickelus.tetra.blocks.workbench.action.ConfigActionImpl;
import se.mickelus.tetra.blocks.workbench.unlocks.UnlockData;
import se.mickelus.tetra.craftingeffect.CraftingEffect;
import se.mickelus.tetra.craftingeffect.condition.CraftingEffectCondition;
import se.mickelus.tetra.craftingeffect.outcome.CraftingEffectOutcome;
import se.mickelus.tetra.data.deserializer.*;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.effect.data.ItemEffectTrigger;
import se.mickelus.tetra.effect.data.condition.ItemEffectCondition;
import se.mickelus.tetra.effect.data.outcome.ItemEffectOutcome;
import se.mickelus.tetra.effect.data.provider.entity.EntityProvider;
import se.mickelus.tetra.effect.data.provider.number.NumberProvider;
import se.mickelus.tetra.effect.data.provider.vector.VectorProvider;
import se.mickelus.tetra.effect.modifier.ModifierType;
import se.mickelus.tetra.items.modular.impl.dynamic.ArchetypeDefinition;
import se.mickelus.tetra.module.Priority;
import se.mickelus.tetra.module.ReplacementDefinition;
import se.mickelus.tetra.module.data.*;
import se.mickelus.tetra.module.schematic.OutcomeDefinition;
import se.mickelus.tetra.module.schematic.OutcomeMaterial;
import se.mickelus.tetra.module.schematic.RepairDefinition;
import se.mickelus.tetra.module.schematic.requirement.CraftingRequirement;
import se.mickelus.tetra.module.schematic.requirement.CraftingRequirementDeserializer;
import se.mickelus.tetra.module.schematic.requirement.IntegerPredicate;
import se.mickelus.tetra.module.schematic.requirement.ModuleRequirement;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Map;

@ParametersAreNonnullByDefault
public class DataManager implements DataDistributor {

    // todo: use the same naming for all deserializers?
    public static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(ToolData.class, new ToolData.Deserializer())
            .registerTypeAdapter(AspectData.class, new AspectData.Deserializer())
            .registerTypeAdapter(ItemAspect.class, new ItemAspect.Deserializer())
            .registerTypeAdapter(EffectData.class, new EffectData.Deserializer())
            .registerTypeAdapter(GlyphData.class, new GlyphDeserializer())
            .registerTypeAdapter(ModuleModel.class, new ModuleModelDeserializer())
            .registerTypeAdapter(Priority.class, new Priority.Deserializer())
            .registerTypeAdapter(ItemPredicate.class, new ItemPredicateDeserializer())
            .registerTypeAdapter(PropertyMatcher.class, new PropertyMatcherDeserializer())
            .registerTypeAdapter(MaterialData.class, new MaterialData.Deserializer())
            .registerTypeAdapter(OutcomeMaterial.class, new OutcomeMaterial.Deserializer())
            .registerTypeAdapter(ReplacementDefinition.class, new ReplacementDeserializer())
            .registerTypeAdapter(BlockPos.class, new BlockPosDeserializer())
            .registerTypeAdapter(Block.class, new BlockDeserializer())
            .registerTypeAdapter(BlockState.class, new BlockStateDeserializer())
            .registerTypeAdapter(AttributesDeserializer.typeToken.getRawType(), new AttributesDeserializer())
            .registerTypeAdapter(ItemTagKeyDeserializer.typeToken.getRawType(), new ItemTagKeyDeserializer())
            .registerTypeAdapter(VariantData.class, new VariantData.Deserializer())
            .registerTypeAdapter(ImprovementData.class, new ImprovementData.Deserializer())
            .registerTypeAdapter(OutcomeDefinition.class, new OutcomeDefinition.Deserializer())
            .registerTypeAdapter(MaterialColors.class, new MaterialColors.Deserializer())
            .registerTypeAdapter(CraftingEffectCondition.class, new CraftingEffectCondition.Deserializer())
            .registerTypeAdapter(CraftingEffectOutcome.class, new CraftingEffectOutcome.Deserializer())
            .registerTypeAdapter(CraftingRequirement.class, new CraftingRequirementDeserializer())
            .registerTypeAdapter(ModuleRequirement.class, new ModuleRequirement.Deserializer())
            .registerTypeAdapter(IntegerPredicate.class, new IntegerPredicate.Deserializer())
            .registerTypeAdapter(Item.class, new ItemDeserializer())
            .registerTypeAdapter(ItemStack.class, new ItemStackDeserializer())
            .registerTypeAdapter(Enchantment.class, new EnchantmentDeserializer())
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocationDeserializer())
            .registerTypeAdapter(Vector3f.class, new VectorDeserializer())
            .registerTypeAdapter(Quaternionf.class, new QuaternionDeserializer())
            .registerTypeAdapter(Transformation.class, new TransformationDeserializer())
            .registerTypeAdapter(AABB.class, new AABBDeserializer())
            .registerTypeAdapter(ItemDisplayContext.class, new ItemDisplayContextDeserializer())
            .registerTypeAdapter(ItemEffect.class, new ItemEffect.Deserializer())
            .registerTypeAdapter(ItemEffectTrigger.class, new ItemEffectTrigger.Deserializer())
            .registerTypeAdapter(ItemEffectCondition.class, new ItemEffectCondition.Deserializer())
            .registerTypeAdapter(ItemEffectOutcome.class, new ItemEffectOutcome.Deserializer())
            .registerTypeAdapter(NumberProvider.class, new NumberProvider.Deserializer())
            .registerTypeAdapter(EntityProvider.class, new EntityProvider.Deserializer())
            .registerTypeAdapter(VectorProvider.class, new VectorProvider.Deserializer())
            .registerTypeAdapter(ModifierType.class, new ModifierType.Deserializer())
            .registerTypeAdapter(EntityPredicate.class, new EntityPredicateDeserializer())
            .registerTypeAdapter(ParticleOptions.class, new ParticleOptionsDeserializer())
            .registerTypeAdapter(SoundEvent.class, new SoundEventDeserializer())
            .registerTypeAdapter(MobEffect.class, new MobEffectDeserializer())
            .registerTypeAdapter(EntityType.class, new EntityTypeDeserializer())
            .registerTypeAdapter(CompoundTag.class, new CompoundTagDeserializer())
            .create();
    public static DataManager instance;

    public final DataStore<ResourceLocation[]> tierData;
    public final DataStore<TweakData[]> tweakData;
    public final MaterialStore materialData;
    public final DataStore<ImprovementData[]> improvementData;
    public final DataStore<ModuleData> moduleData;
    public final DataStore<RepairDefinition> repairData;
    public final DataStore<EnchantmentMapping[]> enchantmentData;
    public final SynergyStore synergyData;
    public final DataStore<ReplacementDefinition[]> replacementData;
    public final SchematicStore schematicData;
    public final DataStore<CraftingEffect> craftingEffectData;
    public final DataStore<ConfigActionImpl[]> actionData;
    public final DataStore<UnlockData> unlockData;
    public final DataStore<ArchetypeDefinition> archetypeData;
    public final ItemEffectStore itemEffectData;
    public final ModifierEffectStore modifierEffectData;

    private final Logger logger = LogManager.getLogger();
    private final DataStore[] dataStores;

    public DataManager() {
        instance = this;

        this.tierData = new DataStore<>(gson, TetraMod.MOD_ID, "tiers", ResourceLocation[].class, this);
        this.tweakData = new DataStore<>(gson, TetraMod.MOD_ID, "tweaks", TweakData[].class, this);
        this.materialData = new MaterialStore(gson, TetraMod.MOD_ID, "materials", this);
        this.improvementData = new ImprovementStore(gson, TetraMod.MOD_ID, "improvements", materialData, this);
        this.moduleData = new ModuleStore(gson, TetraMod.MOD_ID, "modules", this);
        this.repairData = new DataStore<>(gson, TetraMod.MOD_ID, "repairs", RepairDefinition.class, this);
        this.enchantmentData = new DataStore<>(gson, TetraMod.MOD_ID, "enchantments", EnchantmentMapping[].class, this);
        this.synergyData = new SynergyStore(gson, TetraMod.MOD_ID, "synergies", this);
        this.replacementData = new DataStore<>(gson, TetraMod.MOD_ID, "replacements", ReplacementDefinition[].class, this);
        this.schematicData = new SchematicStore(gson, TetraMod.MOD_ID, "schematics", this);
        this.craftingEffectData = new CraftingEffectStore(gson, TetraMod.MOD_ID, "crafting_effects", this);
        this.actionData = new DataStore<>(gson, TetraMod.MOD_ID, "actions", ConfigActionImpl[].class, this);
        this.unlockData = new DataStore<>(gson, TetraMod.MOD_ID, "unlocks", UnlockData.class, this);
        this.archetypeData = new DataStore<>(gson, TetraMod.MOD_ID, "archetypes", ArchetypeDefinition.class, this);
        this.itemEffectData = new ItemEffectStore(gson, TetraMod.MOD_ID, "item_effects", this);
        this.modifierEffectData = new ModifierEffectStore(gson, TetraMod.MOD_ID, "modifier_effects", this);

        dataStores = new DataStore[] { tierData, tweakData, materialData, improvementData, moduleData, enchantmentData, synergyData, replacementData,
                schematicData, craftingEffectData, repairData, actionData, unlockData, archetypeData, itemEffectData, modifierEffectData };
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void addReloadListener(AddReloadListenerEvent event) {
        logger.debug("Setting up datastore reload listeners");
        Arrays.stream(dataStores).forEach(event::addListener);
    }

    @SubscribeEvent
    public void tagsUpdated(TagsUpdatedEvent event) {
        logger.debug("Reloaded tags");
    }

    @SubscribeEvent
    public void playerConnected(PlayerEvent.PlayerLoggedInEvent event) {
        // todo: stop this from sending to player in singleplayer (while still sending to others in lan worlds)
        logger.info("Sending data to client: {}", event.getEntity().getName().getString());
        for (DataStore dataStore : dataStores) {
            dataStore.sendToPlayer((ServerPlayer) event.getEntity());
        }
    }

    public void onDataRecieved(String directory, Map<ResourceLocation, String> data) {
        Arrays.stream(dataStores)
                .filter(dataStore -> dataStore.getDirectory().equals(directory))
                .forEach(dataStore -> dataStore.loadFromPacket(data));
    }

    @Override
    public void sendToAll(String directory, Map<ResourceLocation, JsonElement> data) {
        TetraMod.packetHandler.sendToAllPlayers(new UpdateDataPacket(directory, data));
    }

    @Override
    public void sendToPlayer(ServerPlayer player, String directory, Map<ResourceLocation, JsonElement> data) {
        TetraMod.packetHandler.sendTo(new UpdateDataPacket(directory, data), player);
    }
}
