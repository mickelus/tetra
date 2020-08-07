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
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.util.JsonOptional;

public class ModuleCraftCriterion extends CriterionInstance {
    private final ItemPredicate before;
    private final ItemPredicate after;

    private final String schematic;

    private final String slot;
    private final String module;
    private final String variant;

    private final Capability capability;
    private final int capabilityLevel;

    public static final GenericTrigger<ModuleCraftCriterion> trigger = new GenericTrigger<>("tetra:craft_module", ModuleCraftCriterion::deserialize);

    public ModuleCraftCriterion(EntityPredicate.AndPredicate playerCondition, ItemPredicate before, ItemPredicate after, String schematic, String slot, String module, String variant, Capability capability, int capabilityLevel) {
        super(trigger.getId(), playerCondition);
        this.before = before;
        this.after = after;
        this.schematic = schematic;
        this.slot = slot;
        this.module = module;
        this.variant = variant;
        this.capability = capability;
        this.capabilityLevel = capabilityLevel;
    }

    public static void trigger(ServerPlayerEntity player, ItemStack before, ItemStack after, String schematic, String slot, String module,
            String variant, Capability capability, int capabilityLevel) {
        trigger.fulfillCriterion(player, criterion -> criterion.test(before, after, schematic, slot, module, variant, capability,
                capabilityLevel));
    }

    public boolean test(ItemStack before, ItemStack after, String schematic, String slot, String module, String variant,
            Capability capability, int capabilityLevel) {
        if (this.before != null && !this.before.test(before)) {
            return false;
        }

        if (this.after != null && !this.after.test(after)) {
            return false;
        }

        if (this.schematic != null && !this.schematic.equals(schematic)) {
            return false;
        }

        if (this.slot != null && !this.slot.equals(slot)) {
            return false;
        }

        if (this.module != null && !this.module.equals(module)) {
            return false;
        }

        if (this.variant != null && !this.variant.equals(variant)) {
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

    private static ModuleCraftCriterion deserialize(JsonObject json, EntityPredicate.AndPredicate entityPredicate, ConditionArrayParser conditionsParser) {
        return new ModuleCraftCriterion(entityPredicate,
                JsonOptional.field(json, "before")
                        .map(ItemPredicate::deserialize)
                        .orElse(null),
                JsonOptional.field(json, "after")
                        .map(ItemPredicate::deserialize)
                        .orElse(null),
                JsonOptional.field(json, "schematic")
                        .map(JsonElement::getAsString)
                        .orElse(null),
                JsonOptional.field(json, "slot")
                        .map(JsonElement::getAsString)
                        .orElse(null),
                JsonOptional.field(json, "module")
                        .map(JsonElement::getAsString)
                        .orElse(null),
                JsonOptional.field(json, "variant")
                        .map(JsonElement::getAsString)
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
