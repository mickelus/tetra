package se.mickelus.tetra.blocks.forged.chthonic;

import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.TetraBlock;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
public class DepletedBedrockBlock extends TetraBlock {
    public static final String unlocalizedName = "depleted_bedrock";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static DepletedBedrockBlock instance;

    public DepletedBedrockBlock() {
        super(Properties.of(Material.STONE).strength(-1.0F, 3600000.0F).noDrops());
        setRegistryName(unlocalizedName);
    }
}
