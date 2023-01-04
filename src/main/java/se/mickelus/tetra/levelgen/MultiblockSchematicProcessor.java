package se.mickelus.tetra.levelgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.registries.RegistryObject;
import se.mickelus.tetra.blocks.multischematic.MultiblockSchematicBlock;
import se.mickelus.tetra.blocks.multischematic.RuinedMultiblockSchematicBlock;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ParametersAreNonnullByDefault
public class MultiblockSchematicProcessor extends StructureProcessor {
    public static final MultiblockSchematicProcessor INSTANCE = new MultiblockSchematicProcessor();
    public static final Codec<MultiblockSchematicProcessor> codec = Codec.unit(() -> MultiblockSchematicProcessor.INSTANCE);
    public static RegistryObject<StructureProcessorType<?>> type;

    public MultiblockSchematicProcessor() {
    }

    public static Collector<Integer, ?, List<Integer>> toShuffledList(Random random) {
        return Collectors.collectingAndThen(
                Collectors.toCollection(ArrayList::new),
                list -> {
                    Collections.shuffle(list, random);
                    return list;
                });
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo process(LevelReader world, BlockPos pos, BlockPos pos2, StructureTemplate.StructureBlockInfo $,
            StructureTemplate.StructureBlockInfo blockInfo, StructurePlaceSettings placementSettings, @Nullable StructureTemplate template) {
        if (blockInfo.state.getBlock() instanceof MultiblockSchematicBlock block) {
            Random random = new Random(Mth.getSeed(pos));
            int size = block.height * block.width;
            boolean isRuined = IntStream.range(0, size)
                    .boxed()
                    .collect(toShuffledList(random))
                    .stream()
                    .limit((int) (size * 0.6))
                    .anyMatch(index -> index == block.y * block.width + block.x);

            if (isRuined) {
                BlockState newState = block.ruinedRef.get().defaultBlockState()
                        .setValue(RuinedMultiblockSchematicBlock.facingProp, blockInfo.state.getValue(MultiblockSchematicBlock.facingProp));
                return new StructureTemplate.StructureBlockInfo(blockInfo.pos, newState, blockInfo.nbt);
            }
        }

        return blockInfo;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return type.get();
    }
}
