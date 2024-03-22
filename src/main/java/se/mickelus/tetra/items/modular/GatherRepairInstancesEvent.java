package se.mickelus.tetra.items.modular;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;
import se.mickelus.tetra.module.schematic.RepairInstance;

public class GatherRepairInstancesEvent extends Event {
    public final ItemStack itemStack;
    public final RepairInstance[] initialInstances;
    public RepairInstance[] instances;

    public GatherRepairInstancesEvent(ItemStack itemStack, RepairInstance[] instances) {
        this.itemStack = itemStack;

        this.initialInstances = instances;
        this.instances = instances;
    }
}
