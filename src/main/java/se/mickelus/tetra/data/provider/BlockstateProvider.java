package se.mickelus.tetra.data.provider;

import net.minecraft.block.BlockState;
import net.minecraft.data.DataGenerator;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.*;
import se.mickelus.tetra.blocks.forged.BlockForgedPlatformSlab;
import se.mickelus.tetra.blocks.forged.BlockForgedVent;
import se.mickelus.tetra.blocks.forged.extractor.CoreExtractorPipeBlock;

import static se.mickelus.tetra.TetraMod.MOD_ID;

public class BlockstateProvider extends BlockStateProvider {
    public BlockstateProvider(DataGenerator gen, String modid, ExistingFileHelper exFileHelper) {
        super(gen, modid, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
//        slabBlock(BlockForgedPlatformSlab.instance,
//                new ResourceLocation(MOD_ID, "block/forged_platform"),
//                new ResourceLocation(MOD_ID, "blocks/forged_platform_side"),
//                new ResourceLocation(MOD_ID, "blocks/forged_platform_bottom"),
//                new ResourceLocation(MOD_ID, "blocks/forged_platform_alternate"));

        setupVent();
    }

    private ConfiguredModel[] directionalBlock(BlockState state, ModelFile model) {
        Direction dir = state.get(BlockStateProperties.FACING);
        return ConfiguredModel.builder()
                .modelFile(model)
                .rotationX(dir == Direction.DOWN ? 180 : dir.getAxis().isHorizontal() ? 90 : 0)
                .rotationY(dir.getAxis().isVertical() ? 0 : (int) dir.getHorizontalAngle() % 360)
                .build();
    }

    private void setupVent() {
        VariantBlockStateBuilder builder = getVariantBuilder(BlockForgedVent.instance);

        builder.partialState()
                .with(BlockForgedVent.propRotation, 0)
                .with(BlockForgedVent.propBroken, false)
                .addModels(new ConfiguredModel(models().getExistingFile(new ResourceLocation(MOD_ID, "block/forged_vent0"))));

        builder.partialState()
                .with(BlockForgedVent.propRotation, 1)
                .with(BlockForgedVent.propBroken, false)
                .addModels(new ConfiguredModel(models().getExistingFile(new ResourceLocation(MOD_ID, "block/forged_vent1"))));

        builder.partialState()
                .with(BlockForgedVent.propRotation, 2)
                .with(BlockForgedVent.propBroken, false)
                .addModels(new ConfiguredModel(models().getExistingFile(new ResourceLocation(MOD_ID, "block/forged_vent2"))));

        builder.partialState()
                .with(BlockForgedVent.propRotation, 3)
                .with(BlockForgedVent.propBroken, false)
                .addModels(new ConfiguredModel(models().getExistingFile(new ResourceLocation(MOD_ID, "block/forged_vent3"))));

        builder.partialState()
                .with(BlockForgedVent.propRotation, 0)
                .with(BlockForgedVent.propBroken, true)
                .addModels(new ConfiguredModel(models().getExistingFile(new ResourceLocation(MOD_ID, "block/forged_vent0_broken"))));

        builder.partialState()
                .with(BlockForgedVent.propRotation, 1)
                .with(BlockForgedVent.propBroken, true)
                .addModels(new ConfiguredModel(models().getExistingFile(new ResourceLocation(MOD_ID, "block/forged_vent1_broken"))));

        builder.partialState()
                .with(BlockForgedVent.propRotation, 2)
                .with(BlockForgedVent.propBroken, true)
                .addModels(new ConfiguredModel(models().getExistingFile(new ResourceLocation(MOD_ID, "block/forged_vent2_broken"))));

        builder.partialState()
                .with(BlockForgedVent.propRotation, 3)
                .with(BlockForgedVent.propBroken, true)
                .addModels(new ConfiguredModel(models().getExistingFile(new ResourceLocation(MOD_ID, "block/forged_vent3_broken"))));
    }
}
