package se.mickelus.tetra.blocks.forged.container;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.mgui.gui.ToggleableSlot;
import se.mickelus.tetra.TetraMod;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
@ParametersAreNonnullByDefault
public class ForgedContainerContainer extends AbstractContainerMenu {
    @ObjectHolder(TetraMod.MOD_ID + ":" + ForgedContainerBlock.unlocalizedName)
    public static MenuType<ForgedContainerContainer> type;

    private ForgedContainerTile tile;

    private ToggleableSlot[][] compartmentSlots;
    private int currentCompartment = 0;

    public ForgedContainerContainer(int windowId, ForgedContainerTile tile, Container playerInventory, Player player) {
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
    public static ForgedContainerContainer create(int windowId, BlockPos pos, Inventory inv) {
        ForgedContainerTile te = (ForgedContainerTile) Minecraft.getInstance().level.getBlockEntity(pos);
        return new ForgedContainerContainer(windowId, te, inv, Minecraft.getInstance().player);
    }

    public void changeCompartment(int compartmentIndex) {
        currentCompartment = compartmentIndex;
        for (int i = 0; i < compartmentSlots.length; i++) {
            boolean enabled = i == compartmentIndex;
            Arrays.stream(compartmentSlots[i]).forEach(slot -> slot.toggle(enabled));
        }

        if (tile.getLevel().isClientSide) {
            TetraMod.packetHandler.sendToServer(new ChangeCompartmentPacket(compartmentIndex));
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
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack resultStack = ItemStack.EMPTY;

        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();

            resultStack = slotStack.copy();

            if (index < getSlots()) {
                if (!moveItemStackTo(slotStack, getSlots(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(slotStack, currentCompartment * ForgedContainerTile.compartmentSize,
                    ( currentCompartment + 1) * ForgedContainerTile.compartmentSize, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return resultStack;
    }

    public boolean stillValid(Player playerIn) {
        return stillValid(ContainerLevelAccess.create(tile.getLevel(), tile.getBlockPos()), playerIn, ForgedContainerBlock.instance);
    }

    public ForgedContainerTile getTile() {
        return tile;
    }
}
