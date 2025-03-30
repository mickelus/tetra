package se.mickelus.tetra.module.schematic.requirement;

import com.google.common.collect.ImmutableList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import se.mickelus.tetra.module.schematic.CraftingContext;

import java.util.Arrays;
import java.util.List;

public class OrRequirement implements CraftingRequirement {

    CraftingRequirement[] requirements;

    @Override
    public boolean test(CraftingContext context) {
        return Arrays.stream(requirements).anyMatch(requirement -> requirement.test(context));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    @Nullable
    public List<Component> getDescription() {
        ImmutableList.Builder<Component> builder = ImmutableList.builder();
        builder.add(Component.literal(I18n.get("tetra.holo.or_requirement")).withStyle(ChatFormatting.GRAY));
        for (int i = 0; i < requirements.length; i++) {
            List<Component> description = requirements[i].getDescription();
            if (description != null) {
                for (int j = 0; j < description.size(); j++) {
                    if (j == 0) {
                        builder.add(Component.literal(i == requirements.length - 1 ? " §8\u2514§r " : " §8\u251c§r ").append(description.get(j)));
                    } else {
                        builder.add(Component.literal(" §8\u2502§r ").append(description.get(j)));
                    }
                }
            }
        }
        return builder.build();
    }
}
