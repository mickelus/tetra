package se.mickelus.tetra.blocks.forged.chthonic;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.blocks.forged.extractor.SeepingBedrockBlock;
import se.mickelus.tetra.util.TileEntityOptional;

import javax.annotation.Nullable;

public class FracturedBedrockBlock extends TetraBlock {
    public static final String unlocalizedName = "fractured_bedrock";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static FracturedBedrockBlock instance;

    public FracturedBedrockBlock() {
        super(Block.Properties.create(Material.ROCK).hardnessAndResistance(-1.0F, 3600000.0F).noDrops());
        setRegistryName(unlocalizedName);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new FracturedBedrockTile();
    }

    public static boolean canPierce(World world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        return Blocks.BEDROCK.equals(blockState.getBlock())
                || (SeepingBedrockBlock.instance.equals(blockState.getBlock()) && !SeepingBedrockBlock.isActive(blockState));
    }

    public static void pierce(World world, BlockPos pos, int amount) {
        FracturedBedrockTile tile = TileEntityOptional.from(world, pos, FracturedBedrockTile.class).orElse(null);

        if (tile == null && canPierce(world, pos)) {
            BlockState blockState = world.getBlockState(pos);
            world.setBlockState(pos, instance.getDefaultState(), 2);
            tile = TileEntityOptional.from(world, pos, FracturedBedrockTile.class).orElse(null);

            if (!world.isRemote) {
                tile.updateLuck(SeepingBedrockBlock.instance.equals(blockState.getBlock()));
            }
        }

        if (tile != null) {
            tile.activate(amount);
        }
    }
}
