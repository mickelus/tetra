package se.mickelus.tetra.blocks.workbench;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import se.mickelus.tetra.blocks.salvage.BlockInteraction;
import se.mickelus.tetra.blocks.salvage.InteractionOutcome;
import se.mickelus.tetra.blocks.workbench.action.WorkbenchAction;
import se.mickelus.tetra.util.CastOptional;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Map;

public class ActionInteraction extends BlockInteraction {

    private String actionKey;

    public ActionInteraction(ToolType requiredType, int requiredLevel, String actionKey) {
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
                    Map.Entry<ToolType, Integer> requirementPair = action.getRequiredTools(targetStack).entrySet().stream().findFirst().get();
                    return new ActionInteraction(requirementPair.getKey(), requirementPair.getValue(), action.getKey());
                })
                .orElse(null);
    }

    @Override
    public boolean applicableForBlock(World world, BlockPos pos, BlockState blockState) {
        return actionKey != null;
    }

    @Override
    public void applyOutcome(World world, BlockPos pos, BlockState blockState, @Nullable PlayerEntity player, @Nullable Hand hand, Direction hitFace) {
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
