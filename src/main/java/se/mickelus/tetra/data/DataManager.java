package se.mickelus.tetra.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.mutil.data.DataDistributor;
import se.mickelus.mutil.data.DataStore;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.PropertyMatcher;
import se.mickelus.tetra.blocks.workbench.action.ConfigActionImpl;
import se.mickelus.tetra.craftingeffect.CraftingEffect;
import se.mickelus.tetra.craftingeffect.condition.CraftingEffectCondition;
import se.mickelus.tetra.craftingeffect.outcome.CraftingEffectOutcome;
import se.mickelus.tetra.data.deserializer.*;
import se.mickelus.tetra.generation.FeatureParameters;
import se.mickelus.tetra.module.Priority;
import se.mickelus.tetra.module.ReplacementDefinition;
import se.mickelus.tetra.module.data.*;
import se.mickelus.tetra.module.improvement.DestabilizationEffect;
import se.mickelus.tetra.module.schematic.OutcomeDefinition;
import se.mickelus.tetra.module.schematic.OutcomeMaterial;
import se.mickelus.tetra.module.schematic.RepairDefinition;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Map;

@ParametersAreNonnullByDefault
public class DataManager implements DataDistributor {

    // todo: use the same naming for all deserializers?
    public static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(ToolData.class, new ToolData.Deserializer())
            .registerTypeAdapter(AspectData.class, new AspectData.Deserializer())
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
            .registerTypeAdapter(AttributesDeserializer.typeToken.getRawType(), new AttributesDeserializer())
            .registerTypeAdapter(VariantData.class, new VariantData.Deserializer())
            .registerTypeAdapter(ImprovementData.class, new ImprovementData.Deserializer())
            .registerTypeAdapter(OutcomeDefinition.class, new OutcomeDefinition.Deserializer())
            .registerTypeAdapter(MaterialColors.class, new MaterialColors.Deserializer())
            .registerTypeAdapter(CraftingEffectCondition.class, new CraftingEffectCondition.Deserializer())
            .registerTypeAdapter(CraftingEffectOutcome.class, new CraftingEffectOutcome.Deserializer())
            .registerTypeAdapter(Item.class, new ItemDeserializer())
            .registerTypeAdapter(Enchantment.class, new EnchantmentDeserializer())
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocationDeserializer())
            .create();
    public static DataManager instance;

    public final DataStore<ResourceLocation[]> tierData;
    public final DataStore<TweakData[]> tweakData;
    public final MaterialStore materialData;
    public final DataStore<ImprovementData[]> improvementData;
    public final DataStore<ModuleData> moduleData;
    public final DataStore<RepairDefinition> repairData;
    public final DataStore<EnchantmentMapping[]> enchantmentData;
    public final DataStore<SynergyData[]> synergyData;
    public final DataStore<ReplacementDefinition[]> replacementData;
    public final SchematicStore schematicData;
    public final DataStore<CraftingEffect> craftingEffectData;
    public final DataStore<ConfigActionImpl[]> actionData;
    public final DataStore<DestabilizationEffect[]> destabilizationData;
    public final DataStore<FeatureParameters> featureData;
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
        this.synergyData = new DataStore<>(gson, TetraMod.MOD_ID, "synergies", SynergyData[].class, this);
        this.replacementData = new DataStore<>(gson, TetraMod.MOD_ID, "replacements", ReplacementDefinition[].class, this);
        this.schematicData = new SchematicStore(gson, TetraMod.MOD_ID, "schematics", this);
        this.craftingEffectData = new CraftingEffectStore(gson, TetraMod.MOD_ID, "crafting_effects", this);
        this.actionData = new DataStore<>(gson, TetraMod.MOD_ID, "actions", ConfigActionImpl[].class, this);
        this.destabilizationData = new DataStore<>(gson, TetraMod.MOD_ID, "destabilization", DestabilizationEffect[].class, this);
        this.featureData = new FeatureStore(gson, TetraMod.MOD_ID, "structures", this);

        dataStores = new DataStore[]{tierData, tweakData, materialData, improvementData, moduleData, enchantmentData, synergyData,
                replacementData, schematicData, craftingEffectData, repairData, actionData, destabilizationData, featureData};
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
        logger.info("Sending data to client: {}", event.getPlayer().getName().getString());
        for (DataStore dataStore : dataStores) {
            dataStore.sendToPlayer((ServerPlayer) event.getPlayer());
        }
    }

    public void onDataRecieved(String directory, Map<ResourceLocation, String> data) {
        Arrays.stream(dataStores)
                .filter(dataStore -> dataStore.getDirectory().equals(directory))
                .forEach(dataStore -> dataStore.loadFromPacket(data));
    }

    /**
     * Wrapped data getter for synergy data so that data may be ordered in such a way that it's efficiently compared. Skipping this step
     * would cause items to incorrectly gain synergies.
     *
     * @param path The path to the synergy data
     * @return An array of synergy data
     */
    public SynergyData[] getSynergyData(String path) {
        SynergyData[] data = synergyData.getDataIn(new ResourceLocation(TetraMod.MOD_ID, path)).stream()
                .flatMap(Arrays::stream)
                .toArray(SynergyData[]::new);
        for (SynergyData entry : data) {
            Arrays.sort(entry.moduleVariants);
            Arrays.sort(entry.modules);
        }
        return data;
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
