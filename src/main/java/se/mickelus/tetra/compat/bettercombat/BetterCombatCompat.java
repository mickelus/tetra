package se.mickelus.tetra.compat.bettercombat;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.items.modular.impl.ModularBladedItem;
import se.mickelus.tetra.items.modular.impl.ModularDoubleHeadedItem;
import se.mickelus.tetra.items.modular.impl.ModularSingleHeadedItem;
import se.mickelus.tetra.items.modular.impl.bow.ModularBowItem;
import se.mickelus.tetra.items.modular.impl.crossbow.AbstractModularCrossbowItem;
import se.mickelus.tetra.module.ItemModule;

import javax.annotation.Nullable;
import java.util.Objects;

public final class BetterCombatCompat {
    private static final ResourceLocation presetComponentId = ResourceLocation.fromNamespaceAndPath("bettercombat", "preset_id");

    private static final ResourceLocation swordPreset = ResourceLocation.fromNamespaceAndPath("bettercombat", "sword");
    private static final ResourceLocation daggerPreset = ResourceLocation.fromNamespaceAndPath("bettercombat", "dagger");
    private static final ResourceLocation cutlassPreset = ResourceLocation.fromNamespaceAndPath("bettercombat", "cutlass");
    private static final ResourceLocation spearPreset = ResourceLocation.fromNamespaceAndPath("bettercombat", "spear");
    private static final ResourceLocation tridentPreset = ResourceLocation.fromNamespaceAndPath("bettercombat", "trident");
    private static final ResourceLocation pickaxePreset = ResourceLocation.fromNamespaceAndPath("bettercombat", "pickaxe");
    private static final ResourceLocation hammerPreset = ResourceLocation.fromNamespaceAndPath("bettercombat", "hammer");
    private static final ResourceLocation doubleAxePreset = ResourceLocation.fromNamespaceAndPath("bettercombat", "double_axe");
    private static final ResourceLocation sicklePreset = ResourceLocation.fromNamespaceAndPath("bettercombat", "sickle");
    private static final ResourceLocation clawPreset = ResourceLocation.fromNamespaceAndPath("bettercombat", "claw");
    private static final ResourceLocation bowPreset = ResourceLocation.fromNamespaceAndPath("bettercombat", "bow_two_handed_heavy");
    private static final ResourceLocation crossbowPreset = ResourceLocation.fromNamespaceAndPath("bettercombat", "crossbow_two_handed_heavy");

    private static DataComponentType<ResourceLocation> presetComponentType;

    private BetterCombatCompat() {
    }

    public static void sync(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return;
        }

        syncAttributeModifiers(itemStack);
        syncPreset(itemStack);
    }

    private static void syncAttributeModifiers(ItemStack itemStack) {
        if (!shouldSyncAttributeModifiers(itemStack)) {
            return;
        }

        ItemAttributeModifiers expectedModifiers = itemStack.getItem().getDefaultAttributeModifiers(itemStack);
        ItemAttributeModifiers currentModifiers = itemStack.get(DataComponents.ATTRIBUTE_MODIFIERS);

        if (expectedModifiers.modifiers().isEmpty()) {
            if (currentModifiers != null) {
                itemStack.remove(DataComponents.ATTRIBUTE_MODIFIERS);
            }
            return;
        }

        if (!Objects.equals(expectedModifiers, currentModifiers)) {
            itemStack.set(DataComponents.ATTRIBUTE_MODIFIERS, expectedModifiers);
        }
    }

    private static boolean shouldSyncAttributeModifiers(ItemStack itemStack) {
        Item item = itemStack.getItem();
        return item instanceof ItemModularHandheld
                || item instanceof ModularBowItem
                || item instanceof AbstractModularCrossbowItem;
    }

    private static void syncPreset(ItemStack itemStack) {
        DataComponentType<ResourceLocation> componentType = getPresetComponentType();
        if (componentType == null) {
            return;
        }

        ResourceLocation expectedPreset = getPreset(itemStack);
        ResourceLocation currentPreset = itemStack.get(componentType);

        if (Objects.equals(expectedPreset, currentPreset)) {
            return;
        }

        if (expectedPreset == null) {
            itemStack.remove(componentType);
        } else {
            itemStack.set(componentType, expectedPreset);
        }
    }

    @SuppressWarnings("unchecked")
    private static @Nullable DataComponentType<ResourceLocation> getPresetComponentType() {
        if (presetComponentType == null && BuiltInRegistries.DATA_COMPONENT_TYPE.containsKey(presetComponentId)) {
            presetComponentType = (DataComponentType<ResourceLocation>) BuiltInRegistries.DATA_COMPONENT_TYPE.get(presetComponentId);
        }

        return presetComponentType;
    }

    private static @Nullable ResourceLocation getPreset(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof IModularItem item)) {
            return null;
        }

        if (itemStack.getItem() instanceof ModularBladedItem) {
            return getSwordPreset(itemStack, item);
        }
        if (itemStack.getItem() instanceof ModularSingleHeadedItem) {
            return getSinglePreset(itemStack, item);
        }
        if (itemStack.getItem() instanceof ModularDoubleHeadedItem) {
            return getDoublePreset(itemStack, item);
        }
        if (itemStack.getItem() instanceof ModularBowItem) {
            return bowPreset;
        }
        if (itemStack.getItem() instanceof AbstractModularCrossbowItem) {
            return crossbowPreset;
        }

        return null;
    }

    private static ResourceLocation getSwordPreset(ItemStack itemStack, IModularItem item) {
        ItemModule blade = item.getModuleFromSlot(itemStack, ModularBladedItem.bladeKey);
        if (blade == null) {
            return swordPreset;
        }

        return switch (getModuleId(blade)) {
            case "sword/short_blade", "sword/throwing_knife" -> daggerPreset;
            case "sword/machete" -> cutlassPreset;
            default -> swordPreset;
        };
    }

    private static @Nullable ResourceLocation getSinglePreset(ItemStack itemStack, IModularItem item) {
        ItemModule head = item.getModuleFromSlot(itemStack, ModularSingleHeadedItem.headKey);
        if (head == null) {
            return null;
        }

        return switch (getModuleId(head)) {
            case "single/spearhead" -> spearPreset;
            case "single/trident" -> tridentPreset;
            case "single/earthpiercer", "single/unbound_earthpiercer" -> pickaxePreset;
            default -> null;
        };
    }

    private static @Nullable ResourceLocation getDoublePreset(ItemStack itemStack, IModularItem item) {
        ResourceLocation leftPreset = getDoubleHeadPreset(item.getModuleFromSlot(itemStack, ModularDoubleHeadedItem.headLeftKey));
        ResourceLocation rightPreset = getDoubleHeadPreset(item.getModuleFromSlot(itemStack, ModularDoubleHeadedItem.headRightKey));

        if (leftPreset == null) {
            return rightPreset;
        }
        if (rightPreset == null) {
            return leftPreset;
        }

        return leftPreset.equals(rightPreset) ? leftPreset : null;
    }

    private static @Nullable ResourceLocation getDoubleHeadPreset(@Nullable ItemModule module) {
        if (module == null) {
            return null;
        }

        return switch (getModuleId(module)) {
            case "double/basic_hammer" -> hammerPreset;
            case "double/basic_axe" -> doubleAxePreset;
            case "double/sickle" -> sicklePreset;
            case "double/claw" -> clawPreset;
            default -> null;
        };
    }

    private static String getModuleId(ItemModule module) {
        return module.getUnlocalizedName();
    }
}
