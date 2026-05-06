package se.mickelus.tetra.module.schematic.requirement;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import se.mickelus.tetra.module.schematic.CraftingContext;

import java.util.Arrays;
import java.util.List;

public class LockedRequirement implements CraftingRequirement {
    public ResourceLocation key;

    @Override
    public boolean test(CraftingContext context) {
        return Arrays.asList(context.unlocks).contains(key);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    @Nullable
    public List<Component> getDescription() {
        if (I18n.exists(key.toLanguageKey("tetra.unlock", "description"))) {
            return List.of(Component.literal(I18n.get(key.toLanguageKey("tetra.unlock", "description"))));
        }
        return List.of(Component.literal(I18n.get("tetra.holo.unlock_requirement", I18n.get(key.toLanguageKey("tetra.unlock", "name")))));
    }
}
