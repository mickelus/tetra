package se.mickelus.tetra.blocks.forged.container;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
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
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.mgui.gui.ToggleableSlot;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.network.PacketHandler;

import java.util.Arrays;


public class ForgedContainerContainer extends Container {
    @ObjectHolder(TetraMod.MOD_ID + ":" + ForgedContainerBlock.unlocalizedName)
    public static ContainerType<ForgedContainerContainer> type;

    private ForgedContainerTile tile;

    private ToggleableSlot[][] compartmentSlots;
    private int currentCompartment = 0;

    public ForgedContainerContainer(int windowId, ForgedContainerTile tile, IInventory playerInventory, PlayerEntity player) {
        super(ForgedContainerContainer.type, windowId);
        this.tile = tile;

        // material inventory
        tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(handler -> {
            compartmentSlots = new ToggleableSlot[ForgedContainerTile.compartmentCount][];
            for (int i = 0; i < compartmentSlots.length; i++) {
                compartmentSlots[i] = new ToggleableSlot[ForgedContainerTile.compartmentSize];
                int offset = i * ForgedContainerTile.compartmentSize;
                for (int j = 0; j < 6; j++) {
                    for (int k = 0; k < 9; k++) {
                        int index = j * 9 + k;
                        compartmentSlots[i][index] = new ToggleableSlot(handler, index + offset, k * 17 + 12, j * 17);
                        compartmentSlots[i][index].toggle(i == 0);
                        addSlot(compartmentSlots[i][index]);
                    }
                }
            }
        });

        IItemHandler playerInventoryHandler = new InvWrapper(playerInventory);

        // player inventory
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlot(new SlotItemHandler(playerInventoryHandler, i * 9 + j + 9, j * 17 + 12, i * 17 + 116));
            }
        }

        // player toolbar
        for (int i = 0; i < 9; i++) {
            addSlot(new SlotItemHandler(playerInventoryHandler, i, i * 17 + 12, 171));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static ForgedContainerContainer create(int windowId, BlockPos pos, PlayerInventory inv) {
        ForgedContainerTile te = (ForgedContainerTile) Minecraft.getInstance().world.getTileEntity(pos);
        return new ForgedContainerContainer(windowId, te, inv, Minecraft.getInstance().player);
    }

    public void changeCompartment(int compartmentIndex) {
        currentCompartment = compartmentIndex;
        for (int i = 0; i < compartmentSlots.length; i++) {
            boolean enabled = i == compartmentIndex;
            Arrays.stream(compartmentSlots[i]).forEach(slot -> slot.toggle(enabled));
        }

        if (tile.getWorld().isRemote) {
            PacketHandler.sendToServer(new ChangeCompartmentPacket(compartmentIndex));
        }
    }

    private int getSlots() {
        return tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                .map(IItemHandler::getSlots)
                .orElse(0);
    }

    /**
     * Take a stack from the specified inventory slot.
     */
    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int index) {
        ItemStack resultStack = ItemStack.EMPTY;

        Slot slot = inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack slotStack = slot.getStack();

            resultStack = slotStack.copy();

            if (index < getSlots()) {
                if (!mergeItemStack(slotStack, getSlots(), this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!mergeItemStack(slotStack, currentCompartment * ForgedContainerTile.compartmentSize,
                    ( currentCompartment + 1) * ForgedContainerTile.compartmentSize, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return resultStack;
    }

    public boolean canInteractWith(PlayerEntity playerIn) {
        return isWithinUsableDistance(IWorldPosCallable.of(tile.getWorld(), tile.getPos()), playerIn, ForgedContainerBlock.instance);
    }

    public ForgedContainerTile getTile() {
        return tile;
    }
}
