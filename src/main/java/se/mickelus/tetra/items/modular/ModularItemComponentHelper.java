package se.mickelus.tetra.items.modular;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Tool;

import java.util.Objects;

public final class ModularItemComponentHelper {
    private ModularItemComponentHelper() {
    }

    public static void sync(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return;
        }

        syncToolComponent(itemStack);
        syncMaxDamageComponent(itemStack);
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

    // Vanilla 1.21 reads MAX_DAMAGE off the component, ignoring Item#getMaxDamage(ItemStack).
    // NeoForge further patched ItemStack#isDamageableItem to require BOTH MAX_DAMAGE and DAMAGE
    // components present (so isDamageableItem returns false until DAMAGE is initialized, which
    // gates hurtAndBreak entirely). Mirror Tetra's per-stack max-damage override into the
    // component AND seed DAMAGE = 0 if absent so vanilla durability code works for modular items.
    private static void syncMaxDamageComponent(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof IModularItem)) {
            return;
        }

        int expectedMax = itemStack.getItem().getMaxDamage(itemStack);
        Integer currentMax = itemStack.get(DataComponents.MAX_DAMAGE);

        if (expectedMax <= 0) {
            if (currentMax != null) {
                itemStack.remove(DataComponents.MAX_DAMAGE);
                itemStack.remove(DataComponents.DAMAGE);
            }
            return;
        }

        if (currentMax == null || currentMax != expectedMax) {
            itemStack.set(DataComponents.MAX_DAMAGE, expectedMax);
        }

        // NeoForge's isDamageableItem also requires the DAMAGE component to be present.
        // Initialize to 0 if absent so hurtAndBreak doesn't silently no-op.
        if (!itemStack.has(DataComponents.DAMAGE)) {
            itemStack.set(DataComponents.DAMAGE, 0);
        }

        // Workbench upgrades can lower max durability below the current damage value; clamp.
        if (itemStack.getDamageValue() > expectedMax) {
            itemStack.setDamageValue(expectedMax);
        }
    }
}
