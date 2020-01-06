package se.mickelus.tetra.blocks.workbench;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import se.mickelus.mgui.gui.ToggleableSlot;
import se.mickelus.tetra.module.schema.UpgradeSchema;


public class WorkbenchContainer extends Container {
    private WorkbenchTile workbench;

    private ToggleableSlot[] materialSlots = new ToggleableSlot[0];

    public WorkbenchContainer(int windowId, WorkbenchTile workbench, IInventory playerInventory, PlayerEntity player) {
        super(WorkbenchTile.containerType, windowId);
        this.workbench = workbench;

        // material inventory
        workbench.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(handler -> {
            addSlot(new SlotItemHandler(handler, 0, 152, 58));

            materialSlots = new ToggleableSlot[3];
            for (int i = 0; i < materialSlots.length; i++) {
                materialSlots[i] = new ToggleableSlot(handler, i + 1, 167, 107 + 18 * i);
                addSlot(materialSlots[i]);
            }
        });

        IItemHandler playerInventoryHandler = new InvWrapper(playerInventory);

        // player inventory
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 3; y++) {
                addSlot(new SlotItemHandler(playerInventoryHandler, y * 9 + x + 9, x * 17 + 84, y * 17 + 166));
            }
        }

        // player toolbar
        for (int i = 0; i < 9; i++) {
            addSlot(new SlotItemHandler(playerInventoryHandler, i, i * 17 + 84, 221));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static WorkbenchContainer create(int windowId, BlockPos pos, PlayerInventory inv) {
        WorkbenchTile te = (WorkbenchTile) Minecraft.getInstance().world.getTileEntity(pos);
        return new WorkbenchContainer(windowId, te, inv, Minecraft.getInstance().player);
    }

    private int getSlots() {
        return workbench.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                .map(IItemHandler::getSlots)
                .orElse(0);
    }

    @Override
    public boolean canInteractWith(PlayerEntity player) {
        BlockPos pos = workbench.getPos();

        // based on Container.isWithinUsableDistance but with more generic blockcheck
        if (workbench.getWorld().getBlockState(workbench.getPos()).getBlock() instanceof AbstractWorkbenchBlock) {
            return player.getDistanceSq((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D) <= 64.0D;
        }

        return false;
    }

    /**
     * Take a stack from the specified inventory slot.
     */
    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (index < getSlots()) {
                if (!this.mergeItemStack(itemstack1, getSlots(), this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(itemstack1, 0, getSlots(), false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return itemstack;
    }

    public void updateSlots() {
        UpgradeSchema currentSchema = workbench.getCurrentSchema();
        int numMaterialSlots = 0;

        if (currentSchema != null) {
            numMaterialSlots = currentSchema.getNumMaterialSlots();
        }

        for (int i = 0; i < materialSlots.length; i++) {
            materialSlots[i].toggle(i < numMaterialSlots);
        }
    }

    public WorkbenchTile getTileEntity() {
        return workbench;
    }
}
