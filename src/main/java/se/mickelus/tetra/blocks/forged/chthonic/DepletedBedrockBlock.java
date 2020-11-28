package se.mickelus.tetra.blocks.forged.chthonic;

import net.minecraft.block.material.Material;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.TetraBlock;

public class DepletedBedrockBlock extends TetraBlock {
    public static final String unlocalizedName = "depleted_bedrock";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static DepletedBedrockBlock instance;

    public DepletedBedrockBlock() {
        super(Properties.create(Material.ROCK).hardnessAndResistance(-1.0F, 3600000.0F).noDrops());
        setRegistryName(unlocalizedName);
    }
}
