package se.mickelus.tetra.advancements;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import se.mickelus.mutil.util.JsonOptional;


public class DestabilizeCriterion extends AbstractCriterionTriggerInstance {
    public static final GenericTrigger<DestabilizeCriterion> trigger = new GenericTrigger<>("tetra:destabilize", DestabilizeCriterion::deserialize);
    private final ItemPredicate item;

    public DestabilizeCriterion(ContextAwarePredicate playerCondition, ItemPredicate item) {
        super(trigger.getId(), playerCondition);
        this.item = item;
    }

    public static void trigger(ServerPlayer player, ItemStack usedItem) {
        trigger.fulfillCriterion(player, criterion -> criterion.test(usedItem));
    }

    private static DestabilizeCriterion deserialize(JsonObject json, ContextAwarePredicate entityPredicate, DeserializationContext conditionsParser) {
        return new DestabilizeCriterion(entityPredicate, JsonOptional.field(json, "item").map(ItemPredicate::fromJson).orElse(null));
    }

    public boolean test(ItemStack usedItem) {
        if (item != null && !item.matches(usedItem)) {
            return false;
        }

        return true;
    }
}
