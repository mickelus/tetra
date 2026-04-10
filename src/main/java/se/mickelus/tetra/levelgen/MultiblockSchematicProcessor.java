package se.mickelus.tetra.levelgen;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import se.mickelus.tetra.compat.forge.registries.RegistryObject;
import se.mickelus.tetra.blocks.multischematic.MultiblockSchematicBlock;
import se.mickelus.tetra.blocks.multischematic.PrimaryMultiblockSchematicBlock;
import se.mickelus.tetra.blocks.multischematic.RuinedMultiblockSchematicBlock;
import se.mickelus.tetra.util.StreamHelper;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;
import java.util.stream.IntStream;

@ParametersAreNonnullByDefault
public class MultiblockSchematicProcessor extends StructureProcessor {
    public static final MultiblockSchematicProcessor INSTANCE = new MultiblockSchematicProcessor();
    public static final MapCodec<MultiblockSchematicProcessor> codec = MapCodec.unit(MultiblockSchematicProcessor.INSTANCE);
    public static RegistryObject<StructureProcessorType<?>> type;

    public MultiblockSchematicProcessor() {
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo process(LevelReader world, BlockPos pos, BlockPos pos2, StructureTemplate.StructureBlockInfo $,
            StructureTemplate.StructureBlockInfo blockInfo, StructurePlaceSettings placementSettings, @Nullable StructureTemplate template) {
        if (blockInfo.state().getBlock() instanceof MultiblockSchematicBlock block) {
            Random random = new Random(Mth.getSeed(pos));
            int size = block.height * block.width;
            boolean isRuined = IntStream.range(0, size)
                    .boxed()
                    .collect(StreamHelper.toShuffledList(random))
                    .stream()
                    .limit((int) Math.ceil(size * 0.25))
                    .anyMatch(index -> index == block.y * block.width + block.x);

            if (isRuined) {
                BlockState newState = block.ruinedRef.get().defaultBlockState()
                        .setValue(RuinedMultiblockSchematicBlock.facingProp, blockInfo.state().getValue(MultiblockSchematicBlock.facingProp));
                return new StructureTemplate.StructureBlockInfo(blockInfo.pos(), newState, blockInfo.nbt());
            } else if (blockInfo.state().getBlock() instanceof PrimaryMultiblockSchematicBlock) {
                BlockState newState = blockInfo.state().setValue(PrimaryMultiblockSchematicBlock.complete, false);
                return new StructureTemplate.StructureBlockInfo(blockInfo.pos(), newState, blockInfo.nbt());
            }
        }

        return blockInfo;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return type.get();
    }
}
