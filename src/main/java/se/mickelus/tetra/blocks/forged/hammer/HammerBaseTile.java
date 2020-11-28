package se.mickelus.tetra.blocks.forged.hammer;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
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

public class HammerBaseTile extends TileEntity implements ITickableTileEntity {

    @ObjectHolder(TetraMod.MOD_ID + ":" + HammerBaseBlock.unlocalizedName)
    public static TileEntityType<HammerBaseTile> type;

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
                    markDirty();
                    return true;
                }
            } else {
                if (moduleB == null) {
                    moduleB = newModule;
                    markDirty();
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
                markDirty();

                return item;
            }
        } else {
            if (moduleB != null) {
                Item item = moduleB.getItem();
                moduleB = null;
                markDirty();

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
        if (!world.isRemote) {
            int fuelUsage = fuelUsage();

            for (int i = 0; i < slots.length; i++) {
                consumeFuel(i, fuelUsage);
            }

            applyConsumeEffect();

            markDirty();
            world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 3);
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
        if (world != null) {
            redstonePower = 0;
            for(Direction direction : Direction.values()) {
                redstonePower += world.getRedstonePower(pos.offset(direction), direction);
            }

            markDirty();
        }
    }

    private int tickrate() {
        // one powered side = 40 tickrate, two powered sides = 20 tickrate
        return redstonePower != 0 ? (int) Math.max(600f / redstonePower, 10) : 20;
    }

    @Override
    public void tick() {
        if (redstonePower > 0 && world.getGameTime() % tickrate() == 0 && world.isBlockPowered(pos) && isFunctional()) {
            BlockPos targetPos = pos.down(2);
            BlockState targetState = world.getBlockState(targetPos);

            HammerHeadTile head = TileEntityOptional.from(world, pos.down(), HammerHeadTile.class).orElse(null);

            if (head == null || head.isJammed()) {
                return;
            }

            CastOptional.cast(targetState.getBlock(), IInteractiveBlock.class)
                    .map(block -> block.getPotentialInteractions(world, targetPos, targetState, Direction.UP, Collections.singletonList(ToolTypes.hammer)))
                    .map(Arrays::stream)
                    .orElseGet(Stream::empty)
                    .filter(interaction -> ToolTypes.hammer.equals(interaction.requiredTool))
                    .filter(interaction -> getHammerLevel() >= interaction.requiredLevel)
                    .findFirst()
                    .ifPresent(interaction -> {
                        interaction.applyOutcome(world, targetPos, targetState, null, null, Direction.UP);
                        if (!(targetState.getBlock() instanceof AbstractWorkbenchBlock)) {
                            if (!world.isRemote) {
                                consumeFuel();
                            } else {
                                head.activate();
                                world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 0.3f, (float) (0.5 + Math.random() * 0.2));
                            }
                        }
                    }); // todo: check that all interactions work with nullable player
        }
    }

    /**
     * Applies effects in the world when the hammer is used. Should only be called serverside as it contains random elements
     */
    private void applyConsumeEffect() {
        Direction facing = getWorld().getBlockState(getPos()).get(HammerBaseBlock.facingProp);
        Vector3d pos = Vector3d.copyCentered(getPos());

        Vector3d oppositePos = pos.add(Vector3d.copy(facing.getOpposite().getDirectionVec()).scale(0.55));
        pos = pos.add(Vector3d.copy(facing.getDirectionVec()).scale(0.55));

        if (hasEffect(HammerEffect.power)) {
            spawnParticle(ParticleTypes.ENCHANTED_HIT, Vector3d.copy(getPos()).add(0.5, -0.9, 0.5), 15, 0.1f);
        }

        if (hasEffect(HammerEffect.power)) {
            spawnParticle(ParticleTypes.WHITE_ASH, Vector3d.copy(getPos()).add(0.5, -0.9, 0.5), 15, 0.1f);
            int count = world.rand.nextInt(2 + getEffectLevel(HammerEffect.power) * 4);

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
                            BlockPos firePos = getPos().add(x, y, z);
                            if (world.isAirBlock(firePos)) {
                                flammableBlocks.add(firePos);
                            }
                        }
                    }
                }

                // set blocks on fire
                Collections.shuffle(flammableBlocks);
                flammableBlocks.stream()
                        .limit(count)
                        .forEach(blockPos -> world.setBlockState(blockPos, Blocks.FIRE.getDefaultState(), 11));
            }
        }

        if (world.rand.nextFloat() < getJamChance()) {
            TileEntityOptional.from(world, getPos().down(), HammerHeadTile.class).ifPresent(head -> head.setJammed(true));
            world.getEntitiesWithinAABB(ServerPlayerEntity.class, new AxisAlignedBB(getPos()).grow(10, 5, 10))
                    .forEach(player -> BlockUseCriterion.trigger(player, getBlockState(), ItemStack.EMPTY));
            world.playSound(null, getPos(), SoundEvents.BLOCK_GRINDSTONE_USE, SoundCategory.PLAYERS, 0.8f, 0.5f);
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

            return true;
        }

        return false;
    }

    /**
     * Utility for spawning particles on the server
     */
    private void spawnParticle(IParticleData particle, Vector3d pos, int count, float speed) {
        if (world instanceof ServerWorld) {
            ((ServerWorld) world).spawnParticle(particle, pos.x, pos.y, pos.z, count, 0, 0, 0, speed);
        }
    }

    public Direction getFacing() {
        return getBlockState().get(HammerBaseBlock.facingProp);
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, 0, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return write(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.read(getBlockState(), pkt.getNbtCompound());
    }

    @Override
    public void read(BlockState blockState, CompoundNBT compound) {
        super.read(blockState, compound);
        if (compound.contains(slotsKey)) {
            ListNBT tagList = compound.getList(slotsKey, 10);

            for (int i = 0; i < tagList.size(); i++) {
                CompoundNBT itemCompound = tagList.getCompound(i);
                int slot = itemCompound.getByte(indexKey) & 255;

                if (slot < this.slots.length) {
                    this.slots[slot] = ItemStack.read(itemCompound);
                }
            }
        }

        if (compound.contains(moduleAKey)) {
            byte data = compound.getByte(moduleAKey);
            if (data < HammerEffect.values().length) {
                moduleA = HammerEffect.values()[data];
            }
        }

        if (compound.contains(moduleBKey)) {
            byte data = compound.getByte(moduleBKey);
            if (data < HammerEffect.values().length) {
                moduleB = HammerEffect.values()[data];
            }
        }

        redstonePower = compound.getInt(redstoneKey);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);

        writeCells(compound, slots);

        writeModules(compound, moduleA, moduleB);

        compound.putInt(redstoneKey, redstonePower);

        return compound;
    }

    public static void writeModules(CompoundNBT compound, HammerEffect moduleA, HammerEffect moduleB) {
        if (moduleA != null) {
            compound.put(moduleAKey, ByteNBT.valueOf((byte) moduleA.ordinal()));
        }

        if (moduleB != null) {
            compound.put(moduleBKey, ByteNBT.valueOf((byte) moduleB.ordinal()));
        }
    }

    public static void writeCells(CompoundNBT compound, ItemStack... cells) {
        ListNBT nbttaglist = new ListNBT();
        for (int i = 0; i < cells.length; i++) {
            if (cells[i] != null) {
                CompoundNBT nbttagcompound = new CompoundNBT();

                nbttagcompound.putByte(indexKey, (byte) i);
                cells[i].write(nbttagcompound);

                nbttaglist.add(nbttagcompound);
            }
        }
        compound.put(slotsKey, nbttaglist);
    }
}
