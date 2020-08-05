package se.mickelus.tetra.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.PropertyMatcher;
import se.mickelus.tetra.blocks.workbench.action.ConfigActionImpl;
import se.mickelus.tetra.data.deserializer.*;
import se.mickelus.tetra.generation.FeatureParameters;
import se.mickelus.tetra.module.Priority;
import se.mickelus.tetra.module.ReplacementDefinition;
import se.mickelus.tetra.module.data.*;
import se.mickelus.tetra.module.improvement.DestabilizationEffect;
import se.mickelus.tetra.module.schema.Material;
import se.mickelus.tetra.module.schema.RepairDefinition;

import java.util.Arrays;
import java.util.Map;

public class DataManager {

    private Logger logger = LogManager.getLogger();

    // todo: use the same naming for all deserializers?
    public static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(CapabilityData.class, new CapabilityData.Deserializer())
            .registerTypeAdapter(EffectData.class, new EffectData.Deserializer())
            .registerTypeAdapter(GlyphData.class, new GlyphDeserializer())
            .registerTypeAdapter(ModuleModel.class, new ModuleModelDeserializer())
            .registerTypeAdapter(Priority.class, new Priority.PriorityAdapter())
            .registerTypeAdapter(ItemPredicate.class, new ItemPredicateDeserializer())
            .registerTypeAdapter(PropertyMatcher.class, new PropertyMatcherDeserializer())
            .registerTypeAdapter(Material.class, new Material.MaterialDeserializer())
            .registerTypeAdapter(ReplacementDefinition.class, new ReplacementDeserializer())
            .registerTypeAdapter(BlockPos.class, new BlockPosDeserializer())
            .registerTypeAdapter(Block.class, new BlockDeserializer())
            .registerTypeAdapter(Item.class, new ItemDeserializer())
            .registerTypeAdapter(Enchantment.class, new EnchantmentDeserializer())
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocationDeserializer())

            // todo 1.16: might have only been used by extended loot pools, safe to remove?
//            .registerTypeAdapter(RandomValueRange.class, new RandomValueRange.Serializer())
//            .registerTypeAdapter(ILootFunction.class, new LootFunctionManager.Serializer())
//            .registerTypeAdapter(ILootCondition.class, new LootConditionManager.Serializer())
//            .registerTypeAdapter(LootContext.EntityTarget.class, new LootContext.EntityTarget.Serializer())
            .create();

    public static DataStore<TweakData[]> tweakData = new DataStore<>(gson, "tweaks", TweakData[].class);
    public static DataStore<ImprovementData[]> improvementData = new DataStore<>(gson, "improvements", ImprovementData[].class);
    public static DataStore<ModuleData> moduleData = new ModuleStore(gson, "modules");
    public static DataStore<RepairDefinition> repairData = new DataStore<>(gson, "repairs", RepairDefinition.class);
    public static DataStore<EnchantmentMapping[]> enchantmentData = new DataStore<>(gson, "enchantments",
            EnchantmentMapping[].class);
    public static DataStore<SynergyData[]> synergyData = new DataStore<>(gson, "synergies", SynergyData[].class);
    public static DataStore<ReplacementDefinition[]> replacementData = new DataStore<>(gson, "replacements",
            ReplacementDefinition[].class);
    public static SchemaStore schemaData = new SchemaStore(gson, "schemas");
    public static DataStore<ItemPredicate[]> predicateData = new DataStore<>(gson, "predicatus", ItemPredicate[].class);
    public static DataStore<ConfigActionImpl[]> actionData = new DataStore<>(gson, "actions", ConfigActionImpl[].class);
    public static DataStore<DestabilizationEffect[]> destabilizationData = new DataStore<>(gson, "destabilization",
            DestabilizationEffect[].class);
    public static DataStore<FeatureParameters> featureData = new FeatureStore(gson, "structures");

    private DataStore[] dataStores = new DataStore[] { tweakData, improvementData, moduleData, enchantmentData, synergyData,
            replacementData, schemaData, repairData, predicateData, actionData, destabilizationData, featureData };

    public static DataManager instance;

    public DataManager() {
        instance = this;
    }

    @SubscribeEvent
    public void addReloadListener(AddReloadListenerEvent event) {
        logger.debug("Setting up datastore reload listeners");
        Arrays.stream(dataStores).forEach(event::addListener);
    }

    @SubscribeEvent
    public void playerConnected(PlayerEvent.PlayerLoggedInEvent event) {
        // todo: stop this from sending to player in singleplayer (while still sending to others in lan worlds)
        logger.info("Sending data to client: {}", event.getPlayer().getName().getUnformattedComponentText());
        for (DataStore dataStore : dataStores) {
            dataStore.sendToPlayer((ServerPlayerEntity) event.getPlayer());
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
     * @param path The path to the synergy data
     * @return An array of synergy data
     */
    public SynergyData[] getSynergyData(String path) {
        SynergyData[] data = synergyData.getData(new ResourceLocation(TetraMod.MOD_ID, path));
        for (SynergyData entry : data) {
            Arrays.sort(entry.moduleVariants);
            Arrays.sort(entry.modules);
        }
        return data;
    }
}
