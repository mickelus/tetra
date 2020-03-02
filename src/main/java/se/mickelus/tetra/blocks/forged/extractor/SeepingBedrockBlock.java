package se.mickelus.tetra.blocks.forged.extractor;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.blocks.forged.ForgedBlockCommon;

import javax.annotation.Nullable;
import java.util.List;

public class SeepingBedrockBlock extends TetraBlock {
    public static final IntegerProperty activeProp = IntegerProperty.create("active", 0, 1);

    public static final String unlocalizedName = "seeping_bedrock";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static SeepingBedrockBlock instance;

    public SeepingBedrockBlock() {
        super(Block.Properties.create(Material.ROCK).hardnessAndResistance(-1.0F, 3600000.0F).noDrops());
        setRegistryName(unlocalizedName);

        hasItem = true;

        setDefaultState(getDefaultState().with(activeProp, 1));
    }

    public static boolean isActive(World world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        return instance.equals(blockState.getBlock()) && blockState.get(activeProp) > 0;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        tooltip.add(ForgedBlockCommon.locationTooltip);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(activeProp);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return getDefaultState().with(activeProp, context.getPlayer().isSneaking() ? 0 : 1);
    }
}
