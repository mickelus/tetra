package se.mickelus.tetra.module.schematic.requirement;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import se.mickelus.mutil.Perks;
import se.mickelus.tetra.module.schematic.CraftingContext;

import java.util.List;

public class PerkRequrement implements CraftingRequirement {
    String perk;
    IntegerPredicate level;

    public PerkRequrement(String perk, IntegerPredicate level) {
        this.perk = perk;
        this.level = level;
    }

    @Override
    public boolean test(CraftingContext context) {
        if (context.world != null && context.world.isClientSide()) {
            return level.test(getValue(this.perk));
        }
        return true;
    }

    private int getValue(String perk) {
        Perks.Data perkData = Perks.getData();
        switch (perk) {
            case "support":
                return perkData.support;
            case "contribute":
                return perkData.contribute;
            case "community":
                return perkData.community;
            case "moderate":
                return perkData.moderate;
        }
        return 0;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    @Nullable
    public List<Component> getDescription() {
        return List.of(Component.literal(I18n.get("tetra.holo.perk_requirement", perk, level.getDescription(I18n.get("tetra.holo.perk_requirement.level_label")))));
    }
}
