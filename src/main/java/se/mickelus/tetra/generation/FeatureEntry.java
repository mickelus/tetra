package se.mickelus.tetra.generation;

import net.minecraft.inventory.IInventory;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootTable;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.template.IntegrityProcessor;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.gen.placement.TopSolidRangeConfig;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.generation.processing.ForgedContainerProcessor;
import se.mickelus.tetra.generation.processing.ForgedCrateProcessor;
import se.mickelus.tetra.generation.processing.ForgedHammerProcessor;
import se.mickelus.tetra.generation.processing.TransferUnitProcessor;
import se.mickelus.tetra.util.ItemHandlerWrapper;
import se.mickelus.tetra.util.RotationHelper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class FeatureEntry extends Feature<NoFeatureConfig> { //<FeatureReference> {
    public static final String key = "feature";
    @ObjectHolder(TetraMod.MOD_ID + ":" + key)
    public static FeatureEntry instance;

    private TemplateManager templateManager;
    private Registry<Biome> biomeRegistry;

    private List<FeatureParameters> entryPoints = Collections.emptyList();

    public FeatureEntry() {
        super(NoFeatureConfig.field_236558_a_);
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
        templateManager = server.func_240792_aT_();
        biomeRegistry = server.func_244267_aX().getRegistry(Registry.BIOME_KEY);
    }

    public void registerFeatures(BiomeLoadingEvent event) {
        event.getGeneration().withFeature(GenerationStage.Decoration.UNDERGROUND_STRUCTURES, withConfiguration(NoFeatureConfig.field_236559_b_));

//        DataManager.featureData.getData().values().stream()
//                .filter(params -> params.biomes.length > 0)
//                .filter(params -> Arrays.stream(params.biomes).anyMatch(biomeName -> biomeName.equalsIgnoreCase(event.getCategory().getName())))
//                .forEach(params -> event.getGeneration().withFeature(
//                        GenerationStage.Decoration.UNDERGROUND_STRUCTURES,
//                        withConfiguration(new FeatureReference(params.location))
//                                .withPlacement(Placement.field_242907_l.configure(new TopSolidRangeConfig(params.minY, params.minY, params.maxY)))));
//                                    .func_242732_c(density));
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
//                                            .withPlacement(Placement.field_242907_l.configure(new TopSolidRangeConfig(params.minY, params.minY, params.maxY)))));
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
    public boolean func_241855_a(ISeedReader world, ChunkGenerator generator, Random rand, BlockPos pos, NoFeatureConfig ref) {
        Biome biome = world.getBiome(pos);
        ResourceLocation dimensionType = world.getWorld().getDimensionKey().getLocation();

        for (FeatureParameters params: entryPoints) {
            if (Arrays.asList(params.dimensions).contains(dimensionType)
                    && Arrays.stream(params.biomes).anyMatch(biomeName -> biomeName.equalsIgnoreCase(biome.getCategory().getName()))
                    && rand.nextFloat() < params.probability) {
                generateFeatureRoot(params, world, pos.up(params.minY + rand.nextInt(params.maxY - params.minY)), rand);
                return true;
            }
        }

        return false;
    }

//    public boolean func_241855_a(ISeedReader world, ChunkGenerator generator, Random rand, BlockPos pos, FeatureReference ref) {
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

    public void generateFeatureRoot(FeatureParameters feature, ISeedReader world, BlockPos pos, Random random) {
        Rotation rotation = Rotation.NONE;
        Mirror mirror = Mirror.NONE;

        if (feature.transform) {
            rotation = Rotation.values()[random.nextInt(Rotation.values().length)];
//            mirror = Mirror.values()[random.nextInt(Mirror.values().length)];
        }

        generateFeature(feature, world, pos, rotation, mirror, random, 0);
    }

    private void generateFeature(FeatureParameters feature, ISeedReader world, BlockPos pos, Rotation rotation, Mirror mirror,
            Random random, int depth) {
        final Template template = templateManager.getTemplate(feature.location);
        if (template != null) {
            final PlacementSettings settings = new PlacementSettings();
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
                        new IntegrityProcessor(random.nextFloat() * (feature.integrityMax - feature.integrityMin) + feature.integrityMin));
            }
            settings.addProcessor(new ForgedContainerProcessor());
            settings.addProcessor(new ForgedCrateProcessor());
            settings.addProcessor(new ForgedHammerProcessor());
            settings.addProcessor(new TransferUnitProcessor());

            // todo 1.16: new BlockPos param here, what does it do?
            boolean blocksAdded = template.func_237146_a_(world, pos, pos, settings, random,2);

            if (blocksAdded) {
                generateLoot(feature, world, pos, settings, random);
            }

            if (depth < ConfigHandler.maxFeatureDepth.get()) {
                generateChildren(feature, world, pos, rotation, mirror, random, depth);
            }
        }
    }

    private void generateChildren(FeatureParameters feature, ISeedReader world, BlockPos pos, Rotation rotation, Mirror mirror,
            Random random, int depth) {
        Arrays.stream(feature.children)
                .filter(child -> child.chance == 1 || random.nextFloat() < child.chance)
                .forEach(child -> {
                    ResourceLocation selectedLocation = child.features[random.nextInt(child.features.length)];
                    FeatureParameters selectedFeature = DataManager.featureData.getData(selectedLocation);


                    if (selectedFeature != null) {
                        Rotation childRotation = rotation.add(RotationHelper.rotationFromFacing(child.facing));
                        PlacementSettings offsetPlacement = new PlacementSettings()
                                .setMirror(mirror)
                                .setRotation(rotation);
                        PlacementSettings originPlacement = new PlacementSettings()
                                .setMirror(mirror)
                                .setRotation(childRotation);

                        // todo: custom child rotation offsetting, keep around until builtin func proves to work
                        // BlockPos childPos = child.offset.rotate(rotation).subtract(selectedFeature.origin.rotate(childRotation));
                        // childPos = childPos.add(pos);
                        BlockPos childPos = Template.transformedBlockPos(offsetPlacement, child.offset)
                                .subtract(Template.transformedBlockPos(originPlacement, selectedFeature.origin))
                                .add(pos);

                        generateFeature(selectedFeature, world, childPos, childRotation, mirror, random, depth + 1);
                    }
                });
    }

    private BlockPos adjustRootPosition(Template template, BlockPos blockPos, Rotation rotation) {
        BlockPos size = template.getSize();
        size = size.rotate(rotation);
        BlockPos offset = new BlockPos(16 - Math.abs(size.getX()) / 2, 0, 16 - Math.abs(size.getZ()) / 2);
        return blockPos.add(offset);
    }

    /**
     * Adds loot from to containers based on the loot field in the feature parameters.
     * @param feature A feature reference
     * @param world The world in which the feature is generating
     * @param pos The base position of the feature
     * @param settings Placement settings
     * @param random Rand
     */
    private void generateLoot(FeatureParameters feature, ISeedReader world, BlockPos pos, PlacementSettings settings, Random random) {
        Arrays.stream(feature.loot).forEach(loot ->
                addLoot(loot.table, world, Template.transformedBlockPos(settings, loot.position).add(pos), random));
    }

    private void addLoot(ResourceLocation lootLocation, ISeedReader world, BlockPos pos, Random random) {
        TileEntity tileEntity = world.getTileEntity(pos);

        ServerWorld serverWorld = world instanceof WorldGenRegion ? world.getWorld() : ((ServerWorld) world);

        if (tileEntity instanceof LockableLootTileEntity) {
            ((LockableLootTileEntity) tileEntity).setLootTable(lootLocation, random.nextLong());
        } else if (tileEntity instanceof IInventory) {
            LootTable lootTable = serverWorld.getServer().getLootTableManager().getLootTableFromLocation(lootLocation);
            LootContext.Builder builder = new LootContext.Builder(serverWorld);

            lootTable.fillInventory((IInventory) tileEntity, builder.build(LootParameterSets.EMPTY));
        } else if (tileEntity != null) {
            tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(handler -> {
                LootTable lootTable = serverWorld.getServer().getLootTableManager().getLootTableFromLocation(lootLocation);
                LootContext.Builder builder = new LootContext.Builder(serverWorld);

                lootTable.fillInventory(new ItemHandlerWrapper(handler), builder.build(LootParameterSets.EMPTY));
            });
        }
    }
}
