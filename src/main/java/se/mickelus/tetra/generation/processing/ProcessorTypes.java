package se.mickelus.tetra.generation.processing;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import se.mickelus.tetra.TetraMod;

public interface ProcessorTypes extends StructureProcessorType {
    StructureProcessorType forgedHammer = StructureProcessorType.register(TetraMod.MOD_ID + ":forged_hammer", Codec.unit(new ForgedHammerProcessor()));
    StructureProcessorType forgedCrate = StructureProcessorType.register(TetraMod.MOD_ID + ":forged_crate", Codec.unit(new ForgedCrateProcessor()));
    StructureProcessorType forgedContainer = StructureProcessorType.register(TetraMod.MOD_ID + ":forged_container", Codec.unit(new ForgedContainerProcessor()));
    StructureProcessorType transferUnit = StructureProcessorType.register(TetraMod.MOD_ID + ":transfer_unit", Codec.unit(new TransferUnitProcessor()));
}
