package se.mickelus.tetra.blocks.forged.extractor;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.blocks.forged.ForgedBlockCommon;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class SeepingBedrockBlock extends TetraBlock {
    public static final IntegerProperty activeProp = IntegerProperty.create("active", 0, 1);

    public static final String identifier = "seeping_bedrock";

    @ObjectHolder(TetraMod.MOD_ID + ":" + identifier)
    public static SeepingBedrockBlock instance;

    public SeepingBedrockBlock() {
        super(BlockBehaviour.Properties.of(Material.STONE).strength(-1.0F, 3600000.0F).noDrops());

        registerDefaultState(defaultBlockState().setValue(activeProp, 1));
    }

    public static boolean isActive(Level world, BlockPos pos) {
        return isActive(world.getBlockState(pos));
    }

    public static boolean isActive(BlockState blockState) {
        return instance.equals(blockState.getBlock()) && blockState.getValue(activeProp) > 0;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(ForgedBlockCommon.locationTooltip);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(activeProp);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(activeProp, context.getPlayer().isCrouching() ? 0 : 1);
    }
}
