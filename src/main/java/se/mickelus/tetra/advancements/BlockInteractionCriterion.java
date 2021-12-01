package se.mickelus.tetra.advancements;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolType;
import se.mickelus.tetra.blocks.PropertyMatcher;
import se.mickelus.tetra.util.JsonOptional;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
public class BlockInteractionCriterion extends AbstractCriterionTriggerInstance {
    private final PropertyMatcher after;
    private final ToolType toolType;
    private int toolLevel;

    public static final GenericTrigger<BlockInteractionCriterion> trigger = new GenericTrigger<>("tetra:block_interaction", BlockInteractionCriterion::deserialize);

    public BlockInteractionCriterion(EntityPredicate.Composite playerCondition, PropertyMatcher after, ToolType toolType, int toolLevel) {
        super(trigger.getId(), playerCondition);
        this.after = after;
        this.toolType = toolType;
        this.toolLevel = toolLevel;
    }

    public static void trigger(ServerPlayer player, BlockState state, ToolType usedToolType, int usedToolLevel) {
        trigger.fulfillCriterion(player, criterion -> criterion.test(state, usedToolType, usedToolLevel));


    }

    public boolean test(BlockState state, ToolType usedToolType, int usedToolLevel) {
        if (after != null && !after.test(state)) {
            return false;
        }

        if (this.toolType != null && !this.toolType.equals(usedToolType)) {
            return false;
        }

        if (this.toolLevel != -1 && this.toolLevel != usedToolLevel) {
            return false;
        }

        return true;
    }

    private static BlockInteractionCriterion deserialize(JsonObject json, EntityPredicate.Composite entityPredicate, DeserializationContext conditionsParser) {
        return new BlockInteractionCriterion(entityPredicate,
                JsonOptional.field(json, "after")
                        .map(PropertyMatcher::deserialize)
                        .orElse(null),
                JsonOptional.field(json, "tool")
                        .map(JsonElement::getAsString)
                        .map(ToolType::get)
                        .orElse(null),
                JsonOptional.field(json, "toolLevel")
                        .map(JsonElement::getAsInt)
                        .orElse(-1));
    }
}
