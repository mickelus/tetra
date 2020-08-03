package se.mickelus.tetra.module;

import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.GrindstoneContainer;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.util.CastOptional;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Removes improvements from tetra items that go through a grindstone, to avoid duping the xp players get from disenchanting.
 * Hacky hack, would be cool to have events to hook into this.
 */
public class DisenchantmentHandler implements IContainerListener {


    // removal of listener not possible due to that being client only, and there's not really a reason to remove the listener anyway
    @SubscribeEvent
    public void onPlayerContainerOpen(PlayerContainerEvent.Open event) {
        CastOptional.cast(event.getContainer(), GrindstoneContainer.class)
                .ifPresent(container -> container.addListener(this));
    }

    @Override
    public void sendAllContents(Container container, NonNullList<ItemStack> itemsList) { }

    @Override
    public void sendSlotContents(Container container, int slot, ItemStack itemStack) {
        if (slot == 2 && itemStack.getItem() instanceof ModularItem) {
            ItemStack copy = itemStack.copy();
            CastOptional.cast(itemStack.getItem(), ModularItem.class)
                    .map(item -> Arrays.stream(item.getMajorModules(itemStack)))
                    .orElseGet(Stream::empty)
                    .filter(Objects::nonNull)
                    .forEach(module -> module.removeEnchantments(itemStack));

            container.putStackInSlot(slot, itemStack);
        }
    }

    @Override
    public void sendWindowProperty(Container container, int varToUpdate, int newValue) { }
}
