package se.mickelus.tetra.blocks.forged.chthonic;

import se.mickelus.tetra.compat.forge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.TetraBlock;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class DepletedBedrockBlock extends TetraBlock {
    public static final String identifier = "depleted_bedrock";

    @ObjectHolder(registryName = "block", value = TetraMod.MOD_ID + ":" + identifier)
    public static DepletedBedrockBlock instance;

    public DepletedBedrockBlock() {
        super(Properties.of().strength(-1.0F, 3600000.0F).noLootTable());
    }
}
