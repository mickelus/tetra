package se.mickelus.tetra.blocks.forged;

import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.workbench.AbstractWorkbenchBlock;

import javax.annotation.Nullable;
import java.util.List;

public class ForgedWorkbenchBlock extends AbstractWorkbenchBlock {
    public static final String unlocalizedName = "forged_workbench";
    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static AbstractWorkbenchBlock instance;

    private static final VoxelShape shape = VoxelShapes.or(
            makeCuboidShape(3, 0, 1, 13, 2, 15),
            makeCuboidShape(4, 2, 2, 12, 9, 14),
            makeCuboidShape(2, 9, 0, 14, 16, 16));

    public ForgedWorkbenchBlock() {
        super(ForgedBlockCommon.properties);

        setRegistryName(unlocalizedName);

        hasItem = true;
    }

    @Override
    public void addInformation(ItemStack itemStack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced) {
        tooltip.add(ForgedBlockCommon.hintTooltip);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return shape;
    }
}
