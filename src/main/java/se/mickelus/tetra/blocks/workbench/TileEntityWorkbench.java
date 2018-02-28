package se.mickelus.tetra.blocks.workbench;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import se.mickelus.tetra.blocks.workbench.action.BreakAction;
import se.mickelus.tetra.blocks.workbench.action.WorkbenchAction;
import se.mickelus.tetra.blocks.workbench.action.WorkbenchActionPacket;
import se.mickelus.tetra.capabilities.CapabilityHelper;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.schema.UpgradeSchema;
import se.mickelus.tetra.network.PacketPipeline;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TileEntityWorkbench extends TileEntity implements IInventory {

    private static final String STACKS_KEY = "stacks";
    private static final String SLOT_KEY = "slot";
    private static final String SCHEMA_KEY = "schema";

    private NonNullList<ItemStack> stacks;

    private ItemStack previousTarget = ItemStack.EMPTY;
    private UpgradeSchema currentSchema;

    private Map<String, Runnable> changeListeners;

    private static final WorkbenchAction[] actions = new WorkbenchAction[] {
            new BreakAction()
    };


    public TileEntityWorkbench() {
        stacks = NonNullList.withSize(4, ItemStack.EMPTY);
        changeListeners = new HashMap<>();
    }

    public WorkbenchAction[] getAvailableActions() {
        ItemStack itemStack = getTargetItemStack();
        return Arrays.stream(actions)
                .filter(action -> action.canPerformOn(itemStack))
                .toArray(WorkbenchAction[]::new);
    }

    public boolean canPerformAction(String actionKey) {
        ItemStack itemStack = getTargetItemStack();
        return Arrays.stream(actions)
                .filter(action -> action.getKey().equals(actionKey))
                .anyMatch(action -> action.canPerformOn(itemStack));
    }

    public void performAction(EntityPlayer player, String actionKey) {
        if (world.isRemote) {
            PacketPipeline.instance.sendToServer(new WorkbenchActionPacket(pos, actionKey));
            return;
        }

        ItemStack itemStack = getTargetItemStack();
        Arrays.stream(actions)
                .filter(action -> action.getKey().equals(actionKey))
                .findFirst()
                .filter(action -> action.canPerformOn(itemStack))
                .filter(action -> checkActionCapabilities(player, action, itemStack))
                .ifPresent(action -> action.perform(player, itemStack, this));
    }

    private boolean checkActionCapabilities(EntityPlayer player, WorkbenchAction action, ItemStack itemStack) {
        return Arrays.stream(action.getRequiredCapabilitiesFor(itemStack))
                .allMatch(capability -> CapabilityHelper.getCapabilityLevel(player, capability) >= action.getCapabilityLevel(itemStack, capability));
    }

    public UpgradeSchema getCurrentSchema() {
        return currentSchema;
    }

    public void setCurrentSchema(UpgradeSchema schema) {
        setCurrentSchema(schema, null);
    }

    public void setCurrentSchema(UpgradeSchema schema, EntityPlayer player) {
        // todo: inventory change hack, better solution?
        if (!world.isRemote && schema == null && player != null) {
            emptyMaterialSlots(player);
        }

        this.currentSchema = schema;

        sync();

        if (world.isRemote) {
            changeListeners.values().forEach(Runnable::run);
        }
    }

    private void sync() {
        if (world.isRemote) {
            PacketPipeline.instance.sendToServer(new UpdateWorkbenchPacket(pos, currentSchema));
        } else {
            world.notifyBlockUpdate(pos, getBlockType().getDefaultState(), getBlockType().getDefaultState(), 3);
            markDirty();
        }
    }

    public ItemStack getTargetItemStack() {
        ItemStack stack = getStackInSlot(0);

        if (stack == null) {
            return ItemStack.EMPTY;
        }

        ItemStack placeholder = ItemUpgradeRegistry.instance.getPlaceholder(stack);
        if (!placeholder.isEmpty()) {
            return placeholder;
        }

        return stack;
    }

    public ItemStack[] getMaterials() {
        return stacks.subList(1, 4).toArray(new ItemStack[3]);
    }

    public void initiateCrafting(EntityPlayer player) {
        if (world.isRemote) {
            PacketPipeline.instance.sendToServer(new CraftWorkbenchPacket(pos));
        }

        craft(player);

        sync();
    }

    public void craft(EntityPlayer player) {
        ItemStack itemStack = getTargetItemStack();

        if (currentSchema != null && currentSchema.canApplyUpgrade(player, itemStack, getMaterials())) {
            itemStack = currentSchema.applyUpgrade(itemStack, getMaterials(), true, player);
        }


        setInventorySlotContents(0, itemStack);
    }

    public void addChangeListener(String key, Runnable runnable) {
        changeListeners.put(key, runnable);
    }

    public void removeChangeListener(String key) {
        changeListeners.remove(key);
    }

    @Override
    public void markDirty() {
        super.markDirty();

        if (this.world.isRemote) {
            // TODO: this is called several times everytime a change occurs

            changeListeners.values().forEach(Runnable::run);
        }
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
    }

    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        this.readFromNBT(packet.getNbtCompound());
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey(STACKS_KEY)) {
            NBTTagList tagList = compound.getTagList(STACKS_KEY, 10);

            for (int i = 0; i < tagList.tagCount(); ++i) {
                NBTTagCompound nbttagcompound = tagList.getCompoundTagAt(i);
                int slot = nbttagcompound.getByte(SLOT_KEY) & 255;

                if (slot < this.stacks.size()) {
                    this.stacks.set(slot, new ItemStack(nbttagcompound));
                }
            }
        }

        String schemaKey = compound.getString(SCHEMA_KEY);
        currentSchema = ItemUpgradeRegistry.instance.getSchema(schemaKey);

        // todo : due to the null check perhaps this is not the right place to do this
        if (this.world != null && this.world.isRemote) {
            changeListeners.values().forEach(Runnable::run);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < this.stacks.size(); ++i) {
            if (!this.stacks.get(i).isEmpty()) {
                NBTTagCompound nbttagcompound = new NBTTagCompound();

                nbttagcompound.setByte(SLOT_KEY, (byte) i);
                this.stacks.get(i).writeToNBT(nbttagcompound);

                nbttaglist.appendTag(nbttagcompound);
            }
        }

        compound.setTag(STACKS_KEY, nbttaglist);

        if (currentSchema != null) {
            compound.setString(SCHEMA_KEY, currentSchema.getKey());
        }

        return compound;
    }

    private void emptyMaterialSlots(EntityPlayer player) {
        for (int i = 1; i < getSizeInventory(); i++) {
            transferStackToPlayer(player, i);
        }
    }

    private void transferStackToPlayer(EntityPlayer player, int index) {
        ItemStack itemStack = getStackInSlot(index);
        if (!itemStack.isEmpty()) {
            if (!player.inventory.addItemStackToInventory(itemStack)) {
                player.dropItem(itemStack, false);
            }
        }
    }


    @Override
    public int getSizeInventory() {
        if (currentSchema != null) {
            return currentSchema.getNumMaterialSlots() + 1;
        }
        return 1;
    }


    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.stacks) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Nullable
    @Override
    public ItemStack getStackInSlot(int index) {
        return this.stacks.get(index);
    }

    @Nullable
    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack itemstack = ItemStackHelper.getAndSplit(this.stacks, index, count);

        if (!itemstack.isEmpty()) {
            this.markDirty();
        }

        return itemstack;
    }

    @Nullable
    @Override
    public ItemStack removeStackFromSlot(int index) {
        return ItemStackHelper.getAndRemove(this.stacks, index);
    }

    @Override
    public void setInventorySlotContents(int index, @Nullable ItemStack stack) {
        this.stacks.set(index, stack);

        if (!stack.isEmpty() && stack.getCount() > this.getInventoryStackLimit()) {
            stack.setCount(this.getInventoryStackLimit());
        }

        if (index == 0 && !world.isRemote) {
            setCurrentSchema(null);
            sync();
        } else {
            markDirty();
        }
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player) {

    }

    @Override
    public void closeInventory(EntityPlayer player) {

    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index == 0) {
            return true;
        } else if (currentSchema != null) {
            return currentSchema.slotAcceptsMaterial(getTargetItemStack(), index - 1, stack);
        }
        return false;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        this.stacks.clear();
    }

    @Override
    public String getName() {
        return "workbench";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }


}
