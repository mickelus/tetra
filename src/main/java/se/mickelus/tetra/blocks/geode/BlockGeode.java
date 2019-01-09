package se.mickelus.tetra.blocks.geode;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.items.TetraCreativeTabs;
import se.mickelus.tetra.network.PacketHandler;

public class BlockGeode extends TetraBlock {

    static final String unlocalizedName = "block_geode";

    public static BlockGeode instance;

    public BlockGeode() {
        super(Material.ROCK);

        setHardness(1.5F);
        setResistance(10.0F);
        setSoundType(SoundType.STONE);
        setHarvestLevel("pickaxe", 0);

        this.setDefaultState(this.blockState.getBaseState());

        setUnlocalizedName("stone.stone.name");
        setRegistryName(unlocalizedName);

        setCreativeTab(TetraCreativeTabs.getInstance());

        instance = this;
    }

    @Override
    public int getExpDrop(final IBlockState state, final IBlockAccess world, final BlockPos pos, final int fortune) {
        return super.getExpDrop(state, world, pos, fortune);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(Blocks.STONE);
    }

    @Nullable
    @Override
    public Item getItemDropped(final IBlockState state, final Random rand, final int fortune) {
        return ItemGeode.instance;
    }

    @Override
    protected ItemStack getSilkTouchDrop(IBlockState state) {
        return new ItemStack(Blocks.STONE);
    }

    @Override
    public void init(PacketHandler packetHandler) {
        if (ConfigHandler.geode_generate) {
            GameRegistry.registerWorldGenerator(new GeodeGenerator(), 10);
        }
    }
}
