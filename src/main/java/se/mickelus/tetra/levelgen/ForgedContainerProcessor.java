package se.mickelus.tetra.levelgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.registries.RegistryObject;
import se.mickelus.tetra.blocks.forged.container.ForgedContainerBlock;
import se.mickelus.tetra.blocks.forged.container.ForgedContainerBlockEntity;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ForgedContainerProcessor extends StructureProcessor {
    public static final ForgedContainerProcessor INSTANCE = new ForgedContainerProcessor();
    public static final Codec<ForgedContainerProcessor> codec = Codec.unit(() -> ForgedContainerProcessor.INSTANCE);
    public static RegistryObject<StructureProcessorType<?>> type;

    public ForgedContainerProcessor() {
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo process(LevelReader world, BlockPos pos, BlockPos pos2, StructureTemplate.StructureBlockInfo $,
            StructureTemplate.StructureBlockInfo blockInfo, StructurePlaceSettings placementSettings, @Nullable StructureTemplate template) {
        if (blockInfo.state.getBlock() instanceof ForgedContainerBlock) {
            RandomSource random;

            // this ensures that both blockstates for the container gets generated from the same seed, then there's no need to sync later
            if (blockInfo.state.getValue(ForgedContainerBlock.flippedProp)) {
                random = placementSettings.getRandom(blockInfo.pos.relative(blockInfo.state.getValue(ForgedContainerBlock.facingProp).getCounterClockWise()));
            } else {
                random = placementSettings.getRandom(blockInfo.pos);
            }

            CompoundTag newCompound = blockInfo.nbt.copy();

            int[] lockIntegrity = new int[ForgedContainerBlockEntity.lockCount];
            for (int i = 0; i < lockIntegrity.length; i++) {
                lockIntegrity[i] = 1 + random.nextInt(ForgedContainerBlockEntity.lockIntegrityMax - 1);
            }
            ForgedContainerBlockEntity.writeLockData(newCompound, lockIntegrity);

            int lidIntegrity = 1 + random.nextInt(ForgedContainerBlockEntity.lidIntegrityMax - 1);
            ForgedContainerBlockEntity.writeLidData(newCompound, lidIntegrity);

            BlockState newState = ForgedContainerBlockEntity.getUpdatedBlockState(blockInfo.state, lockIntegrity, lidIntegrity);

            return new StructureTemplate.StructureBlockInfo(blockInfo.pos, newState, newCompound);
        }

        return blockInfo;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return type.get();
    }
}
