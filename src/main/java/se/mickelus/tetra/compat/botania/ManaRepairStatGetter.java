package se.mickelus.tetra.compat.botania;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.gui.stats.getter.StatGetterEffectEfficiency;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
public class ManaRepairStatGetter extends StatGetterEffectEfficiency {
    public ManaRepairStatGetter() {
        super(ManaRepair.effect, 1);
    }

    @Override
    public double getValue(Player player, ItemStack itemStack) {
        return Optional.of(super.getValue(player, itemStack))
                .filter(val -> val != 0)
                .map(val -> 1 / val)
                .orElse(0d);
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot) {
        return Optional.of(super.getValue(player, itemStack, slot))
                .filter(val -> val != 0)
                .map(val -> 1 / val)
                .orElse(0d);
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot, String improvement) {
        return Optional.of(super.getValue(player, itemStack, slot, improvement))
                .filter(val -> val != 0)
                .map(val -> 1 / val)
                .orElse(0d);
    }
}
