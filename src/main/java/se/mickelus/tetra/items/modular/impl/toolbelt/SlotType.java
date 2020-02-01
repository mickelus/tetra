package se.mickelus.tetra.items.modular.impl.toolbelt;

import se.mickelus.tetra.module.ItemEffect;

public enum SlotType {
    storage(ItemEffect.storageSlot),
    quiver(ItemEffect.quiverSlot),
    potion(ItemEffect.potionSlot),
    quick(ItemEffect.quickSlot);


    ItemEffect effect;

    SlotType(ItemEffect effect) {
        this.effect = effect;
    }
}
