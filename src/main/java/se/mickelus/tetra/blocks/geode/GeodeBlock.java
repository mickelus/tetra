package se.mickelus.tetra.blocks.geode;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.TetraBlock;

public class GeodeBlock extends TetraBlock {

    public static final String unlocalizedName = "block_geode";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static GeodeBlock instance;

    public GeodeBlock() {
        super(Properties.create(Material.ROCK)
        .sound(SoundType.STONE)
        .harvestTool(ToolType.PICKAXE)
        .harvestLevel(0)
        .hardnessAndResistance(1.5F, 6.0F));

        setRegistryName(unlocalizedName);
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        return Blocks.STONE.getPickBlock(state, target, world, pos, player);
    }

    public static void registerFeature(BiomeGenerationSettingsBuilder builder) {
        int density = ConfigHandler.geodeDensity.get();
        if (density > 0) {
            int size = 3;
            int maxHeight = 32;
            OreFeatureConfig config = new OreFeatureConfig(OreFeatureConfig.FillerBlockType.field_241882_a, GeodeBlock.instance.getDefaultState(), size);
            builder.withFeature(
                    GenerationStage.Decoration.UNDERGROUND_ORES,
                    Feature.ORE.withConfiguration(config)
                            .func_242733_d(maxHeight)
                            .func_242728_a()
                            .func_242732_c(density)
            );
        }
    }
}
