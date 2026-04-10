package se.mickelus.tetra.module.schematic.requirement;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import se.mickelus.tetra.module.schematic.CraftingContext;

import java.util.List;

public class SlotRequirement implements CraftingRequirement {
    String slot;

    @Override
    public boolean test(CraftingContext context) {
        return slot.equals(context.slot);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    @Nullable
    public List<Component> getDescription() {
        return List.of(Component.literal(I18n.get("tetra.holo.slot_requirement", I18n.get("tetra.slot." + slot))));
    }
}
