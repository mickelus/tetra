package se.mickelus.tetra.advancements;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import org.apache.commons.lang3.EnumUtils;
import se.mickelus.tetra.blocks.PropertyMatcher;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.util.JsonOptional;

public class BlockInteractionCriterion extends CriterionInstance {
    private final PropertyMatcher after;
    private final Capability capability;
    private int capabilityLevel;

    public static final GenericTrigger<BlockInteractionCriterion> trigger = new GenericTrigger<>("tetra:block_interaction", BlockInteractionCriterion::deserialize);

    public BlockInteractionCriterion(EntityPredicate.AndPredicate playerCondition, PropertyMatcher after, Capability capability, int capabilityLevel) {
        super(trigger.getId(), playerCondition);
        this.after = after;
        this.capability = capability;
        this.capabilityLevel = capabilityLevel;
    }

    public static void trigger(ServerPlayerEntity player, BlockState state, Capability usedCapability, int usedCapabilityLevel) {
        trigger.fulfillCriterion(player, criterion -> criterion.test(state, usedCapability, usedCapabilityLevel));


    }

    public boolean test(BlockState state, Capability usedCapability, int usedCapabilityLevel) {
        if (after != null && !after.test(state)) {
            return false;
        }

        if (this.capability != null && !this.capability.equals(usedCapability)) {
            return false;
        }

        if (this.capabilityLevel != -1 && this.capabilityLevel != usedCapabilityLevel) {
            return false;
        }

        return true;
    }

    private static BlockInteractionCriterion deserialize(JsonObject json, EntityPredicate.AndPredicate entityPredicate, ConditionArrayParser conditionsParser) {
        return new BlockInteractionCriterion(entityPredicate,
                JsonOptional.field(json, "block")
                        .map(PropertyMatcher::deserialize)
                        .orElse(null),
                JsonOptional.field(json, "capability")
                        .map(JsonElement::getAsString)
                        .filter(cap -> EnumUtils.isValidEnum(Capability.class, cap))
                        .map(Capability::valueOf)
                        .orElse(null),
                JsonOptional.field(json, "capabilityLevel")
                        .map(JsonElement::getAsInt)
                        .orElse(-1));
    }
}
