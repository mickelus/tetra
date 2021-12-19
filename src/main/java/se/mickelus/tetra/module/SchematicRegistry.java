package se.mickelus.tetra.module;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.mutil.util.Filter;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.module.schematic.*;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
public class SchematicRegistry {
    private static final Logger logger = LogManager.getLogger();

    public static SchematicRegistry instance;
    private final Map<ResourceLocation, UpgradeSchematic> dynamicSchematics;
    private Map<ResourceLocation, UpgradeSchematic> schematicMap;

    public SchematicRegistry() {
        instance = this;

        schematicMap = Collections.emptyMap();
        dynamicSchematics = new HashMap<>();

        DataManager.instance.schematicData.onReload(() -> setupSchematics(DataManager.instance.schematicData.getData()));
    }

    public static UpgradeSchematic getSchematic(ResourceLocation identifier) {
        return instance.schematicMap.get(identifier);
    }

    public static UpgradeSchematic getSchematic(String key) {
        return getSchematic(new ResourceLocation(TetraMod.MOD_ID, key));
    }

    public static Collection<UpgradeSchematic> getAllSchematics() {
        return instance.schematicMap.values();
    }

    public static UpgradeSchematic[] getAvailableSchematics(Player player, WorkbenchTile tile, ItemStack itemStack) {
        return getAllSchematics().stream()
                .filter(upgradeSchematic -> playerHasSchematic(player, tile, itemStack, upgradeSchematic))
                .filter(upgradeSchematic -> upgradeSchematic.isApplicableForItem(itemStack))
                .toArray(UpgradeSchematic[]::new);
    }

    public static UpgradeSchematic[] getSchematics(String slot, ItemStack itemStack) {
        return getAllSchematics().stream()
                .filter(upgradeSchematic -> upgradeSchematic.isApplicableForSlot(slot, itemStack))
                .toArray(UpgradeSchematic[]::new);
    }

    public static boolean playerHasSchematic(Player player, WorkbenchTile tile, ItemStack targetStack, UpgradeSchematic schematic) {
        return schematic.isVisibleForPlayer(player, tile, targetStack);
    }

    /**
     * Register a schematic that's not config driven
     *
     * @param schematic
     */
    public void registerSchematic(UpgradeSchematic schematic) {
        dynamicSchematics.put(new ResourceLocation(TetraMod.MOD_ID, schematic.getKey()), schematic);
    }

    private void setupSchematics(Map<ResourceLocation, SchematicDefinition> data) {
        schematicMap = data.entrySet().stream()
                .filter(entry -> validateSchematicDefinition(entry.getKey(), entry.getValue()))
                .flatMap(entry -> createSchematics(entry.getKey(), entry.getValue()).stream())
                .filter(entry -> entry.getRight() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        dynamicSchematics.forEach((identifier, schematic) -> schematicMap.put(identifier, schematic));

        RepairRegistry.instance.injectFromSchematics(data.values());
    }

    private boolean validateSchematicDefinition(ResourceLocation identifier, SchematicDefinition definition) {
        if (definition == null) {
            logger.warn("Failed to create schematic from schematic definition '{}': Data is null (probably due to it failing to parse)",
                    identifier);
            return false;
        }

        if (definition.slots == null || definition.slots.length < 1) {
            logger.warn("Failed to create schematic from schematic definition '{}': Slots field is empty", identifier);
            return false;
        }

        return true;
    }

    // todo: hacky stuff to get multislot modules to work, there has to be another way
    private Collection<Pair<ResourceLocation, ConfigSchematic>> createSchematics(ResourceLocation identifier, SchematicDefinition definition) {
        processDefinition(definition);

        if (definition.slots.length == definition.keySuffixes.length) {
            ArrayList<Pair<ResourceLocation, ConfigSchematic>> result = new ArrayList<>(definition.slots.length);
            for (int i = 0; i < definition.slots.length; i++) {
                try {
                    ResourceLocation suffixedIdentifier = new ResourceLocation(
                            identifier.getNamespace(), identifier.getPath() + definition.keySuffixes[i]);

                    result.add(new ImmutablePair<>(suffixedIdentifier,
                            new ConfigSchematic(definition, definition.keySuffixes[i], definition.slots[i])));
                } catch (InvalidSchematicException e) {
                    e.printMessage();
                }
            }

            return result;
        } else {
            try {
                return Collections.singletonList(new ImmutablePair<>(identifier, new ConfigSchematic(definition)));
            } catch (InvalidSchematicException e) {
                e.printMessage();
            }
        }

        return Collections.singletonList(new ImmutablePair<>(identifier, null));
    }

    private void processDefinition(SchematicDefinition definition) {
        if (definition.applicableMaterials == null) {
            definition.applicableMaterials = Arrays.stream(definition.outcomes)
                    .flatMap(outcome -> {
                        if (outcome instanceof MaterialOutcomeDefinition) {
                            return Arrays.stream(((MaterialOutcomeDefinition) outcome).materials)
                                    .map(ResourceLocation::getPath)
                                    .map(path -> {
                                        if (path.endsWith("/")) {
                                            return "#" + path.substring(0, path.length() - 1);
                                        }

                                        int separatorIndex = path.lastIndexOf("/");
                                        if (separatorIndex != -1) {
                                            return "!" + path.substring(separatorIndex + 1);
                                        }
                                        return "!" + path;
                                    });
                        } else if (outcome.material.isValid()) {
                            ItemStack[] applicableItemStacks = outcome.material.getApplicableItemStacks();
                            if (applicableItemStacks.length > 0) {
                                return Stream.of(applicableItemStacks[0].getItem().getRegistryName().toString());
                            }
                        }

                        return Stream.empty();
                    })
                    .toArray(String[]::new);
        }

        // todo: merge outcomes instead of filtering duplicates
        definition.outcomes = Arrays.stream(definition.outcomes)
                .flatMap(outcome ->
                        outcome instanceof MaterialOutcomeDefinition
                                ? expandMaterialOutcome((MaterialOutcomeDefinition) outcome)
                                : Stream.of(outcome))
                .filter(Filter.distinct(outcome -> outcome.material))
                .sorted((a, b) -> Boolean.compare(b.material != null && b.material.isTagged(), a.material != null && a.material.isTagged()))
                .toArray(OutcomeDefinition[]::new);
    }

    private Stream<OutcomeDefinition> expandMaterialOutcome(MaterialOutcomeDefinition source) {
        return Arrays.stream(source.materials)
                .map(rl -> rl.getPath().endsWith("/")
                        ? DataManager.instance.materialData.getDataIn(rl)
                        : Optional.ofNullable(DataManager.instance.materialData.getData(rl)).map(Collections::singletonList).orElseGet(Collections::emptyList))
                .flatMap(Collection::stream)
                .map(source::combine);
    }
}
