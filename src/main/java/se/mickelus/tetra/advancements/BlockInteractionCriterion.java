package se.mickelus.tetra.advancements;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.PlayerEntityMP;
import se.mickelus.tetra.blocks.PropertyMatcher;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.data.DataHandler;

public class BlockInteractionCriterion extends AbstractCriterionInstance {
    private PropertyMatcher after = null;
    private  Capability capability = null;
    private int capabilityLevel = -1;

    public static final GenericTrigger<BlockInteractionCriterion> trigger = new GenericTrigger<>("tetra:block_interaction", BlockInteractionCriterion::deserialize);

    public BlockInteractionCriterion() {
        super(trigger.getId());
    }

    public static void trigger(PlayerEntityMP player, IBlockState state, Capability usedCapability, int usedCapabilityLevel) {
        trigger.fulfillCriterion(player.getAdvancements(), criterion -> criterion.test(state, usedCapability, usedCapabilityLevel));
    }

    public boolean test(IBlockState state, Capability usedCapability, int usedCapabilityLevel) {
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
        return DataHandler.instance.gson.fromJson(json, BlockInteractionCriterion.class);
    }
}
