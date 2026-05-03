package se.mickelus.tetra.blocks.rack;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
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
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import se.mickelus.tetra.blocks.ItemHandlerBlockEntity;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class RackTile extends BlockEntity implements ItemHandlerBlockEntity {
    public static final String unlocalizedName = "rack";
    public static final int inventorySize = 2;
    private static final String inventoryKey = "inv";
    public static BlockEntityType<RackTile> type;
    private final ItemStackHandler handler = new ItemStackHandler(inventorySize) {
        protected void onContentsChanged(int slot) {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    };

    public RackTile(BlockPos p_155268_, BlockState p_155269_) {
        super(type, p_155268_, p_155269_);
    }

    @Override
    public IItemHandler getItemHandler(@Nullable Direction side) {
        return handler;
    }

    public void slotInteract(int slot, Player playerEntity, InteractionHand hand) {
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
    }

    public AABB getRenderBoundingBox() {
        return Shapes.block().bounds().move(worldPosition);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        loadWithComponents(pkt.getTag(), lookupProvider);
    }

    @Override
    protected void loadAdditional(CompoundTag compound, HolderLookup.Provider registries) {
        super.loadAdditional(compound, registries);

        handler.deserializeNBT(registries, compound.getCompound(inventoryKey));
    }

    @Override
    protected void saveAdditional(CompoundTag compound, HolderLookup.Provider registries) {
        super.saveAdditional(compound, registries);

        compound.put(inventoryKey, handler.serializeNBT(registries));
    }
}
