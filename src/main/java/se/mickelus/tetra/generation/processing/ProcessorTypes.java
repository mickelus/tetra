package se.mickelus.tetra.generation.processing;

import com.mojang.serialization.Codec;
import net.minecraft.world.gen.feature.template.IStructureProcessorType;
import se.mickelus.tetra.TetraMod;

public interface ProcessorTypes extends IStructureProcessorType {
    IStructureProcessorType forgedHammer = IStructureProcessorType.func_237139_a_(TetraMod.MOD_ID + ":forged_hammer", Codec.unit(new ForgedHammerProcessor()));
    IStructureProcessorType forgedCrate = IStructureProcessorType.func_237139_a_(TetraMod.MOD_ID + ":forged_crate", Codec.unit(new ForgedCrateProcessor()));
    IStructureProcessorType forgedContainer = IStructureProcessorType.func_237139_a_(TetraMod.MOD_ID + ":forged_container", Codec.unit(new ForgedContainerProcessor()));
    IStructureProcessorType transferUnit = IStructureProcessorType.func_237139_a_(TetraMod.MOD_ID + ":transfer_unit", Codec.unit(new TransferUnitProcessor()));
}
