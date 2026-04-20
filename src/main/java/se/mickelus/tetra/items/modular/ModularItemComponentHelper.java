package se.mickelus.tetra.items.modular;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Tool;
import se.mickelus.tetra.compat.bettercombat.BetterCombatCompat;

import java.util.Objects;

public final class ModularItemComponentHelper {
    private ModularItemComponentHelper() {
    }

    public static void sync(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return;
        }

        syncToolComponent(itemStack);
        BetterCombatCompat.sync(itemStack);
    }

    private static void syncToolComponent(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof ItemModularHandheld item)) {
            return;
        }

        Tool expectedToolComponent = item.getDefaultToolComponent(itemStack);
        Tool currentToolComponent = itemStack.get(DataComponents.TOOL);
        if (Objects.equals(expectedToolComponent, currentToolComponent)) {
            return;
        }

        if (expectedToolComponent == null) {
            itemStack.remove(DataComponents.TOOL);
        } else {
            itemStack.set(DataComponents.TOOL, expectedToolComponent);
        }
    }
}
