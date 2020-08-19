package se.mickelus.tetra.advancements;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraftforge.common.ToolType;
import org.apache.commons.lang3.EnumUtils;
import se.mickelus.tetra.blocks.PropertyMatcher;
import se.mickelus.tetra.util.JsonOptional;

public class BlockInteractionCriterion extends CriterionInstance {
    private final PropertyMatcher after;
    private final ToolType toolType;
    private int capabilityLevel;

    public static final GenericTrigger<BlockInteractionCriterion> trigger = new GenericTrigger<>("tetra:block_interaction", BlockInteractionCriterion::deserialize);

    public BlockInteractionCriterion(EntityPredicate.AndPredicate playerCondition, PropertyMatcher after, ToolType toolType, int capabilityLevel) {
        super(trigger.getId(), playerCondition);
        this.after = after;
        this.toolType = toolType;
        this.capabilityLevel = capabilityLevel;
    }

    public static void trigger(ServerPlayerEntity player, BlockState state, ToolType usedToolType, int usedCapabilityLevel) {
        trigger.fulfillCriterion(player, criterion -> criterion.test(state, usedToolType, usedCapabilityLevel));


    }

    public boolean test(BlockState state, ToolType usedToolType, int usedCapabilityLevel) {
        if (after != null && !after.test(state)) {
            return false;
        }

        if (this.toolType != null && !this.toolType.equals(usedToolType)) {
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
                        .map(ToolType::get)
                        .orElse(null),
                JsonOptional.field(json, "capabilityLevel")
                        .map(JsonElement::getAsInt)
                        .orElse(-1));
    }
}
