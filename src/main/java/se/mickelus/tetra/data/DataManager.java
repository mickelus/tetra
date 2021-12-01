package se.mickelus.tetra.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
public class DataManager {

    private final Logger logger = LogManager.getLogger();

    // todo: use the same naming for all deserializers?
    public static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(ToolData.class, new ToolData.Deserializer())
            .registerTypeAdapter(EffectData.class, new EffectData.Deserializer())
            .registerTypeAdapter(GlyphData.class, new GlyphDeserializer())
            .registerTypeAdapter(ModuleModel.class, new ModuleModelDeserializer())
            .registerTypeAdapter(Priority.class, new Priority.Deserializer())
            .registerTypeAdapter(ItemPredicate.class, new ItemPredicateDeserializer())
            .registerTypeAdapter(PropertyMatcher.class, new PropertyMatcherDeserializer())
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

    public static DataStore<TweakData[]> tweakData = new DataStore<>(gson, "tweaks", TweakData[].class);
    public static DataStore<MaterialData> materialData = new MaterialStore(gson, "materials");
    public static DataStore<ImprovementData[]> improvementData = new ImprovementStore(gson, "improvements");
    public static DataStore<ModuleData> moduleData = new ModuleStore(gson, "modules");
    public static DataStore<RepairDefinition> repairData = new DataStore<>(gson, "repairs", RepairDefinition.class);
    public static DataStore<EnchantmentMapping[]> enchantmentData = new DataStore<>(gson, "enchantments",
            EnchantmentMapping[].class);
    public static DataStore<SynergyData[]> synergyData = new DataStore<>(gson, "synergies", SynergyData[].class);
    public static DataStore<ReplacementDefinition[]> replacementData = new DataStore<>(gson, "replacements",
            ReplacementDefinition[].class);
    public static SchematicStore schematicData = new SchematicStore(gson, "schematics");
    public static DataStore<CraftingEffect> craftingEffectData = new CraftingEffectStore(gson, "crafting_effects");
    public static DataStore<ItemPredicate[]> predicateData = new DataStore<>(gson, "predicatus", ItemPredicate[].class);
    public static DataStore<ConfigActionImpl[]> actionData = new DataStore<>(gson, "actions", ConfigActionImpl[].class);
    public static DataStore<DestabilizationEffect[]> destabilizationData = new DataStore<>(gson, "destabilization",
            DestabilizationEffect[].class);
    public static DataStore<FeatureParameters> featureData = new FeatureStore(gson, "structures");

    private final DataStore[] dataStores = new DataStore[] { tweakData, materialData, improvementData, moduleData, enchantmentData, synergyData,
            replacementData, schematicData, craftingEffectData, repairData, predicateData, actionData, destabilizationData, featureData };

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
    public void tagsUpdated(TagsUpdatedEvent event) {
        logger.debug("Reloaded tags");
    }

    @SubscribeEvent
    public void playerConnected(PlayerEvent.PlayerLoggedInEvent event) {
        // todo: stop this from sending to player in singleplayer (while still sending to others in lan worlds)
        logger.info("Sending data to client: {}", event.getPlayer().getName().getContents());
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
}
