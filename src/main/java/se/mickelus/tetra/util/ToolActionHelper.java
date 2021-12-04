package se.mickelus.tetra.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.Tag;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.ForgeEventFactory;
import se.mickelus.tetra.TetraToolActions;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.minecraft.tags.BlockTags.*;
import static se.mickelus.tetra.items.modular.ItemModularHandheld.*;

public class ToolActionHelper {
	public static final BiMap<ToolAction, Tag.Named<Block>> appropriateTools = HashBiMap.create(4);

	static {
		appropriateTools.put(ToolActions.AXE_DIG, MINEABLE_WITH_AXE);
		appropriateTools.put(ToolActions.PICKAXE_DIG, MINEABLE_WITH_PICKAXE);
		appropriateTools.put(ToolActions.SHOVEL_DIG, MINEABLE_WITH_SHOVEL);
		appropriateTools.put(ToolActions.HOE_DIG, MINEABLE_WITH_HOE);
	}

	/*
   public static final Tag.Named<Block> NEEDS_DIAMOND_TOOL = bind("needs_diamond_tool");
   public static final Tag.Named<Block> NEEDS_IRON_TOOL = bind("needs_iron_tool");
   public static final Tag.Named<Block> NEEDS_STONE_TOOL = bind("needs_stone_tool");
	 */

	private static Stream<ToolAction> getActionsFor(BlockState state) {
		return ToolAction.getActions()
			.stream()
			.filter(action -> isToolEffective(action, state));
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
		if (state.getDestroySpeed(player.level, pos) < 0)
			return false;
		if (player.hasEffect(MobEffects.DIG_SLOWDOWN))
			return false;
		if (useAction == null ? !isEffectiveOn(toolStack, state) : !isEffectiveOn(useAction, state))
			return false;
		if (!toolStack.isCorrectToolForDrops(state))
			return false;
		return ForgeEventFactory.doPlayerHarvestCheck(player, state, !state.requiresCorrectToolForDrops() || toolStack.isCorrectToolForDrops(state));
	}
}
