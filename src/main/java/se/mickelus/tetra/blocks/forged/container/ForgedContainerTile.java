package se.mickelus.tetra.blocks.forged.container;

import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.salvage.BlockInteraction;
import se.mickelus.tetra.util.TileEntityOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

public class ForgedContainerTile extends TileEntity implements INamedContainerProvider {
    @ObjectHolder(TetraMod.MOD_ID + ":" + ForgedContainerBlock.unlocalizedName)
    public static TileEntityType<ForgedContainerTile> type;

    private static final String inventoryKey = "inv";

    public static int lockIntegrityMax = 4;
    public static int lockCount = 4;
    private int[] lockIntegrity;

    public static int lidIntegrityMax = 5;
    private int lidIntegrity = 0;

    private static final ResourceLocation lockLootTable = new ResourceLocation(TetraMod.MOD_ID, "forged/lock_break");

    public static int compartmentCount = 3;
    public static int compartmentSize = 54;

    public long openTime = -1;

    private LazyOptional<ItemStackHandler> handler = LazyOptional.of(() -> new ItemStackHandler(compartmentSize * compartmentCount));

    public ForgedContainerTile() {
        super(type);

        lockIntegrity = new int[lockCount];
    }

    public ForgedContainerTile getOrDelegate() {
        if (level != null && getBlockState().getBlock() instanceof ForgedContainerBlock && isFlipped()) {
            return TileEntityOptional.from(level, worldPosition.relative(getFacing().getCounterClockWise()), ForgedContainerTile.class)
                    .orElse(null);
        }
        return this;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull net.minecraftforge.common.capabilities.Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return getOrDelegate().handler.cast();
        }
        return super.getCapability(cap, side);
    }

    public void open(@Nullable PlayerEntity player) {
        if (lidIntegrity > 0) {
            lidIntegrity--;
            setChanged();

            if (!level.isClientSide) {
                ServerWorld worldServer = (ServerWorld) level;
                if (lidIntegrity == 0) {
                    causeOpeningEffects(worldServer);
                } else {
                    worldServer.playSound(null, worldPosition, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundCategory.PLAYERS, 0.5f, 1.3f);
                }

                Optional.ofNullable(player)
                        .filter(p -> !p.hasEffect(Effects.DAMAGE_BOOST))
                        .ifPresent(p -> p.addEffect(new EffectInstance(Effects.DIG_SLOWDOWN, 200, 5)));
            } else if (lidIntegrity == 0) { // start lid open animation on the client
                openTime = System.currentTimeMillis();
            }

            updateBlockState();
        }
    }

    private void causeOpeningEffects(ServerWorld worldServer) {
        Direction facing = worldServer.getBlockState(worldPosition).getValue(HorizontalBlock.FACING);
        Vector3d smokeDirection = Vector3d.atLowerCornerOf(facing.getClockWise().getNormal());
        Random random = new Random();
        int smokeCount = 5 + random.nextInt(4);

        BlockPos smokeOrigin = worldPosition;
        if (Direction.SOUTH.equals(facing)) {
            smokeOrigin = smokeOrigin.offset(1, 0, 0);
        } else if (Direction.WEST.equals(facing)) {
            smokeOrigin = smokeOrigin.offset(1, 0, 1);
        } else if (Direction.NORTH.equals(facing)) {
            smokeOrigin = smokeOrigin.offset(0, 0, 1);
        }

        for (int i = 0; i < smokeCount; i++) {
            worldServer.sendParticles(ParticleTypes.SMOKE,
                    smokeOrigin.getX() + smokeDirection.x * i * 2 / ( smokeCount - 1),
                    smokeOrigin.getY() + 0.8,
                    smokeOrigin.getZ() + smokeDirection.z * i * 2 / ( smokeCount - 1),
                    1, 0, 0, 0, 0d);
        }

        worldServer.playSound(null, worldPosition, SoundEvents.IRON_TRAPDOOR_OPEN, SoundCategory.PLAYERS, 1, 0.5f);
        worldServer.playSound(null, worldPosition, SoundEvents.LAVA_EXTINGUISH, SoundCategory.PLAYERS, 0.2f, 0.8f);
    }

    private void updateBlockState() {
        level.setBlock(worldPosition, getUpdatedBlockState(getBlockState(), lockIntegrity, lidIntegrity), 3);

        BlockPos offsetPos = worldPosition.relative(getFacing().getClockWise());
        level.setBlock(offsetPos, getUpdatedBlockState(level.getBlockState(offsetPos), lockIntegrity, lidIntegrity), 3);
    }

    public static BlockState getUpdatedBlockState(BlockState blockState, int[] lockIntegrity, int lidIntegrity) {
        if (blockState.getValue(ForgedContainerBlock.flippedProp)) {
            return blockState
                    .setValue(ForgedContainerBlock.locked1Prop, lockIntegrity[2] > 0)
                    .setValue(ForgedContainerBlock.locked2Prop, lockIntegrity[3] > 0)
                    .setValue(ForgedContainerBlock.anyLockedProp, Arrays.stream(lockIntegrity).anyMatch(integrity -> integrity > 0))
                    .setValue(ForgedContainerBlock.openProp, lidIntegrity <= 0);
        }

        return blockState
                .setValue(ForgedContainerBlock.locked1Prop, lockIntegrity[0] > 0)
                .setValue(ForgedContainerBlock.locked2Prop, lockIntegrity[1] > 0)
                .setValue(ForgedContainerBlock.anyLockedProp, Arrays.stream(lockIntegrity).anyMatch(integrity -> integrity > 0))
                .setValue(ForgedContainerBlock.openProp, lidIntegrity <= 0);
    }

    public Direction getFacing() {
        return getBlockState().getValue(ForgedContainerBlock.facingProp);
    }

    public boolean isFlipped() {
        return getBlockState().getValue(ForgedContainerBlock.flippedProp);
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

    public void breakLock(@Nullable PlayerEntity player, int index, @Nullable Hand hand) {
        if (lockIntegrity[index] > 0) {
            lockIntegrity[index]--;

            if (lockIntegrity[index] == 0) {
                level.playSound(player, worldPosition, SoundEvents.SHIELD_BREAK, SoundCategory.PLAYERS, 1,0.5f);
            } else {
                level.playSound(player, worldPosition, SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, SoundCategory.PLAYERS, 1,0.5f);
            }

            if (!level.isClientSide && lockIntegrity[index] == 0) {
                if (player != null) {
                    BlockInteraction.dropLoot(lockLootTable, player, hand, (ServerWorld) level, getBlockState());
                } else {
                    BlockInteraction.dropLoot(lockLootTable, (ServerWorld) level, getBlockPos(), getBlockState());
                }
            }
        }

        updateBlockState();
        setChanged();
    }

    @Override
    public ITextComponent getDisplayName() {
        return new StringTextComponent(ForgedContainerBlock.unlocalizedName);
    }

    @Nullable
    @Override
    public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ForgedContainerContainer(windowId, this, playerInventory, playerEntity);
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(worldPosition, 0, getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return save(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        load(getBlockState(), pkt.getTag());
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);

        handler.ifPresent(handler -> handler.deserializeNBT(compound.getCompound(inventoryKey)));

        for (int i = 0; i < lockIntegrity.length; i++) {
            lockIntegrity[i] = compound.getInt("lock_integrity" + i);
        }

        lidIntegrity = compound.getInt("lid_integrity");
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        super.save(compound);

        handler.ifPresent(handler -> compound.put(inventoryKey, handler.serializeNBT()));

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
}
