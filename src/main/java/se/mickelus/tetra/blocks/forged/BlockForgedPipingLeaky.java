package se.mickelus.tetra.blocks.forged;

import net.minecraft.block.material.Material;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.items.TetraCreativeTabs;

public class BlockForgedPipingLeaky extends TetraBlock {
    static final String unlocalizedName = "forged_piping_leaky";

    public static BlockForgedPipingLeaky instance;

    public BlockForgedPipingLeaky() {
        super(Material.IRON);

        setRegistryName(unlocalizedName);
        setUnlocalizedName(unlocalizedName);
        setCreativeTab(TetraCreativeTabs.getInstance());
        setBlockUnbreakable();

        instance = this;
    }

}