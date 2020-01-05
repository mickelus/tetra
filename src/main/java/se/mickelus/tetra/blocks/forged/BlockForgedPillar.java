package se.mickelus.tetra.blocks.forged;

import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.block.SoundType;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.ToolTypes;
import se.mickelus.tetra.blocks.ITetraBlock;

import javax.annotation.Nullable;
import java.util.List;

public class BlockForgedPillar extends RotatedPillarBlock implements ITetraBlock {
    private static final String unlocalizedName = "forged_pillar";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static BlockForgedPillar instance;

    public BlockForgedPillar() {
        super(ForgedBlockCommon.properties);

        setRegistryName(unlocalizedName);
    }

    @Override
    public void addInformation(ItemStack itemStack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced) {
        tooltip.add(ForgedBlockCommon.hintTooltip);
    }

    @Override
    public boolean hasItem() {
        return true;
    }
}
