package se.mickelus.tetra.blocks.forged.chthonic;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.ToolTypes;
import se.mickelus.tetra.Tooltips;
import se.mickelus.tetra.blocks.PropertyMatcher;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.blocks.forged.ForgedBlockCommon;
import se.mickelus.tetra.blocks.salvage.BlockInteraction;
import se.mickelus.tetra.blocks.salvage.IInteractiveBlock;
import se.mickelus.tetra.items.TetraItemGroup;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.properties.IToolProvider;
import se.mickelus.tetra.util.CastOptional;
import se.mickelus.tetra.util.TileEntityOptional;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class ChthonicExtractorBlock extends TetraBlock implements IInteractiveBlock {
    public static final String unlocalizedName = "chthonic_extractor";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ChthonicExtractorBlock instance;

    public static final String description = "block.tetra.chthonic_extractor.description";
    public static final String extendedDescription = "block.tetra.chthonic_extractor.description_extended";

    static final BlockInteraction[] interactions = new BlockInteraction[] {
            new BlockInteraction(ToolTypes.hammer, 4, Direction.UP, 0, 4, 0, 4,
                    PropertyMatcher.any, (world, pos, blockState, player, hand, hitFace) -> hit(world, pos, player, hand)),
            new BlockInteraction(ToolTypes.hammer, 5, Direction.UP, 0, 4, 0, 4,
                    PropertyMatcher.any, (world, pos, blockState, player, hand, hitFace) -> hit(world, pos, player, hand)),
            new BlockInteraction(ToolTypes.hammer, 6, Direction.UP, 0, 4, 0, 4,
                    PropertyMatcher.any, (world, pos, blockState, player, hand, hitFace) -> hit(world, pos, player, hand)),
            new BlockInteraction(ToolTypes.hammer, 7, Direction.UP, 0, 4, 0, 4,
                    PropertyMatcher.any, (world, pos, blockState, player, hand, hitFace) -> hit(world, pos, player, hand))
    };

    protected static final VoxelShape shape = VoxelShapes.or(
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
        RenderingRegistry.registerEntityRenderingHandler(ExtractorProjectileEntity.type, ExtractorProjectileRenderer::new);
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
    public void appendHoverText(final ItemStack stack, @Nullable final IBlockReader world, final List<ITextComponent> tooltip, final ITooltipFlag advanced) {
        tooltip.add(new TranslationTextComponent(description).withStyle(TextFormatting.GRAY));
        tooltip.add(new StringTextComponent(" "));

        if (Screen.hasShiftDown()) {
            tooltip.add(Tooltips.expanded);
            tooltip.add(new StringTextComponent(" "));
            tooltip.add(ForgedBlockCommon.locationTooltip);
            tooltip.add(new StringTextComponent(" "));
            tooltip.add(new TranslationTextComponent(extendedDescription).withStyle(TextFormatting.GRAY));
        } else {
            tooltip.add(Tooltips.expand);
        }
    }

    private static boolean hit(World world, BlockPos pos, @Nullable PlayerEntity playerEntity, Hand hand) {
        if (ConfigHandler.enableExtractor.get()) {
            int amount = Optional.ofNullable(playerEntity)
                    .map(player -> player.getItemInHand(hand))
                    .filter(itemStack -> itemStack.getItem() instanceof IToolProvider)
                    .map(itemStack -> ((IToolProvider) itemStack.getItem()).getToolEfficiency(itemStack, ToolTypes.hammer))
                    .map(Math::round)
                    .orElse(4);

            TileEntityOptional.from(world, pos, ChthonicExtractorTile.class).ifPresent(tile -> tile.damage(amount));
            FracturedBedrockBlock.pierce(world, pos.below(), amount);
            world.playSound(playerEntity, pos, SoundEvents.NETHERITE_BLOCK_HIT, SoundCategory.PLAYERS, 0.8f, 0.5f);
            return true;
        }
        return false;
    }

    private static int getTier(World world, BlockPos pos) {
        return TileEntityOptional.from(world, pos.below(), FracturedBedrockTile.class)
                .map(FracturedBedrockTile::getProjectedTier)
                .orElseGet(() -> FracturedBedrockBlock.canPierce(world, pos.below()) ? 0 : -1);
    }

    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        TileEntityOptional.from(world, pos, ChthonicExtractorTile.class)
                .ifPresent(tile -> tile.setDamage(stack.getDamageValue()));
    }

    public void playerWillDestroy(World world, BlockPos pos, BlockState state, PlayerEntity player) {
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
        TileEntity tile = builder.getOptionalParameter(LootParameters.BLOCK_ENTITY);
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
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new ChthonicExtractorTile();
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return shape;
    }

    @Override
    public BlockInteraction[] getPotentialInteractions(World world, BlockPos pos, BlockState blockState, Direction face, Collection<ToolType> tools) {
        int tier = getTier(world, pos);

        // todo: this could be less hacky
        if (ConfigHandler.enableExtractor.get() && tier >= 0 && face == Direction.UP) {
            return new BlockInteraction[] { interactions[Math.min(tier, interactions.length - 1)] };
        }

        return new BlockInteraction[0];
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        return BlockInteraction.attemptInteraction(world, state, pos, player, hand, hit);
    }
}
