package se.mickelus.tetra.advancements;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ToolAction;
import se.mickelus.tetra.util.JsonOptional;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
public class ModuleCraftCriterion extends AbstractCriterionTriggerInstance {
    private final ItemPredicate before;
    private final ItemPredicate after;

    private final String schematic;

    private final String slot;
    private final String module;
    private final String variant;

    private final ToolAction toolAction;
    private final MinMaxBounds.Ints toolLevel;

    public static final GenericTrigger<ModuleCraftCriterion> trigger = new GenericTrigger<>("tetra:craft_module", ModuleCraftCriterion::deserialize);

    public ModuleCraftCriterion(EntityPredicate.Composite playerCondition, ItemPredicate before, ItemPredicate after, String schematic, String slot, String module, String variant, ToolAction toolAction, MinMaxBounds.Ints toolLevel) {
        super(trigger.getId(), playerCondition);
        this.before = before;
        this.after = after;
        this.schematic = schematic;
        this.slot = slot;
        this.module = module;
        this.variant = variant;
        this.toolAction = toolAction;
        this.toolLevel = toolLevel;
    }

    public static void trigger(ServerPlayer player, ItemStack before, ItemStack after, String schematic, String slot, String module,
            String variant, ToolAction toolAction, int toolLevel) {
        trigger.fulfillCriterion(player, criterion -> criterion.test(before, after, schematic, slot, module, variant, toolAction,
                toolLevel));
    }

    public boolean test(ItemStack before, ItemStack after, String schematic, String slot, String module, String variant,
            ToolAction toolAction, int toolLevel) {
        if (this.before != null && !this.before.matches(before)) {
            return false;
        }

        if (this.after != null && !this.after.matches(after)) {
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

        if (this.toolAction != null && !this.toolAction.equals(toolAction)) {
            return false;
        }

        if (!this.toolLevel.matches(toolLevel)) {
            return false;
        }

        return true;
    }

    private static ModuleCraftCriterion deserialize(JsonObject json, EntityPredicate.Composite entityPredicate, DeserializationContext conditionsParser) {
        return new ModuleCraftCriterion(entityPredicate,
                JsonOptional.field(json, "before")
                        .map(ItemPredicate::fromJson)
                        .orElse(null),
                JsonOptional.field(json, "after")
                        .map(ItemPredicate::fromJson)
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
                JsonOptional.field(json, "tool")
                        .map(JsonElement::getAsString)
                        .map(ToolAction::get)
                        .orElse(null),
                JsonOptional.field(json, "toolLevel")
                        .map(MinMaxBounds.Ints::fromJson)
                        .orElse(MinMaxBounds.Ints.ANY));
    }
}
