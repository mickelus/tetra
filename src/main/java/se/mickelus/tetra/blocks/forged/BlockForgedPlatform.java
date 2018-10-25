package se.mickelus.tetra.blocks.forged;

import net.minecraft.block.material.Material;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.items.TetraCreativeTabs;

public class BlockForgedPlatform extends TetraBlock {
    static final String unlocalizedName = "forged_platform";

    public static BlockForgedPlatform instance;

    public BlockForgedPlatform() {
        super(Material.IRON);

        setRegistryName(unlocalizedName);
        setUnlocalizedName(unlocalizedName);
        setCreativeTab(TetraCreativeTabs.getInstance());
        setBlockUnbreakable();

        hasItem = true;

        instance = this;
    }

}