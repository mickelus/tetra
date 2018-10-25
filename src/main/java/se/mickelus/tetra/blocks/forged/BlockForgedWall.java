package se.mickelus.tetra.blocks.forged;

import net.minecraft.block.material.Material;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.items.TetraCreativeTabs;

public class BlockForgedWall extends TetraBlock {
    static final String unlocalizedName = "forged_wall";

    public static BlockForgedWall instance;

    public BlockForgedWall() {
        super(Material.IRON);

        setRegistryName(unlocalizedName);
        setUnlocalizedName(unlocalizedName);
        setCreativeTab(TetraCreativeTabs.getInstance());
        setBlockUnbreakable();

        hasItem = true;

        instance = this;
    }

}