package se.mickelus.tetra.blocks.rack;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RackTile extends TileEntity {
    public static final String unlocalizedName = "rack";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static TileEntityType<RackTile> type;

    private static final String inventoryKey = "inv";

    public static final int inventorySize = 2;
    private LazyOptional<ItemStackHandler> handler = LazyOptional.of(() -> new ItemStackHandler(inventorySize) {
        protected void onContentsChanged(int slot) {
            markDirty();
            world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 3);
        }
    });

    public RackTile() {
        super(type);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull net.minecraftforge.common.capabilities.Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return handler.cast();
        }
        return super.getCapability(cap, side);
    }

    public void slotInteract(int slot, PlayerEntity playerEntity, Hand hand) {
        handler.ifPresent(handler -> {
            ItemStack slotStack = handler.getStackInSlot(slot);
            ItemStack heldStack = playerEntity.getHeldItem(hand);
            if (slotStack.isEmpty()) {
                ItemStack remainder = handler.insertItem(slot, heldStack.copy(), false);
                playerEntity.setHeldItem(hand, remainder);
                playerEntity.playSound(SoundEvents.BLOCK_WOOD_PLACE, 0.5f, 0.7f);
            } else {
                ItemStack extractedStack = handler.extractItem(slot, handler.getSlotLimit(slot), false);
                if (playerEntity.inventory.addItemStackToInventory(extractedStack)) {
                    playerEntity.playSound(SoundEvents.ENTITY_ITEM_PICKUP, 0.5f, 1);
                } else {
                    playerEntity.dropItem(extractedStack, false);
                }
            }
        });
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return VoxelShapes.fullCube().getBoundingBox().offset(pos);
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(pos, 0, getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return write(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        read(getBlockState(), pkt.getNbtCompound());
    }

    @Override
    public void read(BlockState blockState, CompoundNBT compound) {
        super.read(blockState, compound);

        handler.ifPresent(handler -> handler.deserializeNBT(compound.getCompound(inventoryKey)));
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);

        handler.ifPresent(handler -> compound.put(inventoryKey, handler.serializeNBT()));

        return compound;
    }
}
