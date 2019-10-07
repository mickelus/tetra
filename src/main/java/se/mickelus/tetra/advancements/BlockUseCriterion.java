package se.mickelus.tetra.advancements;

import com.google.gson.JsonObject;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.blocks.PropertyMatcher;
import se.mickelus.tetra.data.DataHandler;

public class BlockUseCriterion extends CriterionInstance {
    private PropertyMatcher before = null;
    private PropertyMatcher after = null;

    private ItemPredicate item = null;

    public static final GenericTrigger<BlockUseCriterion> trigger = new GenericTrigger<>("tetra:block_use", BlockUseCriterion::deserialize);

    public BlockUseCriterion() {
        super(trigger.getId());
    }

    public static void trigger(ServerPlayerEntity player, BlockState state, ItemStack usedItem) {
        trigger.fulfillCriterion(player.getAdvancements(), criterion -> criterion.test(state, usedItem));
    }

    public boolean test(BlockState state, ItemStack usedItem) {
        if (before != null && !before.test(state)) {
            return false;
        }

        if (after != null && !after.test(state)) {
            return false;
        }

        if (item != null && !item.test(usedItem)) {
            return false;
        }

        return true;
    }

    private static BlockUseCriterion deserialize(JsonObject json) {
        return DataHandler.instance.gson.fromJson(json, BlockUseCriterion.class);
    }
}
