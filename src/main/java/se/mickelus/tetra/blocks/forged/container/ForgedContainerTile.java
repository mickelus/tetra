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

    public Optional<ForgedContainerTile> getOrDelegate() {
        if (world != null && getBlockState().getBlock() instanceof ForgedContainerBlock && isFlipped()) {
            return TileEntityOptional.from(world, pos.offset(getFacing().rotateYCCW()), ForgedContainerTile.class);
        }
        return Optional.of(this);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull net.minecraftforge.common.capabilities.Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return getOrDelegate().map(primary -> primary.handler.<T>cast()).orElseGet(LazyOptional::empty);
        }
        return super.getCapability(cap, side);
    }

    public void open(@Nullable PlayerEntity player) {
        if (lidIntegrity > 0) {
            lidIntegrity--;
            markDirty();

            if (!world.isRemote) {
                ServerWorld worldServer = (ServerWorld) world;
                if (lidIntegrity == 0) {
                    causeOpeningEffects(worldServer);
                } else {
                    worldServer.playSound(null, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, SoundCategory.PLAYERS, 0.5f, 1.3f);
                }

                Optional.ofNullable(player)
                        .filter(p -> !p.isPotionActive(Effects.STRENGTH))
                        .ifPresent(p -> p.addPotionEffect(new EffectInstance(Effects.MINING_FATIGUE, 200, 5)));
            } else if (lidIntegrity == 0) { // start lid open animation on the client
                openTime = System.currentTimeMillis();
            }

            updateBlockState();
        }
    }

    private void causeOpeningEffects(ServerWorld worldServer) {
        Direction facing = worldServer.getBlockState(pos).get(HorizontalBlock.HORIZONTAL_FACING);
        Vector3d smokeDirection = Vector3d.copy(facing.rotateY().getDirectionVec());
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
            worldServer.spawnParticle(ParticleTypes.SMOKE,
                    smokeOrigin.getX() + smokeDirection.x * i * 2 / ( smokeCount - 1),
                    smokeOrigin.getY() + 0.8,
                    smokeOrigin.getZ() + smokeDirection.z * i * 2 / ( smokeCount - 1),
                    1, 0, 0, 0, 0d);
        }

        worldServer.playSound(null, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN, SoundCategory.PLAYERS, 1, 0.5f);
        worldServer.playSound(null, pos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.PLAYERS, 0.2f, 0.8f);
    }

    private void updateBlockState() {
        world.setBlockState(pos, getUpdatedBlockState(getBlockState(), lockIntegrity, lidIntegrity), 3);

        BlockPos offsetPos = pos.offset(getFacing().rotateY());
        world.setBlockState(offsetPos, getUpdatedBlockState(world.getBlockState(offsetPos), lockIntegrity, lidIntegrity), 3);
    }

    public static BlockState getUpdatedBlockState(BlockState blockState, int[] lockIntegrity, int lidIntegrity) {
        if (blockState.get(ForgedContainerBlock.flippedProp)) {
            return blockState
                    .with(ForgedContainerBlock.locked1Prop, lockIntegrity[2] > 0)
                    .with(ForgedContainerBlock.locked2Prop, lockIntegrity[3] > 0)
                    .with(ForgedContainerBlock.anyLockedProp, Arrays.stream(lockIntegrity).anyMatch(integrity -> integrity > 0))
                    .with(ForgedContainerBlock.openProp, lidIntegrity <= 0);
        }

        return blockState
                .with(ForgedContainerBlock.locked1Prop, lockIntegrity[0] > 0)
                .with(ForgedContainerBlock.locked2Prop, lockIntegrity[1] > 0)
                .with(ForgedContainerBlock.anyLockedProp, Arrays.stream(lockIntegrity).anyMatch(integrity -> integrity > 0))
                .with(ForgedContainerBlock.openProp, lidIntegrity <= 0);
    }

    public Direction getFacing() {
        return getBlockState().get(ForgedContainerBlock.facingProp);
    }

    public boolean isFlipped() {
        return getBlockState().get(ForgedContainerBlock.flippedProp);
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
                world.playSound(player, pos, SoundEvents.ITEM_SHIELD_BREAK, SoundCategory.PLAYERS, 1,0.5f);
            } else {
                world.playSound(player, pos, SoundEvents.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, SoundCategory.PLAYERS, 1,0.5f);
            }

            if (!world.isRemote && lockIntegrity[index] == 0) {
                if (player != null) {
                    BlockInteraction.dropLoot(lockLootTable, player, hand, (ServerWorld) world, getBlockState());
                } else {
                    BlockInteraction.dropLoot(lockLootTable, (ServerWorld) world, getPos(), getBlockState());
                }
            }
        }

        updateBlockState();
        markDirty();
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
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);

        handler.ifPresent(handler -> handler.deserializeNBT(compound.getCompound(inventoryKey)));

        for (int i = 0; i < lockIntegrity.length; i++) {
            lockIntegrity[i] = compound.getInt("lock_integrity" + i);
        }

        lidIntegrity = compound.getInt("lid_integrity");
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);

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
