package se.mickelus.tetra.blocks.geode;

import java.util.Arrays;
import java.util.Random;
import javax.annotation.Nullable;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.state.IntegerProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.data.DataHandler;
import se.mickelus.tetra.items.TetraItemGroup;
import se.mickelus.tetra.network.PacketHandler;

public class BlockGeode extends TetraBlock {

    static final String unlocalizedName = "block_geode";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static BlockGeode instance;

    // hacky, but avoids some log warnings
    public static PropertyInteger variantProp = PropertyInteger.create("variant", 0,
            (int) Arrays.stream(DataHandler.instance.getData("geode/variants", GeodeVariant[].class)).count() - 1);

    public GeodeVariant[] variants = new GeodeVariant[0];

    private GeodeVariant fallbackVariant = new GeodeVariant();

    public BlockGeode() {
        super(Material.ROCK);
        setSoundType(SoundType.STONE);
        setHarvestLevel("pickaxe", 0);


        setUnlocalizedName(unlocalizedName);
        setRegistryName(unlocalizedName);

        setCreativeTab(TetraItemGroup.getInstance());
        this.setDefaultState(this.blockState.getBaseState().withProperty(variantProp, 0));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, variantProp);
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(variantProp, meta);
    }

    @Override
    public int getMetaFromState(BlockState blockState) {
        return blockState.getValue(variantProp);
    }

    private GeodeVariant getVariant(BlockState state) {
        int index = state.getValue(variantProp);
        if (index < variants.length) {
            return variants[index];
        }

        return fallbackVariant;
    }

    @Override
    public ItemStack getPickBlock(BlockState blockState, RayTraceResult target, World world, BlockPos pos, PlayerEntity player) {
        return getSilkTouchDrop(blockState);
    }

    @Override
    public Item getItemDropped(BlockState state, Random rand, int fortune) {
        return ItemGeode.instance;
    }

    @Override
    public int damageDropped(BlockState blockState) {
        return getVariant(blockState).dropMeta;
    }

    @Override
    protected ItemStack getSilkTouchDrop(BlockState state) {
        GeodeVariant variant = getVariant(state);
        return new ItemStack(variant.block, 1, variant.blockMeta);
    }

    @Override
    public float getBlockHardness(BlockState blockState, World worldIn, BlockPos pos) {
        return getVariant(blockState).hardness;
    }

    @Override
    public float getExplosionResistance(World world, BlockPos pos, @Nullable Entity exploder, Explosion explosion) {
        return getVariant(world.getBlockState(pos)).resistance;
    }

    @Override
    public void init(PacketHandler packetHandler) {
        if (ConfigHandler.geodeGenerate) {
            GameRegistry.registerWorldGenerator(new GeodeGenerator(), 10);
        }

        variants = Arrays.stream(DataHandler.instance.getData("geode/variants", GeodeVariant[].class))
                .filter(variant -> variant.block != null)
                .toArray(GeodeVariant[]::new);
    }
}
