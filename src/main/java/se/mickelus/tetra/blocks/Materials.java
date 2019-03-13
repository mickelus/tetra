package se.mickelus.tetra.blocks;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

public class Materials {
    public static Material forged = new ForgedMaterial();
    public static Material forgedCrate = new ForgedCrateMaterial();

    static class ForgedMaterial extends Material {
        public ForgedMaterial() {
            super(MapColor.IRON);
            setImmovableMobility();
        }
    }

    static class ForgedCrateMaterial extends Material {
        public ForgedCrateMaterial() {
            super(MapColor.IRON);
        }
    }
}
