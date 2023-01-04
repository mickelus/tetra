package se.mickelus.tetra.data.provider;

import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.generators.*;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import se.mickelus.tetra.blocks.forged.ForgedVentBlock;
import se.mickelus.tetra.blocks.multischematic.MultiblockSchematicBlock;

import javax.annotation.ParametersAreNonnullByDefault;

import static se.mickelus.tetra.TetraMod.MOD_ID;

@ParametersAreNonnullByDefault
public class TetraBlockStateProvider extends BlockStateProvider {
    public TetraBlockStateProvider(DataGenerator gen, String modid, ExistingFileHelper exFileHelper) {
        super(gen, modid, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
//        slabBlock(BlockForgedPlatformSlab.instance,
//                new ResourceLocation(MOD_ID, "block/forged_platform"),
//                new ResourceLocation(MOD_ID, "blocks/forged_platform_side"),
//                new ResourceLocation(MOD_ID, "blocks/forged_platform_bottom"),
//                new ResourceLocation(MOD_ID, "blocks/forged_platform_alternate"));

//        setupVent();
        setupMultiBlockSchematics();
    }

    private ConfiguredModel[] directionalBlock(BlockState state, ModelFile model) {
        Direction dir = state.getValue(BlockStateProperties.FACING);
        return ConfiguredModel.builder()
                .modelFile(model)
                .rotationX(dir == Direction.DOWN ? 180 : dir.getAxis().isHorizontal() ? 90 : 0)
                .rotationY(dir.getAxis().isVertical() ? 0 : (int) dir.toYRot() % 360)
                .build();
    }

    private void setupVent() {
        VariantBlockStateBuilder builder = getVariantBuilder(ForgedVentBlock.instance);

        builder.partialState()
                .with(ForgedVentBlock.propRotation, 0)
                .with(ForgedVentBlock.propBroken, false)
                .addModels(new ConfiguredModel(models().getExistingFile(new ResourceLocation(MOD_ID, "block/forged_vent0"))));

        builder.partialState()
                .with(ForgedVentBlock.propRotation, 1)
                .with(ForgedVentBlock.propBroken, false)
                .addModels(new ConfiguredModel(models().getExistingFile(new ResourceLocation(MOD_ID, "block/forged_vent1"))));

        builder.partialState()
                .with(ForgedVentBlock.propRotation, 2)
                .with(ForgedVentBlock.propBroken, false)
                .addModels(new ConfiguredModel(models().getExistingFile(new ResourceLocation(MOD_ID, "block/forged_vent2"))));

        builder.partialState()
                .with(ForgedVentBlock.propRotation, 3)
                .with(ForgedVentBlock.propBroken, false)
                .addModels(new ConfiguredModel(models().getExistingFile(new ResourceLocation(MOD_ID, "block/forged_vent3"))));

        builder.partialState()
                .with(ForgedVentBlock.propRotation, 0)
                .with(ForgedVentBlock.propBroken, true)
                .addModels(new ConfiguredModel(models().getExistingFile(new ResourceLocation(MOD_ID, "block/forged_vent0_broken"))));

        builder.partialState()
                .with(ForgedVentBlock.propRotation, 1)
                .with(ForgedVentBlock.propBroken, true)
                .addModels(new ConfiguredModel(models().getExistingFile(new ResourceLocation(MOD_ID, "block/forged_vent1_broken"))));

        builder.partialState()
                .with(ForgedVentBlock.propRotation, 2)
                .with(ForgedVentBlock.propBroken, true)
                .addModels(new ConfiguredModel(models().getExistingFile(new ResourceLocation(MOD_ID, "block/forged_vent2_broken"))));

        builder.partialState()
                .with(ForgedVentBlock.propRotation, 3)
                .with(ForgedVentBlock.propBroken, true)
                .addModels(new ConfiguredModel(models().getExistingFile(new ResourceLocation(MOD_ID, "block/forged_vent3_broken"))));
    }

    private void setupMultiBlockSchematics() {
        setupMultiBlockSchematics("stonecutter", 3, 2, true);
        setupMultiBlockSchematics("earthpiercer", 2, 2, true);
        setupMultiBlockSchematics("extractor", 3, 3, true);
    }

    private void setupMultiBlockSchematics(String identifier, int width, int height, boolean ruinable) {
        for (int h = 0; h < width; h++) {
            for (int v = 0; v < height; v++) {
                setupMultiBlockSchematic(identifier, "blocks/forged_schematic/", h, v);
                if (ruinable) {
                    setupMultiBlockSchematic(identifier + "_ruined", "blocks/forged_schematic/", h, v);
                }
            }
        }
    }

    private void setupMultiBlockSchematic(String identifier, String modelPrefix, int h, int v) {
        String id = String.format(MultiblockSchematicBlock.Builder.format, identifier, h, v);
        ResourceLocation rl = new ResourceLocation("tetra", id);
        ResourceLocation front = new ResourceLocation("tetra", modelPrefix + id);
        Block block = ForgeRegistries.BLOCKS.getValue(rl);
        ModelFile model = getSchematicModel(id, front,
                new ResourceLocation("tetra", modelPrefix + "side"),
                new ResourceLocation("tetra", modelPrefix + "back"));
        horizontalBlock(block, model, 90);

        simpleBlockItem(block, model);
    }

    private ModelFile getSchematicModel(String name, ResourceLocation front, ResourceLocation side, ResourceLocation back) {
        return models().withExistingParent(name, "tetra:" + ModelProvider.BLOCK_FOLDER + "/multi_schematic_base")
                .texture("particle", front)
                .texture("side", side)
                .texture("front", front)
                .texture("back", back);
    }
}
