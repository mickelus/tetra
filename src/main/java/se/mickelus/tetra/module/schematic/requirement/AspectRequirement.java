package se.mickelus.tetra.module.schematic.requirement;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import se.mickelus.tetra.aspect.ItemAspect;
import se.mickelus.tetra.module.schematic.CraftingContext;

import java.util.List;
import java.util.Optional;

public class AspectRequirement implements CraftingRequirement {
    ItemAspect aspect;
    IntegerPredicate level;

    public AspectRequirement(ItemAspect aspect, IntegerPredicate level) {
        this.aspect = aspect;
        this.level = level;
    }

    @Override
    public boolean test(CraftingContext context) {
        return Optional.ofNullable(context.targetModule)
                .map(module -> module.getAspects(context.targetStack))
                .filter(aspects -> aspects.contains(aspect))
                .map(aspects -> level == null || level.test(aspects.getLevel(aspect)))
                .orElse(false);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    @Nullable
    public List<Component> getDescription() {
        if (level != null) {
            return List.of(Component.literal(I18n.get("tetra.holo.aspect_requirement_level", aspect.getLabel().getString(), level.getDescription(I18n.get("tetra.holo.aspect_requirement_level.level_label")))));
        }
        return List.of(Component.literal(I18n.get("tetra.holo.aspect_requirement", aspect.getLabel().getString())));
    }
}
