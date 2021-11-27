package se.mickelus.tetra.blocks.forged;

import net.minecraft.block.SlabBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.ITetraBlock;

import javax.annotation.Nullable;
import java.util.List;


public class BlockForgedPlatformSlab extends SlabBlock implements ITetraBlock {
    static final String unlocalizedName = "forged_platform_slab";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static BlockForgedPlatformSlab instance;

    public BlockForgedPlatformSlab() {
        super(ForgedBlockCommon.propertiesSolid);

        setRegistryName(unlocalizedName);
    }

    @Override
    public void addInformation(ItemStack itemStack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced) {
        tooltip.add(ForgedBlockCommon.locationTooltip);
    }

    @Override
    public boolean hasItem() {
        return true;
    }
}
