package se.mickelus.tetra.module.schematic.requirement;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.schematic.CraftingContext;

import java.util.List;

public class HasImprovementRequirement implements CraftingRequirement {
    String improvement;
    IntegerPredicate level;

    @Override
    public boolean test(CraftingContext context) {
        if (context.targetMajorModule != null) {
            if (level != null && !level.test(context.targetMajorModule.getImprovementLevel(context.targetStack, improvement))) {
                return false;
            }
            return context.targetMajorModule.getImprovement(context.targetStack, improvement) != null;
        }
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    @Nullable
    public List<Component> getDescription() {
        if (level != null) {
            return List.of(Component.literal(I18n.get("tetra.holo.improvement_requirement_level", IModularItem.getImprovementName(improvement, 0), level.getDescription(I18n.get("tetra.holo.improvement_requirement_level.level_label")))));
        }
        return List.of(Component.literal(I18n.get("tetra.holo.improvement_requirement", IModularItem.getImprovementName(improvement, 0))));
    }
}
