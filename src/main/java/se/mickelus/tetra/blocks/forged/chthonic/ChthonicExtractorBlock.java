package se.mickelus.tetra.blocks.forged.chthonic;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
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
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.mutil.util.TileEntityOptional;
import se.mickelus.tetra.FeatureFlag;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.TetraItemAbilities;
import se.mickelus.tetra.Tooltips;
import se.mickelus.tetra.blocks.PropertyMatcher;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.blocks.forged.ForgedBlockCommon;
import se.mickelus.tetra.blocks.salvage.BlockInteraction;
import se.mickelus.tetra.blocks.salvage.IInteractiveBlock;
import se.mickelus.tetra.properties.IToolProvider;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
public class ChthonicExtractorBlock extends TetraBlock implements IInteractiveBlock, EntityBlock {
    public static final String identifier = "chthonic_extractor";
    public static final String usedIdentifier = "chthonic_extractor_used";
    public static final String description = "block.tetra.chthonic_extractor.description";
    public static final String extendedDescription = "block.tetra.chthonic_extractor.description_extended";
    public static final int maxDamage = 1024;
    protected static final VoxelShape shape = Shapes.or(
            Block.box(7.0D, 0.0D, 7.0D, 9.0D, 16.0D, 9.0D),
            Block.box(6.0D, 15.0D, 6.0D, 10.0D, 16.0D, 10.0D));
    static final BlockInteraction[] interactions = new BlockInteraction[] {
            new BlockInteraction(TetraItemAbilities.hammer, 4, Direction.UP, 0, 4, 0, 4,
                    PropertyMatcher.any, (world, pos, blockState, player, hand, hitFace) -> hit(world, pos, player, hand)),
            new BlockInteraction(TetraItemAbilities.hammer, 5, Direction.UP, 0, 4, 0, 4,
                    PropertyMatcher.any, (world, pos, blockState, player, hand, hitFace) -> hit(world, pos, player, hand)),
            new BlockInteraction(TetraItemAbilities.hammer, 6, Direction.UP, 0, 4, 0, 4,
                    PropertyMatcher.any, (world, pos, blockState, player, hand, hitFace) -> hit(world, pos, player, hand)),
            new BlockInteraction(TetraItemAbilities.hammer, 7, Direction.UP, 0, 4, 0, 4,
                    PropertyMatcher.any, (world, pos, blockState, player, hand, hitFace) -> hit(world, pos, player, hand))
    };
    @ObjectHolder(registryName = "block", value = TetraMod.MOD_ID + ":" + identifier)
    public static ChthonicExtractorBlock instance;
    @ObjectHolder(registryName = "item", value = TetraMod.MOD_ID + ":" + identifier)
    public static Item item;
    @ObjectHolder(registryName = "item", value = TetraMod.MOD_ID + ":" + usedIdentifier)
    public static Item usedItem;

    public ChthonicExtractorBlock() {
        super(Block.Properties.of()
                .mapColor(MapColor.COLOR_GRAY)
                .sound(SoundType.NETHERITE_BLOCK)
                .strength(2.5F, 2400.0F));
    }

    private static boolean hit(Level world, BlockPos pos, @Nullable Player playerEntity, InteractionHand hand) {
        if (FeatureFlag.isEnabled(FeatureFlag.bedrockExtraction)) {
            int amount = Optional.ofNullable(playerEntity)
                    .map(player -> player.getItemInHand(hand))
                    .filter(itemStack -> itemStack.getItem() instanceof IToolProvider)
                    .map(itemStack -> ((IToolProvider) itemStack.getItem()).getToolEfficiency(itemStack, TetraItemAbilities.hammer))
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

    public static DeferredHolder<Item, BlockItem> registerItems(DeferredRegister<Item> registry) {
        registry.register(usedIdentifier, () -> {
            usedItem = new BlockItem(instance, new Item.Properties().durability(maxDamage));
            return (BlockItem) usedItem;
        });
        return registry.register(identifier, () -> {
            item = new BlockItem(instance, new Item.Properties().stacksTo(64));
            return (BlockItem) item;
        });
    }

    @Override
    public void appendHoverText(final ItemStack stack, final Item.TooltipContext context, final List<Component> tooltip, final TooltipFlag advanced) {
        tooltip.add(Component.translatable(description).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal(" "));

        if (Screen.hasShiftDown()) {
            tooltip.add(Tooltips.expanded);
            tooltip.add(Component.literal(" "));
            tooltip.add(ForgedBlockCommon.locationTooltip);
            tooltip.add(Component.literal(" "));
            tooltip.add(Component.translatable(extendedDescription).withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Tooltips.expand);
        }
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        TileEntityOptional.from(world, pos, ChthonicExtractorTile.class)
                .ifPresent(tile -> tile.setDamage(stack.getDamageValue()));
    }

    public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        TileEntityOptional.from(world, pos, ChthonicExtractorTile.class)
                .ifPresent(tile -> {
                    ItemStack itemStack = getItemStack(tile);

                    ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, itemStack);
                    itemEntity.setDefaultPickUpDelay();
                    world.addFreshEntity(itemEntity);
                });

        return super.playerWillDestroy(world, pos, state, player);
    }

    @Override
    public List<ItemStack> getDrops(BlockState blockState, LootParams.Builder lootParams) {
        if (lootParams.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof ChthonicExtractorTile tile) {
            lootParams = lootParams.withDynamicDrop(ResourceLocation.parse("tetra:cthtonic_drop"),
                    consumer -> consumer.accept(getItemStack(tile)));
        }

        return super.getDrops(blockState, lootParams);
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
    public BlockInteraction[] getPotentialInteractions(Level world, BlockPos pos, BlockState blockState, Direction face, Collection<ItemAbility> tools) {
        int tier = getTier(world, pos);

        // todo: this could be less hacky
        if (FeatureFlag.isEnabled(FeatureFlag.bedrockExtraction) && tier >= 0 && face == Direction.UP) {
            return new BlockInteraction[] { interactions[Math.min(tier, interactions.length - 1)] };
        }

        return new BlockInteraction[0];
    }

    private InteractionResult useInternal(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return BlockInteraction.attemptInteraction(world, state, pos, player, hand, hit);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        return switch (useInternal(state, world, pos, player, hand, hit)) {
            case SUCCESS, CONSUME -> ItemInteractionResult.sidedSuccess(world.isClientSide);
            case CONSUME_PARTIAL -> ItemInteractionResult.CONSUME_PARTIAL;
            case FAIL -> ItemInteractionResult.FAIL;
            default -> ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        return useInternal(state, world, pos, player, InteractionHand.MAIN_HAND, hit);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
        return new ChthonicExtractorTile(p_153215_, p_153216_);
    }
}
