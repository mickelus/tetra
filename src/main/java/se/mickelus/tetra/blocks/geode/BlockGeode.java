package se.mickelus.tetra.blocks.geode;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.registry.GameRegistry;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.items.TetraCreativeTabs;
import se.mickelus.tetra.network.PacketPipeline;

public class BlockGeode extends TetraBlock {

    static final String unlocalizedName = "block_geode";

    public static BlockGeode instance;

    public BlockGeode() {
        super(Material.ROCK);

        setHardness(1.5F);
        setResistance(10.0F);
        setSoundType(SoundType.STONE);

        this.setDefaultState(this.blockState.getBaseState());

        setUnlocalizedName(unlocalizedName);
        setRegistryName(unlocalizedName);

        setCreativeTab(TetraCreativeTabs.getInstance());

        instance = this;

        this.setDefaultState(this.blockState.getBaseState());
    }

    @Override
    public int getExpDrop(final IBlockState state, final IBlockAccess world, final BlockPos pos, final int fortune) {

        return super.getExpDrop(state, world, pos, fortune);
    }

    @Nullable
    @Override
    public Item getItemDropped(final IBlockState state, final Random rand, final int fortune) {
        return ItemGeode.instance;
    }

    @Override
    public void init(PacketPipeline packetPipeline) {
        if (ConfigHandler.geode_generate) {
            GameRegistry.registerWorldGenerator(new GeodeGenerator(), 10);
        }
    }

    @Override
    public String getLocalizedName() {
        return I18n.format("tile.stone.stone.name");
    }
}
