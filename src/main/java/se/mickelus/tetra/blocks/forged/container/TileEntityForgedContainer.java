package se.mickelus.tetra.blocks.forged.container;

import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import se.mickelus.tetra.NBTHelper;
import se.mickelus.tetra.TetraMod;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Random;

public class TileEntityForgedContainer extends TileEntity implements IInventory, INamedContainerProvider {

    private NonNullList<ItemStack> stacks;

    public static int lockIntegrityMax = 4;
    public static int lockCount = 4;
    private int[] lockIntegrity;

    public static int lidIntegrityMax = 5;
    private int lidIntegrity = 3;

    private static final ResourceLocation lockLootTable = new ResourceLocation(TetraMod.MOD_ID, "forged/lock_break");

    public static int compartmentCount = 3;
    public static int compartmentSize = 54;

    public long openTime = -1;

    public TileEntityForgedContainer() {
        stacks = NonNullList.withSize(compartmentSize * compartmentCount, ItemStack.EMPTY);

        lockIntegrity = new int[lockCount];
        Arrays.fill(lockIntegrity, 1);
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

    public Direction getFacing() {
        return world.getBlockState(pos).getValue(BlockForgedContainer.propFacing);
    }

    public boolean isFlipped() {
        return world.getBlockState(pos).getValue(BlockForgedContainer.propFlipped);
    }

    public void open(PlayerEntity player) {
        if (lidIntegrity > 0) {
            lidIntegrity--;
            markDirty();

            if (!world.isRemote) {
                WorldServer worldServer = (WorldServer) world;;
                if (lidIntegrity == 0) {
                    causeOpeningEffects(worldServer);
                } else {
                    worldServer.playSound(null, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, SoundCategory.PLAYERS, 0.5f, 1.3f);
                }

                if (!player.isPotionActive(MobEffects.STRENGTH)) {
                    player.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 100));
                }
            } else if (lidIntegrity == 0) { // start lid open animation on the client
                openTime = System.currentTimeMillis();
            }
        }
    }

    private void causeOpeningEffects(WorldServer worldServer) {
        Direction facing = worldServer.getBlockState(pos).getValue(HorizontalBlock.FACING);
        Vec3d smokeDirection = new Vec3d(facing.rotateY().getDirectionVec());
        Random random = new Random();
        int smokeCount = 5 + random.nextInt(4);

        BlockPos smokeOrigin = pos;
        if (Direction.SOUTH.equals(facing)) {
            smokeOrigin = smokeOrigin.add(1, 0, 0);
        } else if (Direction.WEST.equals(facing)) {
            smokeOrigin = smokeOrigin.add(1, 0, 1);
        } else if (Direction.NORTH.equals(facing)) {
            smokeOrigin = smokeOrigin.add(0, 0, 1);
        }

        for (int i = 0; i < smokeCount; i++) {
            worldServer.spawnParticle(EnumParticleTypes.SMOKE_NORMAL,
                    smokeOrigin.getX() + smokeDirection.x * i * 2 / ( smokeCount - 1),
                    smokeOrigin.getY() + 0.8,
                    smokeOrigin.getZ() + smokeDirection.z * i * 2 / ( smokeCount - 1),
                    1, 0, 0, 0, 0d);
        }

        worldServer.playSound(null, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN, SoundCategory.PLAYERS, 1, 0.5f);
        worldServer.playSound(null, pos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.PLAYERS, 0.2f, 0.8f);
    }

    public boolean isOpen() {
        return lidIntegrity <= 0;
    }

    public boolean isLocked(int index) {
        return lockIntegrity[index] > 0;
    }

    public Boolean[] isLocked() {
        return Arrays.stream(lockIntegrity)
                .mapToObj(integrity -> integrity > 0)
                .toArray(Boolean[]::new);
    }

    public void breakLock(PlayerEntity player, int index) {
        if (lockIntegrity[index] > 0) {
            lockIntegrity[index]--;

            if (!world.isRemote) {
                WorldServer worldServer = (WorldServer) world;

                if (lockIntegrity[index] == 0) {
                    worldServer.playSound(null, pos, SoundEvents.ITEM_SHIELD_BREAK, SoundCategory.PLAYERS, 1,0.5f);
                } else {
                    worldServer.playSound(null, pos, SoundEvents.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, SoundCategory.PLAYERS, 1,0.5f);
                }

                if (lockIntegrity[index] == 0) {
                    LootTable table = worldServer.getLootTableManager().getLootTableFromLocation(lockLootTable);
                    LootContext.Builder builder = new LootContext.Builder(worldServer);
                    builder.withLuck(player.getLuck()).withPlayer(player);

                    table.generateLootForPools(player.getRNG(), builder.build()).forEach(itemStack -> {
                        if (!player.inventory.addItemStackToInventory(itemStack)) {
                            player.dropItem(itemStack, false);
                        }
                    });
                }
            }
        }
        markDirty();
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return writeToNBT(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        this.readFromNBT(packet.getNbtCompound());
    }

    @Override
    public void readFromNBT(CompoundNBT compound) {
        super.readFromNBT(compound);

        NBTHelper.readItemStacks(compound, stacks);

        for (int i = 0; i < lockIntegrity.length; i++) {
            lockIntegrity[i] = compound.getInt("lock_integrity" + i);
        }

        lidIntegrity = compound.getInt("lid_integrity");
    }

    @Override
    public CompoundNBT writeToNBT(CompoundNBT compound) {
        super.writeToNBT(compound);

        NBTHelper.writeItemStacks(stacks, compound);

        writeLockData(compound, lockIntegrity);
        writeLidData(compound, lidIntegrity);

        return compound;
    }

    public static void writeLockData(CompoundNBT compound, int[] lockIntegrity) {
        for (int i = 0; i < lockIntegrity.length; i++) {
            compound.putInt("lock_integrity" + i, lockIntegrity[i]);
        }
    }

    public static void writeLidData(CompoundNBT compound, int lidIntegrity) {
        compound.putInt("lid_integrity", lidIntegrity);
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
    public boolean isUsableByPlayer(PlayerEntity player) {
        return true;
    }

    @Override
    public void openInventory(PlayerEntity player) { }

    @Override
    public void closeInventory(PlayerEntity player) { }

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


    @Override
    public ITextComponent getDisplayName() {
        return new StringTextComponent(getType().getRegistryName().getPath());
    }

    @Nullable
    @Override
    public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ForgedContainerContainer(windowId, this, playerInventory, playerEntity);
    }
}
