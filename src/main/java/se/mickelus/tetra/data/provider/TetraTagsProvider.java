package se.mickelus.tetra.data.provider;

import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.multischematic.MultiblockSchematicBlock;

public class TetraTagsProvider extends TagsProvider<Block> {

    public TetraTagsProvider(DataGenerator generator, String modId, ExistingFileHelper existingFileHelper) {
        super(generator, Registry.BLOCK, modId, existingFileHelper);
    }

    @Override
    protected void addTags() {
        MultiblockSchematicEntry[] schematics = {
                new MultiblockSchematicEntry("stonecutter", 3, 2),
                new MultiblockSchematicEntry("earthpiercer", 2, 2),
                new MultiblockSchematicEntry("extractor", 3, 3)
        };

        var schematicsAppender = tag(BlockTags.create(new ResourceLocation(TetraMod.MOD_ID, "multiblock_schematic")));
        for (MultiblockSchematicEntry schematic : schematics) {
            var tag = BlockTags.create(new ResourceLocation(TetraMod.MOD_ID, schematic.id));
            var appender = tag(tag);
            schematicsAppender.addTag(tag);
            for (int h = 0; h < schematic.width; h++) {
                for (int v = 0; v < schematic.height; v++) {
                    String id = String.format(MultiblockSchematicBlock.Builder.format, schematic.id, h, v);
                    appender.addOptional(new ResourceLocation("tetra", id));
                }
            }
        }
    }

    record MultiblockSchematicEntry(String id, int width, int height) {
    }
}
