package se.mickelus.tetra.advancements;

import com.google.gson.JsonObject;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import se.mickelus.tetra.blocks.PropertyMatcher;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.data.DataManager;

public class BlockInteractionCriterion extends CriterionInstance {
    private PropertyMatcher after = null;
    private  Capability capability = null;
    private int capabilityLevel = -1;

    public static final GenericTrigger<BlockInteractionCriterion> trigger = new GenericTrigger<>("tetra:block_interaction", BlockInteractionCriterion::deserialize);

    public BlockInteractionCriterion() {
        super(trigger.getId());
    }

    public static void trigger(ServerPlayerEntity player, BlockState state, Capability usedCapability, int usedCapabilityLevel) {
        trigger.fulfillCriterion(player.getAdvancements(), criterion -> criterion.test(state, usedCapability, usedCapabilityLevel));
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

    private static BlockInteractionCriterion deserialize(JsonObject json) {
        return DataManager.instance.gson.fromJson(json, BlockInteractionCriterion.class);
    }
}
