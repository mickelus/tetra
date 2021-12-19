package se.mickelus.tetra.blocks.scroll;

import com.google.common.collect.ImmutableList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.forgespi.Environment;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.mutil.util.TileEntityOptional;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.Tooltips;
import se.mickelus.tetra.blocks.scroll.gui.ScrollScreen;
import se.mickelus.tetra.blocks.workbench.AbstractWorkbenchBlock;
import se.mickelus.tetra.items.TetraItemGroup;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
public class ScrollItem extends BlockItem {
    static final String identifier = "scroll_rolled";
    @ObjectHolder(TetraMod.MOD_ID + ":" + identifier)
    public static ScrollItem instance;


    public static ItemStack gemExpertise;
    public static ItemStack metalExpertise;
    public static ItemStack woodExpertise;
    public static ItemStack stoneExpertise;
    public static ItemStack fibreExpertise;
    public static ItemStack skinExpertise;
    public static ItemStack boneExpertise;
    public static ItemStack fabricExpertise;
    public static ItemStack scaleExpertise;
    public static ItemStack hammerEfficiency;
    public static ItemStack axeEfficiency;
    public static ItemStack cutEfficiency;
    public static ItemStack sturdyGuard;
    public static ItemStack throwingKnife;
    public static ItemStack howlingBlade;

    public ScrollItem(Block block) {
        super(block, new Properties().tab(TetraItemGroup.instance).stacksTo(1));

        setRegistryName(TetraMod.MOD_ID, identifier);

        MinecraftForge.EVENT_BUS.register(new ScrollDrops());

        gemExpertise = setupTreatise("gem_expertise", false, 0, 0x2bffee, 14, 13, 14, 15);
        metalExpertise = setupTreatise("metal_expertise", false, 0, 0xffffff, 4, 6, 6, 5);
        woodExpertise = setupTreatise("wood_expertise", false, 0, 0xbf934b, 2, 1, 2, 1);
        stoneExpertise = setupTreatise("stone_expertise", false, 0, 0x9a9a9a, 2);
        fibreExpertise = setupTreatise("fibre_expertise", false, 0, 0xa88054, 5, 10, 11, 4);
        skinExpertise = setupTreatise("skin_expertise", false, 0, 0xbf6930, 0, 1, 1, 1);
        boneExpertise = setupTreatise("bone_expertise", false, 0, 0xfff193, 12, 14, 12, 14);
        fabricExpertise = setupTreatise("fabric_expertise", false, 0, 0xff3333, 5, 3, 6, 4);
        scaleExpertise = setupTreatise("scale_expertise", false, 0, 0x75a03e, 6, 7, 6, 8);
        hammerEfficiency = setupTreatise("hammer_efficiency", false, 0, 0xff6666, 6, 7, 6, 11);
        axeEfficiency = setupTreatise("axe_efficiency", false, 0, 0x66ff66, 0, 1, 3, 3);
        cutEfficiency = setupTreatise("cut_efficiency", false, 0, 0x6666ff, 4, 0, 3, 5);

        sturdyGuard = setupSchematic("sword/sturdy_guard", null, false, 1, 0xbcb8b5, 3, 2, 2, 1);
        throwingKnife = setupSchematic("sword/throwing_knife", null, false, 1, 0xb8ced9, 4, 1, 0, 5);
        howlingBlade = setupSchematic("sword/howling", null, false, 1, 0xfaf396, 8, 9, 10, 5);
    }

    @OnlyIn(Dist.CLIENT)
    public static void clientPostInit() {
        Minecraft.getInstance().getItemColors().register(new ScrollItemColor(), instance);
        ItemProperties.register(instance, new ResourceLocation(TetraMod.MOD_ID, "scroll_mat"),
                (itemStack, world, livingEntity, i) -> ScrollData.readMaterialFast(itemStack));
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        if (allowdedIn(group)) {
            items.add(gemExpertise);
            items.add(metalExpertise);
            items.add(woodExpertise);
            items.add(stoneExpertise);
            items.add(fibreExpertise);
            items.add(skinExpertise);
            items.add(boneExpertise);
            items.add(fabricExpertise);
            items.add(scaleExpertise);
            items.add(hammerEfficiency);
            items.add(axeEfficiency);
            items.add(cutEfficiency);

            items.add(sturdyGuard);
            items.add(throwingKnife);
            items.add(howlingBlade);

            items.add(setupSchematic("double/adze/warforge", "warforge", false, 2, 0x8559b3, 6, 7, 11, 7));
            items.add(setupSchematic("double/basic_axe/warforge", "warforge", false, 2, 0xb35973, 5, 10, 8, 9));
            items.add(setupSchematic("double/basic_hammer/warforge", "warforge", false, 2, 0x3d4299, 9, 8, 11, 10));
            items.add(setupSchematic("double/basic_pickaxe/warforge", "warforge", false, 2, 0x508cb3, 6, 11, 8, 7));
            items.add(setupSchematic("double/claw/warforge", "warforge", false, 2, 0x1d262f, 8, 10, 5, 11));
            items.add(setupSchematic("double/hoe/warforge", "warforge", false, 2, 0x93b350, 10, 7, 9, 5));
            items.add(setupSchematic("double/sickle/warforge", "warforge", false, 2, 0xd99e4c, 5, 9, 6, 10));
            items.add(setupSchematic("double/butt/warforge", "warforge", new String[]{"double/butt_shared/warforge/"}, false, 2, 0xb33636, 11, 5, 8, 9));

            items.add(setupSchematic("hone_gild_1", null, new String[]{"shared/hone_gild_1"}, true, 2, 0xc9ae69, 15, 14, 15, 15));
            items.add(setupSchematic("hone_gild_5", null, new String[]{"shared/hone_gild_"}, true, 2, 0xf2b313, 12, 12, 12, 12));
        }
    }

    private ItemStack setupSchematic(String key, String details, boolean isIntricate, int material, int tint, Integer... glyphs) {
        return setupSchematic(key, details, new String[]{key}, isIntricate, material, tint, glyphs);
    }

    private ItemStack setupSchematic(String key, String details, String[] schematics, boolean isIntricate, int material, int tint, Integer... glyphs) {
        ScrollData data = new ScrollData(key, Optional.ofNullable(details), isIntricate, material, tint, Arrays.asList(glyphs),
                Arrays.stream(schematics).map(s -> new ResourceLocation(TetraMod.MOD_ID, s)).collect(Collectors.toList()),
                Collections.emptyList());

        ItemStack itemStack = new ItemStack(this);
        data.write(itemStack);

        return itemStack;
    }

    private ItemStack setupTreatise(String key, boolean isIntricate, int material, int tint, Integer... glyphs) {
        ScrollData data = new ScrollData(key, Optional.empty(), isIntricate, material, tint, Arrays.asList(glyphs), Collections.emptyList(),
                ImmutableList.of(new ResourceLocation(TetraMod.MOD_ID, key)));

        ItemStack itemStack = new ItemStack(this);
        data.write(itemStack);

        return itemStack;
    }

    @Override
    public Component getName(ItemStack stack) {
        String key = ScrollData.read(stack).key;
        // sometimes called on the server, need to check before calling I18n
        if (!Environment.get().getDist().isDedicatedServer()) {
            String prefixKey = "item.tetra.scroll." + key + ".prefix";
            if (I18n.exists(prefixKey)) {
                return new TranslatableComponent("item.tetra.scroll." + key + ".prefix")
                        .append(new TextComponent(": "))
                        .append(new TranslatableComponent("item.tetra.scroll." + key + ".name"));
            }
        }

        return new TranslatableComponent("item.tetra.scroll." + key + ".name");
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        ScrollData data = ScrollData.read(itemStack);
        StringJoiner attributes = new StringJoiner(" ");

        if (!ScrollData.read(itemStack).schematics.isEmpty()) {
            attributes.add(ChatFormatting.DARK_PURPLE + I18n.get("item.tetra.scroll.schematics"));
        }

        if (!ScrollData.read(itemStack).craftingEffects.isEmpty()) {
            attributes.add(ChatFormatting.DARK_AQUA + I18n.get("item.tetra.scroll.effects"));
        }

        if (data.isIntricate) {
            attributes.add(ChatFormatting.GOLD + I18n.get("item.tetra.scroll.intricate"));
        }

        tooltip.add(new TextComponent(attributes.toString()));
        tooltip.add(new TextComponent(" "));
        tooltip.add(new TranslatableComponent("item.tetra.scroll." + data.key + ".description").withStyle(ChatFormatting.GRAY));
        tooltip.add(new TextComponent(" "));

        if (Screen.hasShiftDown()) {
            tooltip.add(Tooltips.expanded);

            if (!ScrollData.read(itemStack).schematics.isEmpty()) {
                tooltip.add(new TextComponent(" "));
                tooltip.add(new TranslatableComponent("item.tetra.scroll.schematics").withStyle(ChatFormatting.UNDERLINE, ChatFormatting.DARK_PURPLE));
                tooltip.add(new TranslatableComponent("item.tetra.scroll.schematics.description").withStyle(ChatFormatting.GRAY));
            }

            if (!ScrollData.read(itemStack).craftingEffects.isEmpty()) {
                tooltip.add(new TextComponent(" "));
                tooltip.add(new TranslatableComponent("item.tetra.scroll.effects").withStyle(ChatFormatting.UNDERLINE, ChatFormatting.DARK_AQUA));
                tooltip.add(new TranslatableComponent("item.tetra.scroll.effects.description").withStyle(ChatFormatting.GRAY));
            }

            if (data.isIntricate) {
                tooltip.add(new TextComponent(" "));
                tooltip.add(new TranslatableComponent("item.tetra.scroll.intricate").withStyle(ChatFormatting.UNDERLINE, ChatFormatting.GOLD));
                tooltip.add(new TranslatableComponent("item.tetra.scroll.intricate.description").withStyle(ChatFormatting.GRAY));
            } else {
                tooltip.add(new TextComponent(" "));
                tooltip.add(new TranslatableComponent("item.tetra.scroll.range.description").withStyle(ChatFormatting.GRAY));
            }

            if (I18n.exists("item.tetra.scroll." + data.key + ".description_extended")) {
                tooltip.add(new TextComponent(" "));
                tooltip.add(new TranslatableComponent("item.tetra.scroll." + data.key + ".description_extended").withStyle(ChatFormatting.GRAY));
            }
        } else {
            tooltip.add(Tooltips.expand);
        }

        if (flagIn.isAdvanced()) {
            tooltip.add(new TextComponent("s: " + data.schematics + ",e: " + data.craftingEffects));
        }
    }


    private boolean openScroll(ItemStack itemStack, boolean isRemote) {
        ScrollData data = ScrollData.read(itemStack);
        if (data.details != null) {
            if (isRemote) {
                showDetailsScreen(data.details);
            }
            return true;
        }

        return false;
    }

    @OnlyIn(Dist.CLIENT)
    private void showDetailsScreen(String detailsKey) {
        ScrollScreen screen = new ScrollScreen(detailsKey);
        Minecraft.getInstance().setScreen(screen);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (openScroll(player.getItemInHand(hand), world.isClientSide)) {
            return InteractionResultHolder.sidedSuccess(itemstack, world.isClientSide());
        }
        return InteractionResultHolder.pass(itemstack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack itemStack = context.getItemInHand();
        Player player = context.getPlayer();
        Block block = context.getLevel().getBlockState(context.getClickedPos()).getBlock();

        if (!(block instanceof AbstractWorkbenchBlock) && player != null && player.isCrouching() && openScroll(itemStack, world.isClientSide)) {
            return InteractionResult.sidedSuccess(world.isClientSide);
        }

        // add scroll to an existing stack of rolled up scrolls
        if (RolledScrollBlock.instance.equals(block)) {
            boolean success = TileEntityOptional.from(world, pos, ScrollTile.class)
                    .map(tile -> tile.addScroll(itemStack))
                    .orElse(false);

            if (success) {
                if (player == null || !player.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
                return InteractionResult.sidedSuccess(world.isClientSide);
            }
        }

        return place(new BlockPlaceContext(context));
    }

    @Nullable
    @Override
    protected BlockState getPlacementState(BlockPlaceContext context) {
        BlockState state = getBlock().defaultBlockState();

        if (context.getClickedFace().getAxis().getPlane() == Direction.Plane.HORIZONTAL) {
            state = WallScrollBlock.instance.defaultBlockState()
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, context.getClickedFace());
        } else {
            if (context.getLevel().getBlockState(context.getClickedPos().relative(context.getClickedFace().getOpposite())).getBlock() instanceof AbstractWorkbenchBlock) {
                state = OpenScrollBlock.instance.defaultBlockState();
            }

            state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection());
        }

        return canPlace(context, state) ? state : null;
    }
}
