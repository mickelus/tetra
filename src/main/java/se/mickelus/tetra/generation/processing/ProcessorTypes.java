package se.mickelus.tetra.generation.processing;

import net.minecraft.world.gen.feature.template.IStructureProcessorType;
import se.mickelus.tetra.TetraMod;

public interface ProcessorTypes extends IStructureProcessorType {
    IStructureProcessorType forgedHammer = IStructureProcessorType.register(TetraMod.MOD_ID + ":forged_hammer", d -> new ForgedHammerProcessor());
    IStructureProcessorType forgedCrate = IStructureProcessorType.register(TetraMod.MOD_ID + ":forged_crate", d -> new ForgedCrateProcessor());
    IStructureProcessorType forgedContainer = IStructureProcessorType.register(TetraMod.MOD_ID + ":forged_container", d -> new ForgedContainerProcessor());
    IStructureProcessorType transferUnit = IStructureProcessorType.register(TetraMod.MOD_ID + ":transfer_unit", d -> new TransferUnitProcessor());
}
