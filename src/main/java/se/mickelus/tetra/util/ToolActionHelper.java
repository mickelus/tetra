package se.mickelus.tetra.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.ForgeEventFactory;
import se.mickelus.tetra.TetraToolActions;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ToolActionHelper {
    public static final BiMap<ToolAction, Tag.Named<Block>> appropriateTools = HashBiMap.create(4);
    /**
     * Below are lists of blocks, materials and tags that describe what different tools can harvest and efficiently destroy. Note that these
     * are copies of what the vanilla tool counterparts explicitly state that they can destroy and harvest, some blocks (and required tiers)
     * are not listed here as that's part of that block's implementation.
     */

    // FIXME add 1.18 materials
    public static final Set<Material> hoeBonusMaterials = Sets.newHashSet(Material.PLANT, Material.REPLACEABLE_PLANT);
    public static final Set<Material> axeMaterials = Sets.newHashSet(Material.WOOD, Material.NETHER_WOOD, Material.PLANT, Material.REPLACEABLE_PLANT, Material.BAMBOO, Material.VEGETABLE);
    public static final Set<Material> pickaxeMaterials = Sets.newHashSet(Material.METAL, Material.HEAVY_METAL, Material.STONE);

    // copy of hardcoded values in SwordItem, materials & tag that it explicitly state it can efficiently DESTROY
    public static final Set<Material> cuttingDestroyMaterials = Sets.newHashSet(Material.PLANT, Material.REPLACEABLE_PLANT, Material.VEGETABLE, Material.WEB, Material.BAMBOO);
    public static final Set<Tag.Named<Block>> cuttingDestroyTags = Sets.newHashSet(BlockTags.LEAVES);

    // copy of hardcoded values in SwordItem, blocks that the sword explicitly state it can efficiently HARVEST
    public static final Set<Block> cuttingHarvestBlocks = Sets.newHashSet(Blocks.COBWEB);

    public static void init() {
        appropriateTools.put(ToolActions.AXE_DIG, BlockTags.MINEABLE_WITH_AXE);
        appropriateTools.put(ToolActions.PICKAXE_DIG, BlockTags.MINEABLE_WITH_PICKAXE);
        appropriateTools.put(ToolActions.SHOVEL_DIG, BlockTags.MINEABLE_WITH_SHOVEL);
        appropriateTools.put(ToolActions.HOE_DIG, BlockTags.MINEABLE_WITH_HOE);
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

        if (TetraToolActions.cut.equals(action)
                && (cuttingHarvestBlocks.contains(state.getBlock())
                || cuttingDestroyMaterials.contains(state.getMaterial())
                || cuttingDestroyTags.stream().anyMatch(state::is))) {
            return true;
        }

        if (ToolActions.HOE_DIG.equals(action) && hoeBonusMaterials.contains(state.getMaterial())) {
            return true;
        }

        if (ToolActions.AXE_DIG.equals(action) && axeMaterials.contains(state.getMaterial())) {
            return true;
        }

        return ToolActions.PICKAXE_DIG.equals(action) && pickaxeMaterials.contains(state.getMaterial());
    }

    public static boolean playerCanDestroyBlock(Player player, BlockState state, BlockPos pos, ItemStack toolStack) {
        return playerCanDestroyBlock(player, state, pos, toolStack, null);
    }

    public static boolean playerCanDestroyBlock(Player player, BlockState state, BlockPos pos, ItemStack toolStack, @Nullable ToolAction useAction) {
        if (state.getDestroySpeed(player.level, pos) < 0) {
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
