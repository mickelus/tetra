package se.mickelus.tetra.advancements;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.EnumUtils;
import se.mickelus.tetra.blocks.PropertyMatcher;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.module.schema.SchemaDefinition;
import se.mickelus.tetra.util.JsonOptional;

public class ImprovementCraftCriterion extends CriterionInstance {
    private final ItemPredicate before;
    private final ItemPredicate after;

    private final String schema;

    private final String slot;
    private final String improvement;
    private final int improvementLevel;

    private final Capability capability;
    private final int capabilityLevel;

    public static final GenericTrigger<ImprovementCraftCriterion> trigger = new GenericTrigger<>("tetra:craft_improvement", ImprovementCraftCriterion::deserialize);

    public ImprovementCraftCriterion(EntityPredicate.AndPredicate playerCondition, ItemPredicate before, ItemPredicate after, String schema, String slot, String improvement, int improvementLevel, Capability capability, int capabilityLevel) {
        super(trigger.getId(), playerCondition);
        this.before = before;
        this.after = after;
        this.schema = schema;
        this.slot = slot;
        this.improvement = improvement;
        this.improvementLevel = improvementLevel;
        this.capability = capability;
        this.capabilityLevel = capabilityLevel;
    }

    public static void trigger(ServerPlayerEntity player, ItemStack before, ItemStack after, String schema, String slot, String improvement,
            int improvementLevel, Capability capability, int capabilityLevel) {
        trigger.fulfillCriterion(player,
                criterion -> criterion.test(before, after, schema, slot, improvement, improvementLevel, capability, capabilityLevel));
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

    private static ImprovementCraftCriterion deserialize(JsonObject json, EntityPredicate.AndPredicate entityPredicate, ConditionArrayParser conditionsParser) {
        return new ImprovementCraftCriterion(entityPredicate,
                JsonOptional.field(json, "before")
                        .map(ItemPredicate::deserialize)
                        .orElse(null),
                JsonOptional.field(json, "after")
                        .map(ItemPredicate::deserialize)
                        .orElse(null),
                JsonOptional.field(json, "schema")
                        .map(JsonElement::getAsString)
                        .orElse(null),
                JsonOptional.field(json, "slot")
                        .map(JsonElement::getAsString)
                        .orElse(null),
                JsonOptional.field(json, "improvement")
                        .map(JsonElement::getAsString)
                        .orElse(null),
                JsonOptional.field(json, "improvementLevel")
                        .map(JsonElement::getAsInt)
                        .orElse(-1),
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
