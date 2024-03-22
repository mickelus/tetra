package se.mickelus.tetra.effect;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.apache.commons.lang3.tuple.Pair;
import se.mickelus.tetra.TetraToolActions;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.util.ToolActionHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class StrikingEffect {
    public static final List<Pair<ItemEffect, ToolAction>> effectActionMap = ImmutableList.of(
            Pair.of(ItemEffect.strikingAxe, ToolActions.AXE_DIG),
            Pair.of(ItemEffect.strikingPickaxe, ToolActions.PICKAXE_DIG),
            Pair.of(ItemEffect.strikingCut, TetraToolActions.cut),
            Pair.of(ItemEffect.strikingShovel, ToolActions.SHOVEL_DIG),
            Pair.of(ItemEffect.strikingHoe, ToolActions.HOE_DIG)
    );

    public static boolean causeEffect(Player breakingPlayer, ItemStack itemStack, ItemModularHandheld item, Level world, BlockPos pos, BlockState blockState) {
        int strikingLevel = 0;
        ToolAction tool = null;

        if (breakingPlayer.hasEffect(MobEffects.DIG_SLOWDOWN)) {
            return false;
        }

        // essentially checks if the item is effective for each tool type, and checks if it can strike for that type
        for (Pair<ItemEffect, ToolAction> entry : effectActionMap) {
            if (ToolActionHelper.isEffectiveOn(entry.getRight(), blockState)) {
                strikingLevel = EffectHelper.getEffectLevel(itemStack, entry.getLeft());
                if (strikingLevel > 0) {
                    tool = entry.getRight();
                    break;
                }
            }
        }

        if (strikingLevel > 0) {
            if (breakingPlayer.getAttackStrengthScale(0) > 0.9 && blockState.getDestroySpeed(world, pos) != -1) {
                if (EffectHelper.getEffectLevel(itemStack, ItemEffect.sweepingStrike) > 0) {
                    SweepingStrikeEffect.causeEffect(world, breakingPlayer, itemStack, pos, tool);
                } else {
                    if (ToolActionHelper.playerCanDestroyBlock(breakingPlayer, blockState, pos, itemStack)) {
                        EffectHelper.breakBlock(world, breakingPlayer, itemStack, pos, blockState, true, false);

                        item.applyUsageEffects(breakingPlayer, itemStack, 1);
                        item.applyDamage(item.getBlockDestroyDamage(), itemStack, breakingPlayer);
                    }
                }
            }
            breakingPlayer.resetAttackStrengthTicker();
            return true;
        }

        return false;
    }
}
