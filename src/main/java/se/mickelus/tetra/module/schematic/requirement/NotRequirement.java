package se.mickelus.tetra.module.schematic.requirement;

import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import se.mickelus.tetra.module.schematic.CraftingContext;

import java.util.List;
import java.util.stream.Stream;

public class NotRequirement implements CraftingRequirement {

    CraftingRequirement requirement;

    @Override
    public boolean test(CraftingContext context) {
        return !requirement.test(context);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    @Nullable
    public List<Component> getDescription() {
        List<Component> childDescription = requirement.getDescription();
        if (childDescription != null) {
            return Stream.concat(
                    childDescription.stream().limit(1).map(component -> Component.translatable("tetra.holo.not_requirement", component)),
                    childDescription.stream().skip(1)
            ).toList();
        }
        return null;
    }
}
