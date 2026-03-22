package se.mickelus.tetra.module.schematic.requirement;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.schematic.CraftingContext;

import java.util.List;

public class AcceptsImprovementRequirement implements CraftingRequirement {
    String improvement;
    Integer level;

    @Override
    public boolean test(CraftingContext context) {
        if (context.targetMajorModule != null) {
            if (level != null && !context.targetMajorModule.acceptsImprovementLevel(improvement, level)) {
                return false;
            }
            return context.targetMajorModule.acceptsImprovement(improvement);
        }
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    @Nullable
    public List<Component> getDescription() {
        return List.of(Component.literal(I18n.get("tetra.holo.accepts_improvement_requirement", IModularItem.getImprovementName(improvement, level != null ? level : 0, null))));
    }
}
