package se.mickelus.tetra.advancements;

import com.google.gson.JsonObject;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.data.DataHandler;

public class ImprovementCraftCriterion extends CriterionInstance {
    private ItemPredicate before = null;
    private ItemPredicate after = null;

    private String schema = null;

    private String slot = null;
    private String improvement = null;
    private int improvementLevel = -1;

    private Capability capability = null;
    private int capabilityLevel = -1;

    public static final GenericTrigger<ImprovementCraftCriterion> trigger = new GenericTrigger<>("tetra:craft_improvement", ImprovementCraftCriterion::deserialize);

    public ImprovementCraftCriterion() {
        super(trigger.getId());
    }

    public static void trigger(ServerPlayerEntity player, ItemStack before, ItemStack after, String schema, String slot, String improvement,
            int improvementLevel, Capability capability, int capabilityLevel) {
        trigger.fulfillCriterion(player.getAdvancements(), criterion -> criterion.test(before, after, schema, slot, improvement, improvementLevel,
                capability, capabilityLevel));
    }

    public boolean test(ItemStack before, ItemStack after, String schema, String slot, String improvement, int improvementLevel,
            Capability capability, int capabilityLevel) {

        if (this.before != null && !this.before.test(before)) {
            return false;
        }

        if (this.after != null && !this.after.test(after)) {
            return false;
        }

        if (this.schema != null && !this.schema.equals(schema)) {
            return false;
        }

        if (this.slot != null && !this.slot.equals(slot)) {
            return false;
        }

        if (this.improvement != null && !this.improvement.equals(improvement)) {
            return false;
        }

        if (this.improvementLevel != -1 && this.improvementLevel != improvementLevel) {
            return false;
        }

        if (this.capability != null && !this.capability.equals(capability)) {
            return false;
        }

        if (this.capabilityLevel != -1 && this.capabilityLevel != capabilityLevel) {
            return false;
        }

        return true;
    }

    private static ImprovementCraftCriterion deserialize(JsonObject json) {
        return DataHandler.instance.gson.fromJson(json, ImprovementCraftCriterion.class);
    }
}
