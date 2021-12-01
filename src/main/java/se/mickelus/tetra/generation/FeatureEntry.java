package se.mickelus.tetra.generation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.Container;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.generation.processing.ForgedContainerProcessor;
import se.mickelus.tetra.generation.processing.ForgedCrateProcessor;
import se.mickelus.tetra.generation.processing.ForgedHammerProcessor;
import se.mickelus.tetra.generation.processing.TransferUnitProcessor;
import se.mickelus.tetra.util.ItemHandlerWrapper;
import se.mickelus.tetra.util.RotationHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
@ParametersAreNonnullByDefault
public class FeatureEntry extends Feature<NoneFeatureConfiguration> { //<FeatureReference> {
    public static final String key = "feature";
    public static FeatureEntry instance = new FeatureEntry();
    public static ConfiguredFeature<?, ?> configuredInstance = instance.configured(NoneFeatureConfiguration.INSTANCE);

    private StructureManager templateManager;
    private Registry<Biome> biomeRegistry;

    private List<FeatureParameters> entryPoints = Collections.emptyList();

    public FeatureEntry() {
        super(NoneFeatureConfiguration.CODEC);
//        super(FeatureReference.codec);

        setRegistryName(TetraMod.MOD_ID, key);

        if (ConfigHandler.generateFeatures.get()) {
            DataManager.featureData.onReload(this::setupEntryPoints);
        }
    }

    private void setupEntryPoints() {
        entryPoints = DataManager.featureData.getData().values().stream()
                .filter(params -> params.biomes.length > 0)
                .collect(Collectors.toList());
    }

    public void setup(MinecraftServer server) {
        templateManager = server.getStructureManager();
        biomeRegistry = server.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
    }

    public void registerFeatures(BiomeLoadingEvent event) {
        event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_STRUCTURES, configuredInstance);

//        DataManager.featureData.getData().values().stream()
//                .filter(params -> params.biomes.length > 0)
//                .filter(params -> Arrays.stream(params.biomes).anyMatch(biomeName -> biomeName.equalsIgnoreCase(event.getCategory().getName())))
//                .forEach(params -> event.getGeneration().withFeature(
//                        GenerationStage.Decoration.UNDERGROUND_STRUCTURES,
//                        withConfiguration(new FeatureReference(params.location))
//                                .withPlacement(Placement.RANGE.configure(new TopSolidRangeConfig(params.minY, params.minY, params.maxY)))));
//                                    .countRandom(density));
    }

    private void addToBiomes() {
//        StreamSupport.stream(biomeRegistry.spliterator(), false)
//                .forEach(biome -> {
//                    DataManager.featureData.getData().values().stream()
//                            .filter(params -> params.biomes.length > 0)
//                            .filter(params -> Arrays.stream(params.biomes).anyMatch(biomeName -> biomeName.equalsIgnoreCase(biome.getCategory().getName())))
//                            .forEach(params -> biome.getGenerationSettings().
//                                    GenerationStage.Decoration.UNDERGROUND_STRUCTURES,
//                                    withConfiguration(new FeatureReference(params.location))
//                                            .withPlacement(Placement.RANGE.configure(new TopSolidRangeConfig(params.minY, params.minY, params.maxY)))));
//                });

//        if (EffectiveSide.get().isClient()) {
//            return;
//        }
//
//        for (Biome biome : ForgeRegistries.BIOMES) {
//            biome.getFeatures(GenerationStage.Decoration.UNDERGROUND_STRUCTURES)
//                    .removeIf(configuredFeature -> CastOptional.cast(configuredFeature.config, DecoratedFeatureConfig.class)
//                            .filter(config -> FeatureEntry.this.equals(config.feature.feature))
//                            .isPresent());
//        }
//
//        DataManager.featureData.getData().values().stream()
//                .filter(params -> params.biomes.length > 0)
//                .forEach(params -> {
//                    StreamSupport.stream(ForgeRegistries.BIOMES.spliterator(), false)
//                            .filter(biome -> {
//                                Set<BiomeDictionary.Type> types = BiomeDictionary.getTypes(biome);
//                                return types.stream().anyMatch(type ->
//                                        Arrays.stream(params.biomes).anyMatch(biomeName -> biomeName.equalsIgnoreCase(type.getName())));
//                            })
//                            .forEach(biome -> biome.addFeature(
//                                    GenerationStage.Decoration.UNDERGROUND_STRUCTURES,
//                                    withConfiguration(new FeatureReference(params.location)).withPlacement(
//                                            Placement.CHANCE_RANGE.configure(
//                                                    new ChanceRangeConfig(1, params.minY, params.minY, params.maxY))
//                                    )
//                            ));
//        });
    }


    @Override
    public boolean place(WorldGenLevel world, ChunkGenerator generator, Random rand, BlockPos pos, NoneFeatureConfiguration ref) {
        Biome biome = world.getBiome(pos);
        ResourceLocation dimensionType = world.getLevel().dimension().location();

        for (FeatureParameters params: entryPoints) {
            if (Arrays.asList(params.dimensions).contains(dimensionType)
                    && Arrays.stream(params.biomes).anyMatch(biomeName -> biomeName.equalsIgnoreCase(biome.getBiomeCategory().getName()))
                    && rand.nextFloat() < params.probability) {
                generateFeatureRoot(params, world, pos.above(params.minY + rand.nextInt(params.maxY - params.minY)), rand);
                return true;
            }
        }

        return false;
    }

//    public boolean place(ISeedReader world, ChunkGenerator generator, Random rand, BlockPos pos, FeatureReference ref) {
//        FeatureParameters params = DataManager.featureData.getData(ref.location);
//
//        ResourceLocation dimensionType = world.getWorld().getDimensionKey().getLocation();
//
//        if (params != null
//                && Arrays.asList(params.dimensions).contains(dimensionType)
//                && rand.nextFloat() < params.probability) {
//            generateFeatureRoot(params, world, pos, rand);
//            return true;
//        }
//
//        return false;
//    }

    public void generateFeatureRoot(FeatureParameters feature, WorldGenLevel world, BlockPos pos, Random random) {
        Rotation rotation = Rotation.NONE;
        Mirror mirror = Mirror.NONE;

        if (feature.transform) {
            rotation = Rotation.values()[random.nextInt(Rotation.values().length)];
//            mirror = Mirror.values()[random.nextInt(Mirror.values().length)];
        }

        generateFeature(feature, world, pos, rotation, mirror, random, 0);
    }

    private void generateFeature(FeatureParameters feature, WorldGenLevel world, BlockPos pos, Rotation rotation, Mirror mirror,
            Random random, int depth) {
        final StructureTemplate template = templateManager.get(feature.location);
        if (template != null) {
            final StructurePlaceSettings settings = new StructurePlaceSettings();
            settings.setRotation(rotation);

            // todo: child mirroring needs to be in place for this to work properly
            // settings.setMirror(mirror);

            if (depth == 0) {
                pos = template.getZeroPositionWithTransform(pos, mirror, rotation);

                // todo: adjust root so that feature anchor is at pos instead, needed for more advanced placement options
//                pos = adjustRootPosition(template, pos, rotation);
            }

            if (feature.integrityMin < 1) {
                settings.addProcessor(
                        new BlockRotProcessor(random.nextFloat() * (feature.integrityMax - feature.integrityMin) + feature.integrityMin));
            }
            settings.addProcessor(new ForgedContainerProcessor());
            settings.addProcessor(new ForgedCrateProcessor());
            settings.addProcessor(new ForgedHammerProcessor());
            settings.addProcessor(new TransferUnitProcessor());

            // todo 1.16: new BlockPos param here, what does it do?
            boolean blocksAdded = template.placeInWorld(world, pos, pos, settings, random,2);

            if (blocksAdded) {
                generateLoot(feature, world, pos, settings, random);
            }

            if (depth < ConfigHandler.maxFeatureDepth.get()) {
                generateChildren(feature, world, pos, rotation, mirror, random, depth);
            }
        }
    }

    private void generateChildren(FeatureParameters feature, WorldGenLevel world, BlockPos pos, Rotation rotation, Mirror mirror,
            Random random, int depth) {
        Arrays.stream(feature.children)
                .filter(child -> child.chance == 1 || random.nextFloat() < child.chance)
                .forEach(child -> {
                    ResourceLocation selectedLocation = child.features[random.nextInt(child.features.length)];
                    FeatureParameters selectedFeature = DataManager.featureData.getData(selectedLocation);


                    if (selectedFeature != null) {
                        Rotation childRotation = rotation.getRotated(RotationHelper.rotationFromFacing(child.facing));
                        StructurePlaceSettings offsetPlacement = new StructurePlaceSettings()
                                .setMirror(mirror)
                                .setRotation(rotation);
                        StructurePlaceSettings originPlacement = new StructurePlaceSettings()
                                .setMirror(mirror)
                                .setRotation(childRotation);

                        // todo: custom child rotation offsetting, keep around until builtin func proves to work
                        // BlockPos childPos = child.offset.rotate(rotation).subtract(selectedFeature.origin.rotate(childRotation));
                        // childPos = childPos.add(pos);
                        BlockPos childPos = StructureTemplate.calculateRelativePosition(offsetPlacement, child.offset)
                                .subtract(StructureTemplate.calculateRelativePosition(originPlacement, selectedFeature.origin))
                                .offset(pos);

                        generateFeature(selectedFeature, world, childPos, childRotation, mirror, random, depth + 1);
                    }
                });
    }

    private BlockPos adjustRootPosition(StructureTemplate template, BlockPos blockPos, Rotation rotation) {
        BlockPos size = template.getSize();
        size = size.rotate(rotation);
        BlockPos offset = new BlockPos(16 - Math.abs(size.getX()) / 2, 0, 16 - Math.abs(size.getZ()) / 2);
        return blockPos.offset(offset);
    }

    /**
     * Adds loot from to containers based on the loot field in the feature parameters.
     * @param feature A feature reference
     * @param world The world in which the feature is generating
     * @param pos The base position of the feature
     * @param settings Placement settings
     * @param random Rand
     */
    private void generateLoot(FeatureParameters feature, WorldGenLevel world, BlockPos pos, StructurePlaceSettings settings, Random random) {
        Arrays.stream(feature.loot).forEach(loot ->
                addLoot(loot.table, world, StructureTemplate.calculateRelativePosition(settings, loot.position).offset(pos), random));
    }

    private void addLoot(ResourceLocation lootLocation, WorldGenLevel world, BlockPos pos, Random random) {
        BlockEntity tileEntity = world.getBlockEntity(pos);

        ServerLevel serverWorld = world instanceof WorldGenRegion ? world.getLevel() : ((ServerLevel) world);

        if (tileEntity instanceof RandomizableContainerBlockEntity) {
            ((RandomizableContainerBlockEntity) tileEntity).setLootTable(lootLocation, random.nextLong());
        } else if (tileEntity instanceof Container) {
            LootTable lootTable = serverWorld.getServer().getLootTables().get(lootLocation);
            LootContext.Builder builder = new LootContext.Builder(serverWorld);

            lootTable.fill((Container) tileEntity, builder.create(LootContextParamSets.EMPTY));
        } else if (tileEntity != null) {
            tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(handler -> {
                LootTable lootTable = serverWorld.getServer().getLootTables().get(lootLocation);
                LootContext.Builder builder = new LootContext.Builder(serverWorld);

                lootTable.fill(new ItemHandlerWrapper(handler), builder.create(LootContextParamSets.EMPTY));
            });
        }
    }
}
