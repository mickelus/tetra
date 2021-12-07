package se.mickelus.tetra.blocks.geode;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.mutil.network.PacketHandler;

import javax.annotation.ParametersAreNonnullByDefault;

import static net.minecraft.data.worldgen.features.OreFeatures.NATURAL_STONE;

@ParametersAreNonnullByDefault
public class GeodeBlock extends TetraBlock {

    public static final String unlocalizedName = "block_geode";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static GeodeBlock instance;

    private ConfiguredFeature configuredFeature;

    // todo 1.18: tag with pickaxe break tag
    public GeodeBlock() {
        super(Properties.of(Material.STONE)
        .sound(SoundType.STONE)
//        .harvestTool(ToolActions.PICKAXE_DIG)
//        .harvestLevel(0)
        .strength(1.5F, 6.0F));

        setRegistryName(unlocalizedName);

        int density = ConfigHandler.geodeDensity.get();
        if (density > 0) {
            int size = 3;
            int maxHeight = 32;

            // todo 1.18: fix worldgen
            OreConfiguration config = new OreConfiguration(NATURAL_STONE, defaultBlockState(), size);
//            configuredFeature = Feature.ORE.configured(config)
//                    .range(maxHeight)
//                    .squared()
//                    .countRandom(density);
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        return Blocks.STONE.getCloneItemStack(state, target, world, pos, player);
    }

    @Override
    public void init(PacketHandler packetHandler) {
        if (configuredFeature != null) {
            Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, new ResourceLocation(TetraMod.MOD_ID, unlocalizedName), configuredFeature);
        }
    }

    public static void registerFeature(BiomeGenerationSettingsBuilder builder) {
        if (instance.configuredFeature != null) {
//            builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, instance.configuredFeature);
        }
    }
}
