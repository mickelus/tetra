package se.mickelus.tetra.blocks.forged.chthonic;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.TetraToolActions;
import se.mickelus.tetra.Tooltips;
import se.mickelus.tetra.blocks.PropertyMatcher;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.blocks.forged.ForgedBlockCommon;
import se.mickelus.tetra.blocks.salvage.BlockInteraction;
import se.mickelus.tetra.blocks.salvage.IInteractiveBlock;
import se.mickelus.tetra.items.TetraItemGroup;
import se.mickelus.tetra.properties.IToolProvider;
import se.mickelus.tetra.util.TileEntityOptional;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
@ParametersAreNonnullByDefault
public class ChthonicExtractorBlock extends TetraBlock implements IInteractiveBlock, EntityBlock {
    public static final String unlocalizedName = "chthonic_extractor";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ChthonicExtractorBlock instance;

    public static final String description = "block.tetra.chthonic_extractor.description";
    public static final String extendedDescription = "block.tetra.chthonic_extractor.description_extended";

    static final BlockInteraction[] interactions = new BlockInteraction[] {
            new BlockInteraction(TetraToolActions.hammer, 4, Direction.UP, 0, 4, 0, 4,
                    PropertyMatcher.any, (world, pos, blockState, player, hand, hitFace) -> hit(world, pos, player, hand)),
            new BlockInteraction(TetraToolActions.hammer, 5, Direction.UP, 0, 4, 0, 4,
                    PropertyMatcher.any, (world, pos, blockState, player, hand, hitFace) -> hit(world, pos, player, hand)),
            new BlockInteraction(TetraToolActions.hammer, 6, Direction.UP, 0, 4, 0, 4,
                    PropertyMatcher.any, (world, pos, blockState, player, hand, hitFace) -> hit(world, pos, player, hand)),
            new BlockInteraction(TetraToolActions.hammer, 7, Direction.UP, 0, 4, 0, 4,
                    PropertyMatcher.any, (world, pos, blockState, player, hand, hitFace) -> hit(world, pos, player, hand))
    };

    protected static final VoxelShape shape = Shapes.or(
            Block.box(7.0D, 0.0D, 7.0D, 9.0D, 16.0D, 9.0D),
            Block.box(6.0D, 15.0D, 6.0D, 10.0D, 16.0D, 10.0D));

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static Item item;


    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName + "_used")
    public static Item usedItem;

    public static final int maxDamage = 1024;

    public ChthonicExtractorBlock() {
        super(Block.Properties.of(ForgedBlockCommon.forgedMaterialNotSolid, MaterialColor.COLOR_GRAY)
                .sound(SoundType.NETHERITE_BLOCK)
                .strength(4F, 2400.0F));

        setRegistryName(unlocalizedName);

        hasItem = true;
    }

    @Override
    public void clientInit() {
        EntityRenderers.register(ExtractorProjectileEntity.type, ExtractorProjectileRenderer::new);
    }

    @Override
    public void registerItem(IForgeRegistry<Item> registry) {
        Item usedItem = new BlockItem(this, new Item.Properties()
                .durability(maxDamage))
                .setRegistryName(getRegistryName() + "_used");
        registry.register(usedItem);

        Item item = new BlockItem(this, new Item.Properties()
                .tab(TetraItemGroup.instance)
                .stacksTo(64))
                .setRegistryName(getRegistryName());
        registry.register(item);
    }

    @Override
    public void appendHoverText(final ItemStack stack, @Nullable final BlockGetter world, final List<Component> tooltip, final TooltipFlag advanced) {
        tooltip.add(new TranslatableComponent(description).withStyle(ChatFormatting.GRAY));
        tooltip.add(new TextComponent(" "));

        if (Screen.hasShiftDown()) {
            tooltip.add(Tooltips.expanded);
            tooltip.add(new TextComponent(" "));
            tooltip.add(ForgedBlockCommon.locationTooltip);
            tooltip.add(new TextComponent(" "));
            tooltip.add(new TranslatableComponent(extendedDescription).withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Tooltips.expand);
        }
    }

    private static boolean hit(Level world, BlockPos pos, @Nullable Player playerEntity, InteractionHand hand) {
        if (ConfigHandler.enableExtractor.get()) {
            int amount = Optional.ofNullable(playerEntity)
                    .map(player -> player.getItemInHand(hand))
                    .filter(itemStack -> itemStack.getItem() instanceof IToolProvider)
                    .map(itemStack -> ((IToolProvider) itemStack.getItem()).getToolEfficiency(itemStack, TetraToolActions.hammer))
                    .map(Math::round)
                    .orElse(4);

            TileEntityOptional.from(world, pos, ChthonicExtractorTile.class).ifPresent(tile -> tile.damage(amount));
            FracturedBedrockBlock.pierce(world, pos.below(), amount);
            world.playSound(playerEntity, pos, SoundEvents.NETHERITE_BLOCK_HIT, SoundSource.PLAYERS, 0.8f, 0.5f);
            return true;
        }
        return false;
    }

    private static int getTier(Level world, BlockPos pos) {
        return TileEntityOptional.from(world, pos.below(), FracturedBedrockTile.class)
                .map(FracturedBedrockTile::getProjectedTier)
                .orElseGet(() -> FracturedBedrockBlock.canPierce(world, pos.below()) ? 0 : -1);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        TileEntityOptional.from(world, pos, ChthonicExtractorTile.class)
                .ifPresent(tile -> tile.setDamage(stack.getDamageValue()));
    }

    public void playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        TileEntityOptional.from(world, pos, ChthonicExtractorTile.class)
                .ifPresent(tile -> {
                    ItemStack itemStack = getItemStack(tile);

                    ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, itemStack);
                    itemEntity.setDefaultPickUpDelay();
                    world.addFreshEntity(itemEntity);
                });

        super.playerWillDestroy(world, pos, state, player);
    }

    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        BlockEntity tile = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (tile instanceof ChthonicExtractorTile) {
            builder = builder.withDynamicDrop(new ResourceLocation("tetra:cthtonic_drop"),
                    (context, stackConsumer) -> stackConsumer.accept(getItemStack((ChthonicExtractorTile) tile)));
        }

        return super.getDrops(state, builder);
    }

    private ItemStack getItemStack(ChthonicExtractorTile tile) {
        if (tile.getDamage() > 0) {
            ItemStack itemStack = new ItemStack(usedItem);
            itemStack.setDamageValue(tile.getDamage());
            return itemStack;
        }

        return new ItemStack(item);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return shape;
    }

    @Override
    public BlockInteraction[] getPotentialInteractions(Level world, BlockPos pos, BlockState blockState, Direction face, Collection<ToolAction> tools) {
        int tier = getTier(world, pos);

        // todo: this could be less hacky
        if (ConfigHandler.enableExtractor.get() && tier >= 0 && face == Direction.UP) {
            return new BlockInteraction[] { interactions[Math.min(tier, interactions.length - 1)] };
        }

        return new BlockInteraction[0];
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return BlockInteraction.attemptInteraction(world, state, pos, player, hand, hit);
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
        return new ChthonicExtractorTile(p_153215_, p_153216_);
    }
}
