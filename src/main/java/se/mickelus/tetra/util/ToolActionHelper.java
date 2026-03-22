package se.mickelus.tetra.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.ForgeEventFactory;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.TetraToolActions;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ToolActionHelper {
    public static final BiMap<ToolAction, TagKey<Block>> appropriateTools = HashBiMap.create(5);
    public static final TagKey<Block> hoeExtraTag = BlockTags.create(new ResourceLocation(TetraMod.MOD_ID, "hoe_extra_mineable"));
    public static final TagKey<Block> swordVeryEfficient = BlockTags.create(new ResourceLocation(TetraMod.MOD_ID, "sword_very_efficient"));
    public static final TagKey<Block> swordInstamine = BlockTags.create(new ResourceLocation(TetraMod.MOD_ID, "sword_instamine"));

    public static final Set<TagKey<Block>> cuttingEfficientTags = Sets.newHashSet(BlockTags.SWORD_EFFICIENT, swordVeryEfficient, swordInstamine);

    public static final TagKey<Block> hammerMineable = BlockTags.create(new ResourceLocation("mineable/hammer"));

    public static void init() {
        appropriateTools.put(ToolActions.AXE_DIG, BlockTags.MINEABLE_WITH_AXE);
        appropriateTools.put(ToolActions.PICKAXE_DIG, BlockTags.MINEABLE_WITH_PICKAXE);
        appropriateTools.put(ToolActions.SHOVEL_DIG, BlockTags.MINEABLE_WITH_SHOVEL);
        appropriateTools.put(ToolActions.HOE_DIG, BlockTags.MINEABLE_WITH_HOE);
        appropriateTools.put(TetraToolActions.hammer, hammerMineable);
    }

    public static Set<ToolAction> getAppropriateTools(BlockState state) {
        return getActionsFor(state).collect(Collectors.toSet());
    }

    @Nullable
    public static ToolAction getAppropriateTool(BlockState state) {
        return getActionsFor(state)
                .findFirst()
                .orElse(null);
    }

    public static boolean isEffectiveOn(ItemStack stack, BlockState state) {
        return getActionsFor(state).anyMatch(stack::canPerformAction);
    }

    private static Stream<ToolAction> getActionsFor(BlockState state) {
        return ToolAction.getActions().stream()
                .filter(action -> isEffectiveOn(action, state));
    }

    public static boolean isEffectiveOn(ToolAction action, BlockState state) {
        if (appropriateTools.containsKey(action) && state.is(appropriateTools.get(action)))
            return true;

        if (TetraToolActions.cut.equals(action) && cuttingEfficientTags.stream().anyMatch(state::is)) {
            return true;
        }

        if (ToolActions.HOE_DIG.equals(action) && state.is(hoeExtraTag)) {
            return true;
        }
//
//        if (ToolActions.AXE_DIG.equals(action) && axeMaterials.contains(state.getMaterial())) {
//            return true;
//        }
//
//        return ToolActions.PICKAXE_DIG.equals(action) && pickaxeMaterials.contains(state.getMaterial());
        return false;
    }

    public static boolean playerCanDestroyBlock(Player player, BlockState state, BlockPos pos, ItemStack toolStack) {
        return playerCanDestroyBlock(player, state, pos, toolStack, null);
    }

    public static boolean playerCanDestroyBlock(Player player, BlockState state, BlockPos pos, ItemStack toolStack, @Nullable ToolAction useAction) {
        if (state.getDestroySpeed(player.level(), pos) < 0) {
            return false;
        }
        if (useAction == null ? !isEffectiveOn(toolStack, state) : !isEffectiveOn(useAction, state)) {
            return false;
        }
        if (!toolStack.isCorrectToolForDrops(state)) {
            return false;
        }
        return ForgeEventFactory.doPlayerHarvestCheck(player, state, true);
    }
}
