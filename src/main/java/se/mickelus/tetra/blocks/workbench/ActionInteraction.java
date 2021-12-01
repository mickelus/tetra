package se.mickelus.tetra.blocks.workbench;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import se.mickelus.tetra.blocks.salvage.BlockInteraction;
import se.mickelus.tetra.blocks.salvage.InteractionOutcome;
import se.mickelus.tetra.blocks.workbench.action.WorkbenchAction;
import se.mickelus.tetra.util.CastOptional;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Map;
@ParametersAreNonnullByDefault
public class ActionInteraction extends BlockInteraction {

    private String actionKey;

    public ActionInteraction(ToolAction requiredType, int requiredLevel, String actionKey) {
        super(requiredType, requiredLevel, Direction.UP, 5, 11, 5, 11, InteractionOutcome.EMPTY);

        this.actionKey = actionKey;

        applyUsageEffects = false;
    }

    public static ActionInteraction create(WorkbenchTile tile) {
        ItemStack targetStack = tile.getTargetItemStack();
        return Arrays.stream(tile.getAvailableActions(null))
                .filter(WorkbenchAction::allowInWorldInteraction)
                .filter(action -> action.getRequiredTools(targetStack).entrySet().size() == 1)
                .findFirst()
                .map(action -> {
                    Map.Entry<ToolAction, Integer> requirementPair = action.getRequiredTools(targetStack).entrySet().stream().findFirst().get();
                    return new ActionInteraction(requirementPair.getKey(), requirementPair.getValue(), action.getKey());
                })
                .orElse(null);
    }

    @Override
    public boolean applicableForBlock(Level world, BlockPos pos, BlockState blockState) {
        return actionKey != null;
    }

    @Override
    public void applyOutcome(Level world, BlockPos pos, BlockState blockState, @Nullable Player player, @Nullable InteractionHand hand, Direction hitFace) {
        if (!world.isClientSide) {
            CastOptional.cast(world.getBlockEntity(pos), WorkbenchTile.class)
                    .ifPresent(tile -> {
                        if (player != null) {
                            tile.performAction(player, actionKey);
                        } else {
                            tile.performAction(actionKey);
                        }
                    });
        }
    }
}
