package se.mickelus.tetra.blocks.forged.hammer;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.cell.ItemCellMagmatic;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedList;

public class HammerBaseTile extends TileEntity {

    @ObjectHolder(TetraMod.MOD_ID + ":" + HammerBaseBlock.unlocalizedName)
    public static TileEntityType<HammerBaseTile> type;

    private static final String moduleAKey = "modA";
    private static final String moduleBKey = "modB";
    private HammerEffect moduleA;
    private HammerEffect moduleB;

    private static final String slotsKey = "slots";
    private static final String indexKey = "slot";
    private ItemStack[] slots;

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
        int fuelUsage = fuelUsage();

        for (int i = 0; i < slots.length; i++) {
            consumeFuel(i, fuelUsage);
        }

        applyConsumeEffect();
    }

    public void consumeFuel(int index, int amount) {
        if (index >= 0 && index < slots.length && slots[index] != null && slots[index].getItem() instanceof ItemCellMagmatic) {
            ItemCellMagmatic item = (ItemCellMagmatic) slots[index].getItem();
            item.drainCharge(slots[index], amount);
        }
    }

    private void applyConsumeEffect() {
        Direction facing = getWorld().getBlockState(getPos()).get(HammerBaseBlock.facingProp);
        Vector3d pos = Vector3d.copyCentered(getPos());

        Vector3d oppositePos = pos.add(Vector3d.copy(facing.getOpposite().getDirectionVec()).scale(0.55));
        pos = pos.add(Vector3d.copy(facing.getDirectionVec()).scale(0.55));

        if (!world.isRemote) {
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
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);

        writeCells(compound, slots);

        writeModules(compound, moduleA, moduleB);

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
