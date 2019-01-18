package se.mickelus.tetra.generation.processor;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.ITemplateProcessor;
import net.minecraft.world.gen.structure.template.Template;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CompoundProcessor implements ITemplateProcessor {

    ITemplateProcessor[] processors;

    public CompoundProcessor(ITemplateProcessor ... processors) {
        this.processors = processors;
    }

    @Nullable
    @Override
    public Template.BlockInfo processBlock(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull Template.BlockInfo blockInfo) {
        for (ITemplateProcessor processor : processors) {
            blockInfo = processor.processBlock(world, pos, blockInfo);

            if (blockInfo == null) {
                return blockInfo;
            }
        }
        return blockInfo;
    }
}
