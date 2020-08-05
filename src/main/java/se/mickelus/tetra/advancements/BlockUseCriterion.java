package se.mickelus.tetra.advancements;

import com.google.gson.JsonObject;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ConditionArrayParser;
import se.mickelus.tetra.blocks.PropertyMatcher;
import se.mickelus.tetra.util.JsonOptional;

public class BlockUseCriterion extends CriterionInstance {
    private final PropertyMatcher before;
    private final PropertyMatcher after;

    private final ItemPredicate item;

    public static final GenericTrigger<BlockUseCriterion> trigger = new GenericTrigger<>("tetra:block_use", BlockUseCriterion::deserialize);

    public BlockUseCriterion(EntityPredicate.AndPredicate playerCondition, PropertyMatcher before, PropertyMatcher after, ItemPredicate item) {
        super(trigger.getId(), playerCondition);
        this.before = before;
        this.after = after;
        this.item = item;
    }

    public static void trigger(ServerPlayerEntity player, BlockState state, ItemStack usedItem) {
        trigger.fulfillCriterion(player, criterion -> criterion.test(state, usedItem));
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

    private static BlockUseCriterion deserialize(JsonObject json, EntityPredicate.AndPredicate entityPredicate, ConditionArrayParser conditionsParser) {
        return new BlockUseCriterion(entityPredicate,
                JsonOptional.field(json, "before")
                        .map(PropertyMatcher::deserialize)
                        .orElse(null),
                JsonOptional.field(json, "after")
                        .map(PropertyMatcher::deserialize)
                        .orElse(null),
                JsonOptional.field(json, "item")
                        .map(ItemPredicate::deserialize)
                        .orElse(null));
    }
}
