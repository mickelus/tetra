package se.mickelus.tetra.blocks.geode;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.generation.FeatureEntry;
import se.mickelus.tetra.network.PacketHandler;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class GeodeBlock extends TetraBlock {

    public static final String unlocalizedName = "block_geode";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static GeodeBlock instance;

    private ConfiguredFeature configuredFeature;

    public GeodeBlock() {
        super(Properties.of(Material.STONE)
        .sound(SoundType.STONE)
        .harvestTool(ToolType.PICKAXE)
        .harvestLevel(0)
        .strength(1.5F, 6.0F));

        setRegistryName(unlocalizedName);

        int density = ConfigHandler.geodeDensity.get();
        if (density > 0) {
            int size = 3;
            int maxHeight = 32;
            OreConfiguration config = new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, defaultBlockState(), size);
            configuredFeature = Feature.ORE.configured(config)
                    .range(maxHeight)
                    .squared()
                    .countRandom(density);
        }
    }

    @Override
    public ItemStack getPickBlock(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        return Blocks.STONE.getPickBlock(state, target, world, pos, player);
    }

    @Override
    public void init(PacketHandler packetHandler) {
        if (configuredFeature != null) {
            Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, new ResourceLocation(TetraMod.MOD_ID, unlocalizedName), configuredFeature);
        }
    }

    public static void registerFeature(BiomeGenerationSettingsBuilder builder) {
        if (instance.configuredFeature != null) {
            builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, instance.configuredFeature);
        }
    }
}
