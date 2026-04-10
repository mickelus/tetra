package se.mickelus.tetra.blocks.workbench;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import se.mickelus.tetra.compat.forge.items.IItemHandler;
import se.mickelus.tetra.compat.forge.items.SlotItemHandler;
import se.mickelus.tetra.compat.forge.items.wrapper.InvWrapper;
import se.mickelus.tetra.compat.forge.registries.RegistryObject;
import se.mickelus.mutil.gui.ToggleableSlot;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
public class WorkbenchContainer extends AbstractContainerMenu {
    public static RegistryObject<MenuType<WorkbenchContainer>> containerType;

    private static final int slotDetailX = 48;
    private static final int slotDetailY = 102;
    private static final int schematicMaterialBaseX = 136;
    private static final int schematicMaterialBaseY = 5;
    private static final int schematicMaterialItemOffsetX = 10;
    private static final int schematicMaterialItemOffsetY = 1;

    private final WorkbenchTile workbench;

    private ToggleableSlot[] materialSlots = new ToggleableSlot[0];

    public WorkbenchContainer(int windowId, WorkbenchTile workbench, Container playerInventory, Player player) {
        super(containerType.get(), windowId);
        this.workbench = workbench;

        // material inventory
        var handler = workbench.getItemHandler(null);
        if (handler != null) {
            addSlot(new SlotItemHandler(handler, 0, 152, 58));

            materialSlots = new ToggleableSlot[3];
            for (int i = 0; i < materialSlots.length; i++) {
                materialSlots[i] = new ToggleableSlot(handler, i + 1, getMaterialSlotX(i, materialSlots.length), getMaterialSlotY());
                addSlot(materialSlots[i]);
            }
        }

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
    public static WorkbenchContainer create(int windowId, BlockPos pos, Inventory inv) {
        WorkbenchTile te = (WorkbenchTile) Minecraft.getInstance().level.getBlockEntity(pos);
        return new WorkbenchContainer(windowId, te, inv, Minecraft.getInstance().player);
    }

    private int getSlots() {
        IItemHandler handler = workbench.getItemHandler(null);
        return handler != null ? handler.getSlots() : 0;
    }

    @Override
    public boolean stillValid(Player player) {
        BlockPos pos = workbench.getBlockPos();

        // based on Container.isWithinUsableDistance but with more generic blockcheck
        if (workbench.getLevel().getBlockState(workbench.getBlockPos()).getBlock() instanceof AbstractWorkbenchBlock) {
            return player.distanceToSqr((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D) <= 64.0D;
        }

        return false;
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
            } else if (!moveItemStackTo(slotStack, 0, getSlots(), false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        workbench.setChanged();
        return resultStack;
    }

    public void updateSlots() {
        int numMaterialSlots = Optional.ofNullable(workbench.getCurrentSchematic())
                .map(UpgradeSchematic::getNumMaterialSlots)
                .orElse(0);

        for (int i = 0; i < materialSlots.length; i++) {
            materialSlots[i].setPosition(getMaterialSlotX(i, numMaterialSlots), getMaterialSlotY());
            materialSlots[i].toggle(i < numMaterialSlots);
        }
    }

    public static int getMaterialSlotGuiX(int index, int numMaterialSlots) {
        return schematicMaterialBaseX + getMaterialSlotOffsetX(index, numMaterialSlots);
    }

    public static int getMaterialSlotX(int index, int numMaterialSlots) {
        return slotDetailX + getMaterialSlotGuiX(index, numMaterialSlots) + schematicMaterialItemOffsetX;
    }

    public static int getMaterialSlotY() {
        return slotDetailY + schematicMaterialBaseY + schematicMaterialItemOffsetY;
    }

    public static int getMaterialSlotOffsetX(int index, int numMaterialSlots) {
        if (numMaterialSlots == 2) {
            return 11 + 32 * index;
        }

        return 28 * index;
    }

    public WorkbenchTile getTileEntity() {
        return workbench;
    }
}
