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
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.placement.CountRangeConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.network.PacketHandler;

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

    @Override
    public void init(PacketHandler packetHandler) {
        if (ConfigHandler.generateGeodes.get()) {
            for (Biome biome : ForgeRegistries.BIOMES) {
                biome.addFeature(
                        GenerationStage.Decoration.UNDERGROUND_ORES,
                        Biome.createDecoratedFeature(
                                Feature.ORE,
                                new OreFeatureConfig(OreFeatureConfig.FillerBlockType.NATURAL_STONE, GeodeBlock.instance.getDefaultState(), 3),
                                Placement.COUNT_RANGE,
                                new CountRangeConfig(120, 0, 0, 32)
                        )
                );
            }
        }
    }
}
