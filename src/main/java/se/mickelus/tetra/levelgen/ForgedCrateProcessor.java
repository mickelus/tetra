package se.mickelus.tetra.levelgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.registries.RegistryObject;
import se.mickelus.tetra.blocks.forged.ForgedCrateBlock;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ForgedCrateProcessor extends StructureProcessor {
    public static final ForgedCrateProcessor INSTANCE = new ForgedCrateProcessor();
    public static final Codec<ForgedCrateProcessor> codec = Codec.unit(() -> ForgedCrateProcessor.INSTANCE);
    public static RegistryObject<StructureProcessorType<?>> type;

    public ForgedCrateProcessor() {
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo process(LevelReader world, BlockPos pos, BlockPos pos2, StructureTemplate.StructureBlockInfo $,
            StructureTemplate.StructureBlockInfo blockInfo, StructurePlaceSettings placementSettings, @Nullable StructureTemplate template) {
        if (blockInfo.state.getBlock() instanceof ForgedCrateBlock) {
            RandomSource random = placementSettings.getRandom(blockInfo.pos);

            BlockState blockState = blockInfo.state
                    .setValue(ForgedCrateBlock.propIntegrity, random.nextInt(4))
                    .setValue(ForgedCrateBlock.propFacing, Direction.Plane.HORIZONTAL.getRandomDirection(random));

            return new StructureTemplate.StructureBlockInfo(blockInfo.pos, blockState, blockInfo.nbt);
        }

        return blockInfo;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return type.get();
    }
}
