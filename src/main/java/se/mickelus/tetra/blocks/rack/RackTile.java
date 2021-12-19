package se.mickelus.tetra.blocks.rack;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class RackTile extends BlockEntity {
    public static final String unlocalizedName = "rack";
    public static final int inventorySize = 2;
    private static final String inventoryKey = "inv";
    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static BlockEntityType<RackTile> type;
    private final LazyOptional<ItemStackHandler> handler = LazyOptional.of(() -> new ItemStackHandler(inventorySize) {
        protected void onContentsChanged(int slot) {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    });

    public RackTile(BlockPos p_155268_, BlockState p_155269_) {
        super(type, p_155268_, p_155269_);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull net.minecraftforge.common.capabilities.Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return handler.cast();
        }
        return super.getCapability(cap, side);
    }

    public void slotInteract(int slot, Player playerEntity, InteractionHand hand) {
        handler.ifPresent(handler -> {
            ItemStack slotStack = handler.getStackInSlot(slot);
            ItemStack heldStack = playerEntity.getItemInHand(hand);
            if (slotStack.isEmpty()) {
                ItemStack remainder = handler.insertItem(slot, heldStack.copy(), false);
                playerEntity.setItemInHand(hand, remainder);
                playerEntity.playSound(SoundEvents.WOOD_PLACE, 0.5f, 0.7f);
            } else {
                ItemStack extractedStack = handler.extractItem(slot, handler.getSlotLimit(slot), false);
                if (playerEntity.getInventory().add(extractedStack)) {
                    playerEntity.playSound(SoundEvents.ITEM_PICKUP, 0.5f, 1);
                } else {
                    playerEntity.drop(extractedStack, false);
                }
            }
        });
    }

    @Override
    public AABB getRenderBoundingBox() {
        return Shapes.block().bounds().move(worldPosition);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        load(pkt.getTag());
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);

        handler.ifPresent(handler -> handler.deserializeNBT(compound.getCompound(inventoryKey)));
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);

        handler.ifPresent(handler -> compound.put(inventoryKey, handler.serializeNBT()));
    }
}
