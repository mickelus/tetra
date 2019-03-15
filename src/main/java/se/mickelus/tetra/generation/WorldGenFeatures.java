package se.mickelus.tetra.generation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.structure.template.*;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableManager;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.server.FMLServerHandler;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.RotationHelper;
import se.mickelus.tetra.data.DataHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.generation.processor.CompoundProcessor;
import se.mickelus.tetra.generation.processor.ForgedContainerProcessor;
import se.mickelus.tetra.generation.processor.ForgedCrateProcessor;
import se.mickelus.tetra.generation.processor.HammerProcessor;

import java.util.*;

public class WorldGenFeatures implements IWorldGenerator {

    GenerationFeature[] features;
    TemplateManager templateManager;

    public static WorldGenFeatures instance;

    public WorldGenFeatures() {
        features = DataHandler.instance.getGenerationFeatures();

        DataFixer dataFixer;
        if (FMLCommonHandler.instance().getEffectiveSide().equals(Side.CLIENT)) {
            dataFixer = Minecraft.getMinecraft().getDataFixer();
        } else {
            dataFixer = FMLServerHandler.instance().getServer().getDataFixer();
        }
        templateManager = new TemplateManager(TetraMod.MOD_ID, dataFixer);

        // reloads features once per second when in development mode
        if (ConfigHandler.development) {
            new Timer("featureReload").schedule(new TimerTask() {
                @Override
                public void run() {
                    GenerationFeature[] features = DataHandler.instance.getGenerationFeatures();
                    Minecraft.getMinecraft().addScheduledTask(() -> WorldGenFeatures.instance.features = features);
                }
            }, 0, 1000);
        }

        instance = this;
    }

    public GenerationFeature getFeature(String name) {
        return Arrays.stream(features)
                .filter(feature -> feature.location.getResourcePath().equals(name))
                .findFirst()
                .orElse(null);
    }

    public GenerationFeature getFeature(ResourceLocation location) {
        return Arrays.stream(features)
                .filter(feature -> feature.location.equals(location))
                .findFirst()
                .orElse(null);
    }

    public GenerationFeature[] getFeatures() {
        return features;
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        Arrays.stream(features)
                .filter(feature -> {
                    Biome biome = world.getBiome(new BlockPos(chunkX * 16, 0, chunkZ * 16));
                    Set<BiomeDictionary.Type> types = BiomeDictionary.getTypes(biome);
                    return types.stream().anyMatch(type -> Arrays.stream(feature.biomes).anyMatch(biomeName -> biomeName.equals(type.getName().toLowerCase())));
                })
                .filter(feature -> random.nextFloat() < feature.probability)
                .forEach(feature -> generateFeatureRoot(feature, chunkX, chunkZ, world, random));
    }

    public void generateFeatureRoot(GenerationFeature feature, int chunkX, int chunkZ, World world, Random random) {
        BlockPos blockPos = new BlockPos(chunkX * 16 + 1, feature.minY, chunkZ * 16 + 1);

        if (feature.maxY > feature.minY) {
            blockPos = blockPos.add(0, random.nextInt(feature.maxY - feature.minY), 0);
        }

        Rotation rotation = Rotation.NONE;
        Mirror mirror = Mirror.NONE;

        if (feature.transform) {
            rotation = Rotation.values()[random.nextInt(Rotation.values().length)];
//            mirror = Mirror.values()[random.nextInt(Mirror.values().length)];
        }

        generateFeature(feature, world, blockPos, rotation, mirror, random, 0);
    }

    private void generateFeature(GenerationFeature feature, World world, BlockPos pos, Rotation rotation, Mirror mirror,
                                 Random random, int depth) {
        final Template template = templateManager.getTemplate(world.getMinecraftServer(), feature.location);
        final PlacementSettings settings = new PlacementSettings();
        settings.setRotation(rotation);

        // todo: child mirroring needs to be in place for this to work properly
        // settings.setMirror(mirror);
        settings.setRandom(random);
        settings.setIntegrity(random.nextFloat() * (feature.integrityMax - feature.integrityMin) + feature.integrityMin);

        if (depth == 0) {
            pos = template.getZeroPositionWithTransform(pos, mirror, rotation);
            pos = adjustRootPosition(feature, world, pos, rotation);
        }

        ITemplateProcessor processors = new CompoundProcessor(
                new BlockRotationProcessor(pos, settings),
                new HammerProcessor(settings.getRandom(pos)),
                new ForgedCrateProcessor(settings.getRandom(pos)),
                new ForgedContainerProcessor(settings.getRandom(pos)));

        template.addBlocksToWorld(world, pos, processors, settings, 2);

        generateLoot(feature, template, world, pos, settings, random);

        if (depth < ConfigHandler.max_feature_depth) {
            generateChildren(feature, world, pos, rotation, mirror, random, depth);
        }
    }

    private void generateChildren(GenerationFeature feature, World world, BlockPos pos, Rotation rotation, Mirror mirror,
                                  Random random, int depth) {
        Arrays.stream(feature.children)
                .filter(child -> child.chance == 1 || random.nextFloat() < child.chance)
                .forEach(child -> {
                    ResourceLocation selectedLocation = child.features[random.nextInt(child.features.length)];
                    GenerationFeature selectedFeature = getFeature(selectedLocation);


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

    private BlockPos adjustRootPosition(GenerationFeature feature, World world, BlockPos blockPos, Rotation rotation) {
        final Template template = templateManager.getTemplate(world.getMinecraftServer(), feature.location);
        BlockPos size = template.getSize();
        size = size.rotate(rotation);
        BlockPos offset = new BlockPos(16 - Math.abs(size.getX()) / 2, 0, 16 - Math.abs(size.getZ()) / 2);
        return blockPos.add(offset);
    }

    private void generateLoot(GenerationFeature feature, Template template, World world, BlockPos pos,
                              PlacementSettings settings, Random random) {
        // add from data blocks
        template.getDataBlocks(pos, settings).entrySet().stream()
                .filter(entry -> entry.getValue().startsWith("loot="))
                .forEach(entry -> {
                    ResourceLocation lootLocation = new ResourceLocation(entry.getValue().substring(5));
                    addLoot(lootLocation, world, entry.getKey().down(), random);
                });

        // add from feature config
        Arrays.stream(feature.loot).forEach(loot ->
                addLoot(loot.table, world, Template.transformedBlockPos(settings, loot.position).add(pos), random));
    }

    private void addLoot(ResourceLocation lootLocation, World world, BlockPos pos, Random random) {
        TileEntity tileEntity = world.getTileEntity(pos);

        if (tileEntity instanceof TileEntityLockableLoot) {
            ((TileEntityLockableLoot) tileEntity).setLootTable(lootLocation, random.nextLong());
        } else if (tileEntity instanceof IInventory) {
            // todo: implement setter interface for lockable loot TEs?
            LootTable lootTable = world.getLootTableManager().getLootTableFromLocation(lootLocation);
            LootContext.Builder builder = new LootContext.Builder((WorldServer)world);

            lootTable.fillInventory((IInventory) tileEntity, random, builder.build());
        }
    }
}
