package se.mickelus.tetra.blocks.forged.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import se.mickelus.tetra.NBTHelper;
import se.mickelus.tetra.TetraMod;

import javax.annotation.Nullable;
import java.util.Arrays;

public class TileEntityForgedContainer extends TileEntity implements IInventory {

    private NonNullList<ItemStack> stacks;

    private boolean[] locked;

    private boolean open = false;

    private static final ResourceLocation lockLootTable = new ResourceLocation(TetraMod.MOD_ID, "forged/lock_break");

    public static int compartmentCount = 3;
    public static int compartmentSize = 54;

    public TileEntityForgedContainer() {
        stacks = NonNullList.withSize(compartmentSize * compartmentCount, ItemStack.EMPTY);

        locked = new boolean[4];
        Arrays.fill(locked, Boolean.TRUE);
    }

    public TileEntityForgedContainer getOrDelegate() {
        if (isFlipped()) {
            TileEntityForgedContainer te = (TileEntityForgedContainer) world.getTileEntity(pos.offset(getFacing().rotateYCCW()));
            if (te != null) {
                return te;
            }
        }
        return this;
    }

    public EnumFacing getFacing() {
        return world.getBlockState(pos).getValue(BlockForgedContainer.propFacing);
    }

    public boolean isFlipped() {
        return world.getBlockState(pos).getValue(BlockForgedContainer.propFlipped);
    }

    public void open() {
        open = true;
    }

    public boolean isOpen() {
        return open;
    }

    public boolean isLocked(int index) {
        return locked[index];
    }

    public boolean[] isLocked() {
        return locked;
    }

    public void breakLock(EntityPlayer player, int index) {
        if (locked[index]) {
            locked[index] = false;

            if (!world.isRemote) {
                world.getTileEntity(pos);
                WorldServer worldServer = (WorldServer) world;
                LootTable table = worldServer.getLootTableManager().getLootTableFromLocation(lockLootTable);
                LootContext.Builder builder = new LootContext.Builder(worldServer);
                builder.withLuck(player.getLuck()).withPlayer(player);

                table.generateLootForPools(player.getRNG(), builder.build()).forEach(itemStack -> {
                    if (!player.inventory.addItemStackToInventory(itemStack)) {
                        player.dropItem(itemStack, false);
                    }
                });

                worldServer.playSound(null, pos, SoundEvents.BLOCK_ANVIL_HIT, SoundCategory.PLAYERS, 1, 0.6f);
            }
            markDirty();
        }
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        this.readFromNBT(packet.getNbtCompound());
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        NBTHelper.readItemStacks(compound, stacks);

        for (int i = 0; i < locked.length; i++) {
            locked[i] = compound.getBoolean("locked" + i);
        }

        open = compound.getBoolean("open");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        NBTHelper.writeItemStacks(stacks, compound);

        for (int i = 0; i < locked.length; i++) {
            compound.setBoolean("locked" + i, locked[i]);
        }

        compound.setBoolean("open", open);

        return compound;
    }


    @Override
    public int getSizeInventory() {
        return stacks.size();
    }


    @Override
    public boolean isEmpty() {
        return stacks.stream()
                .allMatch(ItemStack::isEmpty);
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
            markDirty();
        }

        return itemstack;
    }

    @Nullable
    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack itemstack = ItemStackHelper.getAndRemove(this.stacks, index);

        if (!itemstack.isEmpty()) {
            markDirty();
        }

        return itemstack;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack itemStack) {
        stacks.set(index, itemStack);

        if (!itemStack.isEmpty() && itemStack.getCount() > getInventoryStackLimit()) {
            itemStack.setCount(getInventoryStackLimit());
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
    public void openInventory(EntityPlayer player) { }

    @Override
    public void closeInventory(EntityPlayer player) { }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) { }

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
        return BlockForgedContainer.unlocalizedName;
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }


}
