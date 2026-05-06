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
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.EventHooks;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.TetraItemAbilities;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ItemAbilityHelper {
    public static final BiMap<ItemAbility, TagKey<Block>> appropriateTools = HashBiMap.create(5);
    public static final TagKey<Block> hoeExtraTag = BlockTags.create(ResourceLocation.fromNamespaceAndPath(TetraMod.MOD_ID, "hoe_extra_mineable"));
    public static final TagKey<Block> swordVeryEfficient = BlockTags.create(ResourceLocation.fromNamespaceAndPath(TetraMod.MOD_ID, "sword_very_efficient"));
    public static final TagKey<Block> swordInstamine = BlockTags.create(ResourceLocation.fromNamespaceAndPath(TetraMod.MOD_ID, "sword_instamine"));

    public static final Set<TagKey<Block>> cuttingEfficientTags = Sets.newHashSet(BlockTags.SWORD_EFFICIENT, swordVeryEfficient, swordInstamine);

    public static final TagKey<Block> hammerMineable = BlockTags.create(ResourceLocation.withDefaultNamespace("mineable/hammer"));

    public static void init() {
        appropriateTools.put(ItemAbilities.AXE_DIG, BlockTags.MINEABLE_WITH_AXE);
        appropriateTools.put(ItemAbilities.PICKAXE_DIG, BlockTags.MINEABLE_WITH_PICKAXE);
        appropriateTools.put(ItemAbilities.SHOVEL_DIG, BlockTags.MINEABLE_WITH_SHOVEL);
        appropriateTools.put(ItemAbilities.HOE_DIG, BlockTags.MINEABLE_WITH_HOE);
        appropriateTools.put(TetraItemAbilities.hammer, hammerMineable);
    }

    public static Set<ItemAbility> getAppropriateTools(BlockState state) {
        return getActionsFor(state).collect(Collectors.toSet());
    }

    @Nullable
    public static ItemAbility getAppropriateTool(BlockState state) {
        return getActionsFor(state)
                .findFirst()
                .orElse(null);
    }

    public static boolean isEffectiveOn(ItemStack stack, BlockState state) {
        return getActionsFor(state).anyMatch(stack::canPerformAction);
    }

    private static Stream<ItemAbility> getActionsFor(BlockState state) {
        return ItemAbility.getActions().stream()
                .filter(action -> isEffectiveOn(action, state));
    }

    public static boolean isEffectiveOn(ItemAbility action, BlockState state) {
        if (appropriateTools.containsKey(action) && state.is(appropriateTools.get(action)))
            return true;

        if (TetraItemAbilities.cut.equals(action) && cuttingEfficientTags.stream().anyMatch(state::is)) {
            return true;
        }

        if (ItemAbilities.HOE_DIG.equals(action) && state.is(hoeExtraTag)) {
            return true;
        }
//
//        if (ItemAbilities.AXE_DIG.equals(action) && axeMaterials.contains(state.getMaterial())) {
//            return true;
//        }
//
//        return ItemAbilities.PICKAXE_DIG.equals(action) && pickaxeMaterials.contains(state.getMaterial());
        return false;
    }

    public static boolean playerCanDestroyBlock(Player player, BlockState state, BlockPos pos, ItemStack toolStack) {
        return playerCanDestroyBlock(player, state, pos, toolStack, null);
    }

    public static boolean playerCanDestroyBlock(Player player, BlockState state, BlockPos pos, ItemStack toolStack, @Nullable ItemAbility useAction) {
        if (state.getDestroySpeed(player.level(), pos) < 0) {
            return false;
        }
        if (useAction == null ? !isEffectiveOn(toolStack, state) : !isEffectiveOn(useAction, state)) {
            return false;
        }
        if (!toolStack.isCorrectToolForDrops(state)) {
            return false;
        }
        return EventHooks.doPlayerHarvestCheck(player, state, player.level(), pos);
    }
}
