package se.mickelus.tetra.blocks.holo;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.RegistryObject;
import se.mickelus.mutil.util.RotationHelper;
import se.mickelus.tetra.TetraToolActions;
import se.mickelus.tetra.blocks.TetraWaterloggedBlock;
import se.mickelus.tetra.items.modular.ModularItem;

import javax.annotation.Nullable;

public class HolosphereBlock extends TetraWaterloggedBlock implements EntityBlock {
    public static final String identifier = "holosphere";
    private static final VoxelShape shape = Block.box(5.5, 0, 5.5, 10.5, 5, 10.5);
    public static RegistryObject<HolosphereBlock> instance;

    public HolosphereBlock() {
        super(BlockBehaviour.Properties.of(Material.HEAVY_METAL, MaterialColor.METAL)
                .strength(1.0F, 1.0F)
                .sound(SoundType.DEEPSLATE));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return Block.box(5.5, 0, 5.5, 10.5, 5, 10.5);
    }

    @Override
    public void clientInit() {
        BlockEntityRenderers.register(HolosphereBlockEntity.type.get(), HolosphereEntityRenderer::new);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
        return new HolosphereBlockEntity(p_153215_, p_153216_);
    }

    @Override
    public InteractionResult use(BlockState blockState, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (!world.isClientSide() && world.getBlockEntity(pos) instanceof HolosphereBlockEntity entity) {
            if (itemStack.getItem() instanceof ModularItem item) {
                int level = item.getToolLevel(itemStack, TetraToolActions.hammer);
                if (level > 0) {
                    float angle = (float) RotationHelper.getHorizontalAngle(Vec3.atBottomCenterOf(pos), player.position());

                    entity.use(level, item.getToolEfficiency(itemStack, TetraToolActions.hammer), (float) angle);
                    return InteractionResult.SUCCESS;
                }
            }
        }

        if (world.isClientSide()) {

        }

        return InteractionResult.SUCCESS;
    }
}
