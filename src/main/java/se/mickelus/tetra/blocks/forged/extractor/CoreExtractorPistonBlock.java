package se.mickelus.tetra.blocks.forged.extractor;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.blocks.forged.ForgedBlockCommon;
import se.mickelus.tetra.network.PacketHandler;
import se.mickelus.tetra.util.TileEntityOptional;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class CoreExtractorPistonBlock extends TetraBlock {
    public static final String unlocalizedName = "extractor_piston";
    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static CoreExtractorPistonBlock instance;

    public static final net.minecraft.state.BooleanProperty hackProp = BooleanProperty.create("hack");

    public static final VoxelShape boundingBox = makeCuboidShape(5, 0, 5, 11, 16, 11);

    public CoreExtractorPistonBlock() {
        super(ForgedBlockCommon.properties);

        setRegistryName(unlocalizedName);

        hasItem = true;
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        TileEntityOptional.from(worldIn, pos, CoreExtractorPistonTile.class).ifPresent(CoreExtractorPistonTile::activate);
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void clientInit() {
        ClientRegistry.bindTileEntitySpecialRenderer(CoreExtractorPistonTile.class, new CoreExtractorPistonTESR());
    }

    @Override
    public void init(PacketHandler packetHandler) {
        super.init(packetHandler);

        PacketHandler.instance.registerPacket(CoreExtractorPistonUpdatePacket.class, CoreExtractorPistonUpdatePacket::new);
    }

    @Override
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        TileEntityOptional.from(worldIn, pos, CoreExtractorPistonTile.class)
                .ifPresent(te -> {
                    if (te.isActive()) {
                        float random = rand.nextFloat();

                        if (random < 0.6f) {
                            worldIn.addParticle(ParticleTypes.SMOKE,
                                    pos.getX() + 0.4 + rand.nextGaussian() * 0.2,
                                    pos.getY() + rand.nextGaussian(),
                                    pos.getZ() + 0.4 + rand.nextGaussian() * 0.2,
                                    0.0D, 0.0D, 0.0D);
                        }
                    }
                });
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        tooltip.add(ForgedBlockCommon.hintTooltip);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return boundingBox;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public boolean causesSuffocation(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return false;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new CoreExtractorPistonTile();
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(hackProp);
    }
}
