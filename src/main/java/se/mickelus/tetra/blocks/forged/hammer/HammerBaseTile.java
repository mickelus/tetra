package se.mickelus.tetra.blocks.forged.hammer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.ToolTypes;
import se.mickelus.tetra.advancements.BlockUseCriterion;
import se.mickelus.tetra.blocks.salvage.IInteractiveBlock;
import se.mickelus.tetra.blocks.workbench.AbstractWorkbenchBlock;
import se.mickelus.tetra.items.cell.ItemCellMagmatic;
import se.mickelus.tetra.util.CastOptional;
import se.mickelus.tetra.util.TileEntityOptional;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.stream.Stream;

public class HammerBaseTile extends BlockEntity implements TickableBlockEntity {

    @ObjectHolder(TetraMod.MOD_ID + ":" + HammerBaseBlock.unlocalizedName)
    public static BlockEntityType<HammerBaseTile> type;

    private static final String moduleAKey = "modA";
    private static final String moduleBKey = "modB";
    private HammerEffect moduleA;
    private HammerEffect moduleB;

    private static final String slotsKey = "slots";
    private static final String indexKey = "slot";
    private ItemStack[] slots;

    private static final String redstoneKey = "rs";
    private int redstonePower = 0;

    public HammerBaseTile() {
        super(type);
        slots = new ItemStack[2];
    }

    public boolean setModule(boolean isA, Item item) {
        HammerEffect newModule = HammerEffect.fromItem(item);

        if (newModule != null) {
            if (isA) {
                if (moduleA == null) {
                    moduleA = newModule;

                    sync();

                    return true;
                }
            } else {
                if (moduleB == null) {
                    moduleB = newModule;

                    sync();

                    return true;
                }
            }
        }

        return false;
    }

    public Item removeModule(boolean isA) {
        if (isA) {
            if (moduleA != null) {
                Item item = moduleA.getItem();
                moduleA = null;

                sync();

                return item;
            }
        } else {
            if (moduleB != null) {
                Item item = moduleB.getItem();
                moduleB = null;

                sync();

                return item;
            }
        }

        return null;
    }

    public boolean hasEffect(HammerEffect effect) {
        return effect == moduleA || effect == moduleB;
    }

    public HammerEffect getEffect(boolean isA) {
        return isA ? moduleA : moduleB;
    }

    public int getEffectLevel(HammerEffect effect) {
        int level = 0;
        if (effect == moduleA) {
            level++;
        }

        if (effect == moduleB) {
            level++;
        }

        return level;
    }

    public int getHammerLevel() {
        return 5 + getEffectLevel(HammerEffect.power);
    }

    public boolean isFunctional() {
        return isFueled() && getEffect(true) != null && getEffect(false) != null;
    }

    public boolean isFueled() {
        for (int i = 0; i < slots.length; i++) {
            if (getCellFuel(i) <= 0) {
                return false;
            }
        }
        return true;
    }

    public void consumeFuel() {
        if (!level.isClientSide) {
            int fuelUsage = fuelUsage();

            for (int i = 0; i < slots.length; i++) {
                consumeFuel(i, fuelUsage);
            }

            applyConsumeEffect();

            sync();
        }
    }

    public void consumeFuel(int index, int amount) {
        if (index >= 0 && index < slots.length && slots[index] != null && slots[index].getItem() instanceof ItemCellMagmatic) {
            ItemCellMagmatic item = (ItemCellMagmatic) slots[index].getItem();
            item.drainCharge(slots[index], amount);
        }
    }

    public float getJamChance() {
        return 0.3f - 0.15f * getEffectLevel(HammerEffect.reliable);
    }

    public void updateRedstonePower() {
        if (level != null) {
            int updatedPower = 0;
            for(Direction direction : Direction.values()) {
                updatedPower += level.getSignal(worldPosition.relative(direction), direction);
            }

            if (updatedPower != redstonePower){
                redstonePower = updatedPower;
                sync();
            }
        }
    }

    private int tickrate() {
        // one powered side = 40 tickrate, two powered sides = 20 tickrate
        return redstonePower != 0 ? (int) Math.max(600f / redstonePower, 10) : 20;
    }

    @Override
    public void tick() {
        if (redstonePower > 0 && level.getGameTime() % tickrate() == 0 && level.hasNeighborSignal(worldPosition) && isFunctional()) {
            BlockPos targetPos = worldPosition.below(2);
            BlockState targetState = level.getBlockState(targetPos);

            HammerHeadTile head = TileEntityOptional.from(level, worldPosition.below(), HammerHeadTile.class).orElse(null);

            if (head == null || head.isJammed()) {
                return;
            }

            CastOptional.cast(targetState.getBlock(), IInteractiveBlock.class)
                    .map(block -> block.getPotentialInteractions(level, targetPos, targetState, Direction.UP, Collections.singletonList(ToolTypes.hammer)))
                    .map(Arrays::stream)
                    .orElseGet(Stream::empty)
                    .filter(interaction -> ToolTypes.hammer.equals(interaction.requiredTool))
                    .filter(interaction -> getHammerLevel() >= interaction.requiredLevel)
                    .findFirst()
                    .ifPresent(interaction -> {
                        interaction.applyOutcome(level, targetPos, targetState, null, null, Direction.UP);

                        // the workbench triggers the hammer on the server side, so no need to consume fuel and play sounds
                        if (!(targetState.getBlock() instanceof AbstractWorkbenchBlock)) {
                            if (!level.isClientSide) {
                                consumeFuel();
                            } else {
                                head.activate();
                                level.playSound(null, worldPosition, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.2f, (float) (0.5 + Math.random() * 0.2));
                            }
                        } else {
                            head.activate();
                        }
                    });
        }
    }

    /**
     * Applies effects in the world when the hammer is used. Should only be called serverside as it contains random elements
     */
    private void applyConsumeEffect() {
        Direction facing = getLevel().getBlockState(getBlockPos()).getValue(HammerBaseBlock.facingProp);
        Vec3 pos = Vec3.atCenterOf(getBlockPos());

        Vec3 oppositePos = pos.add(Vec3.atLowerCornerOf(facing.getOpposite().getNormal()).scale(0.55));
        pos = pos.add(Vec3.atLowerCornerOf(facing.getNormal()).scale(0.55));

        if (hasEffect(HammerEffect.power)) {
            spawnParticle(ParticleTypes.ENCHANTED_HIT, Vec3.atLowerCornerOf(getBlockPos()).add(0.5, -0.9, 0.5), 15, 0.1f);
        }

        if (hasEffect(HammerEffect.power)) {
            spawnParticle(ParticleTypes.WHITE_ASH, Vec3.atLowerCornerOf(getBlockPos()).add(0.5, -0.9, 0.5), 15, 0.1f);
            int count = level.random.nextInt(2 + getEffectLevel(HammerEffect.power) * 4);

            if (count > 2) {
                // particles cell 1
                spawnParticle(ParticleTypes.LAVA, pos, 2, 0.06f);
                spawnParticle(ParticleTypes.LARGE_SMOKE, pos, 2, 0f);

                // particles cell 2
                spawnParticle(ParticleTypes.LAVA, oppositePos, 2, 0.06f);
                spawnParticle(ParticleTypes.LARGE_SMOKE, oppositePos, 2, 0f);

                // gather flammable blocks
                LinkedList<BlockPos> flammableBlocks = new LinkedList<>();
                for (int x = -3; x < 3; x++) {
                    for (int y = -3; y < 2; y++) {
                        for (int z = -3; z < 3; z++) {
                            BlockPos firePos = getBlockPos().offset(x, y, z);
                            if (level.isEmptyBlock(firePos)) {
                                flammableBlocks.add(firePos);
                            }
                        }
                    }
                }

                // set blocks on fire
                Collections.shuffle(flammableBlocks);
                flammableBlocks.stream()
                        .limit(count)
                        .forEach(blockPos -> level.setBlock(blockPos, Blocks.FIRE.defaultBlockState(), 11));
            }
        }

        if (level.random.nextFloat() < getJamChance()) {
            TileEntityOptional.from(level, getBlockPos().below(), HammerHeadTile.class).ifPresent(head -> head.setJammed(true));
            level.getEntitiesOfClass(ServerPlayer.class, new AABB(getBlockPos()).inflate(10, 5, 10))
                    .forEach(player -> BlockUseCriterion.trigger(player, getBlockState(), ItemStack.EMPTY));
            level.playSound(null, getBlockPos(), SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 0.8f, 0.5f);
        }
    }

    private int fuelUsage() {
        int usage = 5;

        usage += getEffectLevel(HammerEffect.power) * 4;
        usage *= 1f - getEffectLevel(HammerEffect.efficient) * 0.4;

        return Math.max(usage, 1);
    }

    public boolean hasCellInSlot(int index) {
        return index >= 0 && index < slots.length && slots[index] != null;
    }

    public int getCellFuel(int index) {
        if (index >= 0 && index < slots.length && slots[index] != null) {
            if (slots[index].getItem() instanceof ItemCellMagmatic) {
                ItemCellMagmatic item = (ItemCellMagmatic) slots[index].getItem();
                return item.getCharge(slots[index]);
            }
        }

        return -1;
    }

    public ItemStack removeCellFromSlot(int index) {
        if (index >= 0 && index < slots.length && slots[index] != null) {
            ItemStack itemStack = slots[index];
            slots[index] = null;

            sync();

            return itemStack;
        }

        return ItemStack.EMPTY;
    }

    public ItemStack getStackInSlot(int index) {
        if (index >= 0 && index < slots.length && slots[index] != null) {
            return slots[index];
        }
        return ItemStack.EMPTY;
    }

    public boolean putCellInSlot(ItemStack itemStack, int index) {
        if (itemStack.getItem() instanceof ItemCellMagmatic
                && index >= 0 && index < slots.length && slots[index] == null) {
            slots[index] = itemStack;

            sync();

            return true;
        }

        return false;
    }

    /**
     * Utility for spawning particles on the server
     */
    private void spawnParticle(ParticleOptions particle, Vec3 pos, int count, float speed) {
        if (level instanceof ServerLevel) {
            ((ServerLevel) level).sendParticles(particle, pos.x, pos.y, pos.z, count, 0, 0, 0, speed);
        }
    }

    public Direction getFacing() {
        return getBlockState().getValue(HammerBaseBlock.facingProp);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 0, this.getUpdateTag());
    }

    @Override
    public CompoundTag getUpdateTag() {
        return save(new CompoundTag());
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.load(getBlockState(), pkt.getTag());
    }

    @Override
    public void load(BlockState blockState, CompoundTag compound) {
        super.load(blockState, compound);

        slots = new ItemStack[2];
        if (compound.contains(slotsKey)) {
            ListTag tagList = compound.getList(slotsKey, 10);

            for (int i = 0; i < tagList.size(); i++) {
                CompoundTag itemCompound = tagList.getCompound(i);
                int slot = itemCompound.getByte(indexKey) & 255;

                if (slot < this.slots.length) {
                    this.slots[slot] = ItemStack.of(itemCompound);
                }
            }
        }

        moduleA = null;
        if (compound.contains(moduleAKey)) {
            byte data = compound.getByte(moduleAKey);
            if (data < HammerEffect.values().length) {
                moduleA = HammerEffect.values()[data];
            }
        }

        moduleB = null;
        if (compound.contains(moduleBKey)) {
            byte data = compound.getByte(moduleBKey);
            if (data < HammerEffect.values().length) {
                moduleB = HammerEffect.values()[data];
            }
        }

        redstonePower = compound.getInt(redstoneKey);
    }

    private void sync() {
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        setChanged();
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        super.save(compound);

        writeCells(compound, slots);

        writeModules(compound, moduleA, moduleB);

        compound.putInt(redstoneKey, redstonePower);

        return compound;
    }

    public static void writeModules(CompoundTag compound, HammerEffect moduleA, HammerEffect moduleB) {
        if (moduleA != null) {
            compound.put(moduleAKey, ByteTag.valueOf((byte) moduleA.ordinal()));
        }

        if (moduleB != null) {
            compound.put(moduleBKey, ByteTag.valueOf((byte) moduleB.ordinal()));
        }
    }

    public static void writeCells(CompoundTag compound, ItemStack... cells) {
        ListTag nbttaglist = new ListTag();
        for (int i = 0; i < cells.length; i++) {
            if (cells[i] != null) {
                CompoundTag nbttagcompound = new CompoundTag();

                nbttagcompound.putByte(indexKey, (byte) i);
                cells[i].save(nbttagcompound);

                nbttaglist.add(nbttagcompound);
            }
        }
        compound.put(slotsKey, nbttaglist);
    }
}
