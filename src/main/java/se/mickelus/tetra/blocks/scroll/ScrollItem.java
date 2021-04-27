package se.mickelus.tetra.blocks.scroll;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.Tooltips;
import se.mickelus.tetra.blocks.scroll.gui.ScrollScreen;
import se.mickelus.tetra.blocks.workbench.AbstractWorkbenchBlock;
import se.mickelus.tetra.items.TetraItemGroup;
import se.mickelus.tetra.util.TileEntityOptional;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class ScrollItem extends BlockItem {
    static final String identifier = "scroll_rolled";
    @ObjectHolder(TetraMod.MOD_ID + ":" + identifier)
    public static ScrollItem instance;

    public ScrollItem(Block block) {
        super(block, new Properties().group(TetraItemGroup.instance).maxStackSize(1));

        setRegistryName(TetraMod.MOD_ID, identifier);

        MinecraftForge.EVENT_BUS.register(new ScrollDrops());
    }

    @OnlyIn(Dist.CLIENT)
    public static void clientPostInit() {
        Minecraft.getInstance().getItemColors().register(new ScrollItemColor(), instance);
        ItemModelsProperties.registerProperty(instance, new ResourceLocation(TetraMod.MOD_ID, "scroll_mat"),
                (itemStack, world, livingEntity) -> ScrollData.read(itemStack).material);
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        if (isInGroup(group) && ConfigHandler.enableScrolls.get()) {
            items.add(setupTreatise("gem_expertise",     false, 0, 0x2bffee, 14, 13, 14, 15));
            items.add(setupTreatise("metal_expertise",   false, 0, 0xffffff, 4, 6, 6, 5));
            items.add(setupTreatise("wood_expertise",    false, 0, 0xbf934b, 2, 1, 2, 1));
            items.add(setupTreatise("stone_expertise",   false, 0, 0x9a9a9a, 2));
            items.add(setupTreatise("fibre_expertise",   false, 0, 0xa88054, 5, 10, 11, 4));
            items.add(setupTreatise("skin_expertise",    false, 0, 0xbf6930, 0, 1, 1, 1));
            items.add(setupTreatise("bone_expertise",    false, 0, 0xfff193, 12, 14, 12, 14));
            items.add(setupTreatise("fabric_expertise",  false, 0, 0xff3333, 5, 3, 6, 4));
            items.add(setupTreatise("scale_expertise",   false, 0, 0x75a03e, 6, 7, 6, 8));
            items.add(setupTreatise("hammer_efficiency", false, 0, 0xff6666, 6, 7, 6, 11));
            items.add(setupTreatise("axe_efficiency",    false, 0, 0x66ff66, 0, 1, 3, 3));
            items.add(setupTreatise("cut_efficiency",    false, 0, 0x6666ff, 4, 0, 3, 5));

            items.add(setupSchematic("sword/sturdy_guard",   null, false, 1, 0xbcb8b5, 3, 2, 2, 1));
            items.add(setupSchematic("sword/throwing_knife", null, false, 1, 0xb8ced9, 4, 1, 0, 5));
            items.add(setupSchematic("sword/howling",        null, false, 1, 0xfaf396, 8, 9, 10, 5));

            items.add(setupSchematic("double/adze/warforge",          "warforge", false, 2, 0x8559b3, 6, 7, 11, 7));
            items.add(setupSchematic("double/basic_axe/warforge",     "warforge", false, 2, 0xb35973, 5, 10, 8, 9));
            items.add(setupSchematic("double/basic_hammer/warforge",  "warforge", false, 2, 0x3d4299, 9, 8, 11, 10));
            items.add(setupSchematic("double/basic_pickaxe/warforge", "warforge", false, 2, 0x508cb3, 6, 11, 8, 7));
            items.add(setupSchematic("double/claw/warforge",          "warforge", false, 2, 0x1d262f, 8, 10, 5, 11));
            items.add(setupSchematic("double/hoe/warforge",           "warforge", false, 2, 0x93b350, 10, 7, 9, 5));
            items.add(setupSchematic("double/sickle/warforge",        "warforge", false, 2, 0xd99e4c, 5, 9, 6, 10));
            items.add(setupSchematic("double/butt/warforge", null, new String[] { "double/butt_shared/warforge/" }, false, 2, 0xb33636, 11, 5, 8, 9));

            items.add(setupSchematic("hone_gild_1", null, new String[] { "shared/hone_gild_1" }, true, 2, 0xc9ae69, 15, 14, 15, 15));
            items.add(setupSchematic("hone_gild_5", null, new String[] { "shared/hone_gild_" }, true, 2, 0xf2b313, 12, 12, 12, 12));
        }
    }

    private ItemStack setupSchematic(String key, String details, boolean isIntricate, int material, int tint, Integer ... glyphs) {
        return setupSchematic(key, details, new String[] { key }, isIntricate, material, tint, glyphs);
    }

    private ItemStack setupSchematic(String key, String details, String[] schematics, boolean isIntricate, int material, int tint, Integer ... glyphs) {
        ScrollData data = new ScrollData(key, Optional.ofNullable(details), isIntricate, material, tint, Arrays.asList(glyphs),
                Arrays.stream(schematics).map(s -> new ResourceLocation(TetraMod.MOD_ID, s)).collect(Collectors.toList()),
                Collections.emptyList());

        ItemStack itemStack = new ItemStack(ScrollItem.instance);
        data.write(itemStack);

        return itemStack;
    }

    private ItemStack setupTreatise(String key, boolean isIntricate, int material, int tint, Integer ... glyphs) {
        ScrollData data = new ScrollData(key, Optional.empty(), isIntricate, material, tint, Arrays.asList(glyphs), Collections.emptyList(),
                ImmutableList.of(new ResourceLocation(TetraMod.MOD_ID, key)));

        ItemStack itemStack = new ItemStack(ScrollItem.instance);
        data.write(itemStack);

        return itemStack;
    }

    @Override
    public ITextComponent getDisplayName(ItemStack stack) {
        String key = ScrollData.read(stack).key;
        String prefixKey = "item.tetra.scroll." + key + ".prefix";
        if (I18n.hasKey(prefixKey)) {
            return new TranslationTextComponent("item.tetra.scroll." + key + ".prefix")
                    .append(new StringTextComponent(": "))
                    .append(new TranslationTextComponent("item.tetra.scroll." + key + ".name"));
        }
        return new TranslationTextComponent("item.tetra.scroll." + key + ".name");
    }

    @Override
    public void addInformation(ItemStack itemStack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        ScrollData data = ScrollData.read(itemStack);
        StringJoiner attributes = new StringJoiner(" ");

        if (!ScrollData.read(itemStack).schematics.isEmpty()) {
            attributes.add(TextFormatting.DARK_PURPLE + I18n.format("item.tetra.scroll.schematics"));
        }

        if (!ScrollData.read(itemStack).craftingEffects.isEmpty()) {
            attributes.add(TextFormatting.DARK_AQUA + I18n.format("item.tetra.scroll.effects"));
        }

        if (data.isIntricate) {
            attributes.add(TextFormatting.GOLD + I18n.format("item.tetra.scroll.intricate"));
        }

        tooltip.add(new StringTextComponent(attributes.toString()));
        tooltip.add(new StringTextComponent(" "));
        tooltip.add(new TranslationTextComponent("item.tetra.scroll." + data.key + ".description").mergeStyle(TextFormatting.GRAY));
        tooltip.add(new StringTextComponent(" "));

        if (Screen.hasShiftDown()) {
            tooltip.add(Tooltips.expanded);

            if (!ScrollData.read(itemStack).schematics.isEmpty()) {
                tooltip.add(new StringTextComponent(" "));
                tooltip.add(new TranslationTextComponent("item.tetra.scroll.schematics").mergeStyle(TextFormatting.UNDERLINE, TextFormatting.DARK_PURPLE));
                tooltip.add(new TranslationTextComponent("item.tetra.scroll.schematics.description").mergeStyle(TextFormatting.GRAY));
            }

            if (!ScrollData.read(itemStack).craftingEffects.isEmpty()) {
                tooltip.add(new StringTextComponent(" "));
                tooltip.add(new TranslationTextComponent("item.tetra.scroll.effects").mergeStyle(TextFormatting.UNDERLINE, TextFormatting.DARK_AQUA));
                tooltip.add(new TranslationTextComponent("item.tetra.scroll.effects.description").mergeStyle(TextFormatting.GRAY));
            }

            if (data.isIntricate) {
                tooltip.add(new StringTextComponent(" "));
                tooltip.add(new TranslationTextComponent("item.tetra.scroll.intricate").mergeStyle(TextFormatting.UNDERLINE, TextFormatting.GOLD));
                tooltip.add(new TranslationTextComponent("item.tetra.scroll.intricate.description").mergeStyle(TextFormatting.GRAY));
            } else {
                tooltip.add(new StringTextComponent(" "));
                tooltip.add(new TranslationTextComponent("item.tetra.scroll.range.description").mergeStyle(TextFormatting.GRAY));
            }

            if (I18n.hasKey("item.tetra.scroll." + data.key + ".description_extended")) {
                tooltip.add(new StringTextComponent(" "));
                tooltip.add(new TranslationTextComponent("item.tetra.scroll." + data.key + ".description_extended").mergeStyle(TextFormatting.GRAY));
            }
        } else {
            tooltip.add(Tooltips.expand);
        }

        if (flagIn.isAdvanced()) {
            tooltip.add(new StringTextComponent("s: " + data.schematics + ",e: " + data.craftingEffects));
        }
    }

    private boolean showDetailsScreen(ItemStack itemStack, boolean isRemote) {
        ScrollData data = ScrollData.read(itemStack);
        if (data.details != null) {
            if (isRemote) {
                ScrollScreen screen = new ScrollScreen(data.details);
                Minecraft.getInstance().displayGuiScreen(screen);
            }
            return true;
        }

        return false;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        if (showDetailsScreen(player.getHeldItem(hand), world.isRemote)) {
            return ActionResult.func_233538_a_(itemstack, world.isRemote());
        }
        return ActionResult.resultPass(itemstack);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        ItemStack itemStack = context.getItem();
        PlayerEntity player = context.getPlayer();
        Block block = context.getWorld().getBlockState(context.getPos()).getBlock();

        if (!(block instanceof AbstractWorkbenchBlock) && player != null && player.isCrouching() && showDetailsScreen(itemStack, world.isRemote)) {
            return ActionResultType.func_233537_a_(world.isRemote);
        }

        // add scroll to an existing stack of rolled up scrolls
        if (RolledScrollBlock.instance.equals(block)) {
            boolean success = TileEntityOptional.from(world, pos, ScrollTile.class)
                    .map(tile -> tile.addScroll(itemStack))
                    .orElse(false);

            if (success) {
                if (player == null || !player.abilities.isCreativeMode) {
                    itemStack.shrink(1);
                }
                return ActionResultType.func_233537_a_(world.isRemote);
            }
        }

        return tryPlace(new BlockItemUseContext(context));
    }

    @Nullable
    @Override
    protected BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockState state = getBlock().getDefaultState();

        if (context.getFace().getAxis().getPlane() == Direction.Plane.HORIZONTAL) {
            state = WallScrollBlock.instance.getDefaultState()
                    .with(BlockStateProperties.HORIZONTAL_FACING, context.getFace());
        } else {
            if (context.getWorld().getBlockState(context.getPos().offset(context.getFace().getOpposite())).getBlock() instanceof AbstractWorkbenchBlock) {
                state = OpenScrollBlock.instance.getDefaultState();
            }

            state = state.with(BlockStateProperties.HORIZONTAL_FACING, context.getPlacementHorizontalFacing());
        }

        return canPlace(context, state) ? state : null;
    }
}
