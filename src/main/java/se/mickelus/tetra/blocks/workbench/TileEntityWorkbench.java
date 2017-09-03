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
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.UpgradeSchema;
import se.mickelus.tetra.network.PacketPipeline;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class TileEntityWorkbench extends TileEntity implements IInventory {

    private static final String STACKS_KEY = "stacks";
    private static final String SLOT_KEY = "slot";
    private static final String SCHEMA_KEY = "schema";

    private NonNullList<ItemStack> stacks;

    private ItemStack previousTarget = ItemStack.EMPTY;
    private UpgradeSchema currentSchema;

    private Map<String, Runnable> changeListeners;


    public TileEntityWorkbench() {
        stacks = NonNullList.withSize(4, ItemStack.EMPTY);
        changeListeners = new HashMap<>();
    }

    public UpgradeSchema getCurrentSchema() {
        return currentSchema;
    }

    public void setCurrentSchema(UpgradeSchema schema) {
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

        if (stack.getItem() instanceof ItemModular) {
            return stack;
        }

        stack = ItemUpgradeRegistry.instance.getPlaceholder(stack);

        return stack;
    }

    public ItemStack[] getMaterials() {
        return stacks.subList(1, 4).toArray(new ItemStack[3]);
    }

    public void initiateCrafting() {
        if (world.isRemote) {
            PacketPipeline.instance.sendToServer(new CraftWorkbenchPacket(pos));
        }

        craft();

        sync();
    }

    public void craft() {
        ItemStack itemStack = getTargetItemStack();

        if (currentSchema != null) {
            itemStack = currentSchema.applyUpgrade(itemStack, getMaterials());
        }

        setInventorySlotContents(0, itemStack);
        this.setCurrentSchema(null);
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

                if (slot >= 0 && slot < this.stacks.size()) {
                    this.stacks.set(slot, new ItemStack(nbttagcompound));
                }
            }
        }

        String schemaKey = compound.getString(SCHEMA_KEY);
        currentSchema = ItemUpgradeRegistry.instance.getSchema(schemaKey);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < this.stacks.size(); ++i) {
            if (!this.stacks.get(i).isEmpty()) {
                NBTTagCompound nbttagcompound = new NBTTagCompound();

                nbttagcompound.setByte(SLOT_KEY, (byte)i);
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
            currentSchema = null;
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
        if(index == 0) {
            return true;
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
