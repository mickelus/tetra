package se.mickelus.tetra.blocks.hammer;

import java.util.Collections;
import java.util.LinkedList;
import net.minecraft.block.BlockLiquid;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import se.mickelus.tetra.items.cell.ItemCellMagmatic;

import javax.annotation.Nullable;
import org.apache.commons.lang3.EnumUtils;

public class TileEntityHammerBase extends TileEntity {

    private static final String slotsKey = "slots";
    private static final String indexKey = "slot";
    private ItemStack[] slots;

    private boolean hasPlateWest = true;
    private boolean hasPlateEast = true;

    private EnumHammerConfig configEast = EnumHammerConfig.A;
    private EnumHammerConfig configWest = EnumHammerConfig.A;


    public TileEntityHammerBase() {
        slots = new ItemStack[2];
    }

    public boolean hasEffect(EnumHammerEffect effect) {
        if (effect.requiresBoth) {
            return effect.equals(EnumHammerEffect.fromConfig(configEast, getWorld().getSeed()))
                    && effect.equals(EnumHammerEffect.fromConfig(configWest, getWorld().getSeed()));
        }
        return effect.equals(EnumHammerEffect.fromConfig(configEast, getWorld().getSeed()))
                || effect.equals(EnumHammerEffect.fromConfig(configWest, getWorld().getSeed()));
    }

    public int getHammerLevel() {
        return hasEffect(EnumHammerEffect.OVERCHARGED) ? 5 : 4;
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
        if (index > 0 && index < slots.length && slots[index] != null && slots[index].getItem() instanceof ItemCellMagmatic) {
            ItemCellMagmatic item = (ItemCellMagmatic) slots[index].getItem();
            item.reduceCharge(slots[index], amount);
        }
    }

    private void applyConsumeEffect() {
        EnumFacing facing = getWorld().getBlockState(getPos()).getValue(BlockHammerBase.propFacing);
        Vec3d pos = new Vec3d(getPos());
        pos = pos.addVector(0.5, 0.5, 0.5);

        if (!world.isRemote && hasEffect(EnumHammerEffect.LEAKY)) {
            int countCell0 = world.rand.nextInt(Math.min(16, getCellFuel(0)));
            int countCell1 = world.rand.nextInt(Math.min(16, getCellFuel(1)));
            consumeFuel(0, countCell0);
            consumeFuel(1, countCell1);

            if (countCell0 > 0 || countCell1 > 0) {

                // particles cell 1
                Vec3d posCell0 = pos.add(new Vec3d(facing.getDirectionVec()).scale(0.55));
                spawnParticle(EnumParticleTypes.LAVA, posCell0, countCell0 * 2, 0.06f);
                spawnParticle(EnumParticleTypes.SMOKE_LARGE, posCell0, 2, 0f);

                // particles cell 2
                Vec3d posCell1 = pos.add(new Vec3d(facing.getOpposite().getDirectionVec()).scale(0.55));
                spawnParticle(EnumParticleTypes.LAVA, posCell1, countCell1 * 2, 0.06f);
                spawnParticle(EnumParticleTypes.SMOKE_LARGE, posCell1, 2, 0f);

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
                        .limit(countCell0 + countCell1)
                        .forEach(blockPos -> world.setBlockState(blockPos, Blocks.FIRE.getDefaultState(), 11));
            }
        }
    }

    private int fuelUsage() {
        int usage = 5;
        if (!hasPlateEast) {
            usage += 2;
        }
        if (!hasPlateWest) {
            usage += 2;
        }

        if (hasEffect(EnumHammerEffect.OVERCHARGED)) {
            usage += 4;
        }

        if (hasEffect(EnumHammerEffect.EFFICIENT)) {
            usage -= 3;
        }

        return Math.max(usage, 1);
    }

    public boolean hasCellInSlot(int index){
        return index >= 0 && index < slots.length && slots[index] != null;
    }

    public int getCellFuel(int index){
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

    public boolean putCellInSlot(ItemStack itemStack, int index) {
        if (itemStack.getItem() instanceof ItemCellMagmatic
                && index >= 0 && index < slots.length && slots[index] == null) {
            slots[index] = itemStack;
            return true;
        }
        return false;
    }

    public void removePlate(EnumHammerPlate plate) {
        switch (plate) {
            case EAST:
                hasPlateEast = false;
                break;
            case WEST:
                hasPlateWest = false;
                break;
        }
        markDirty();
    }

    public void attachPlate(EnumHammerPlate plate) {
        switch (plate) {
            case EAST:
                hasPlateEast = true;
                break;
            case WEST:
                hasPlateWest = true;
                break;
        }
        markDirty();
    }

    public boolean hasPlate(EnumHammerPlate plate) {
        switch (plate) {
            case EAST:
                return hasPlateEast;
            case WEST:
                return hasPlateWest;
        }
        return false;
    }

    public void reconfigure(EnumFacing side) {
        if (EnumFacing.EAST.equals(side)) {
            configEast = EnumHammerConfig.getNextConfiguration(configEast);
            applyReconfigurationEffect(EnumHammerEffect.fromConfig(configEast, world.getSeed()));
        } else if (EnumFacing.WEST.equals(side)) {
            configWest = EnumHammerConfig.getNextConfiguration(configWest);
            applyReconfigurationEffect(EnumHammerEffect.fromConfig(configWest, world.getSeed()));
        }
        markDirty();
    }

    private void applyReconfigurationEffect(EnumHammerEffect effect) {
        EnumFacing facing = getWorld().getBlockState(getPos()).getValue(BlockHammerBase.propFacing);
        Vec3d pos = new Vec3d(getPos());
        pos = pos.addVector(0.5, 0.5, 0.5);

        if (EnumHammerEffect.OVERCHARGED.equals(effect)) {
            if (!hasCellInSlot(0)) {
                Vec3d rotPos = pos.add(new Vec3d(facing.getDirectionVec()).scale(0.55));
                spawnParticle(EnumParticleTypes.SMOKE_NORMAL, rotPos, 15, 0.02f);
            }

            if (!hasCellInSlot(1)) {
                Vec3d rotPos = pos.add(new Vec3d(facing.getOpposite().getDirectionVec()).scale(0.55));
                spawnParticle(EnumParticleTypes.SMOKE_NORMAL, rotPos, 15, 0.02f);
            }
        }

        if (EnumHammerEffect.LEAKY.equals(effect)) {
            if (getCellFuel(0) > 0) {
                Vec3d rotPos = pos.add(new Vec3d(facing.getDirectionVec()).scale(0.55));
                spawnParticle(EnumParticleTypes.LAVA, rotPos, 3, 0.06f);
                spawnParticle(EnumParticleTypes.SMOKE_LARGE, rotPos, 3, 0f);
            }

            if (getCellFuel(1) > 0) {
                Vec3d rotPos = pos.add(new Vec3d(facing.getOpposite().getDirectionVec()).scale(0.55));
                spawnParticle(EnumParticleTypes.LAVA, rotPos, 3, 0.06f);
                spawnParticle(EnumParticleTypes.SMOKE_LARGE, rotPos, 3, 0f);
            }
        }
    }

    public EnumHammerConfig getConfiguration(EnumFacing side) {
        if (EnumFacing.EAST.equals(side)) {
            return configEast;
        } else if (EnumFacing.WEST.equals(side)) {
            return configWest;
        }

        return EnumHammerConfig.A;
    }

    /**
     * Utility for spawning particles from the server
     */
    private void spawnParticle(EnumParticleTypes particle, Vec3d pos, int count, float speed) {
        if (world instanceof WorldServer) {
            ((WorldServer) world).spawnParticle(particle, pos.x, pos.y, pos.z, count,  0, 0, 0, speed);
        }
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
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
        if (compound.hasKey(slotsKey)) {
            NBTTagList tagList = compound.getTagList(slotsKey, 10);

            for (int i = 0; i < tagList.tagCount(); i++) {
                NBTTagCompound nbttagcompound = tagList.getCompoundTagAt(i);
                int slot = nbttagcompound.getByte(indexKey) & 255;

                if (slot < this.slots.length) {
                    this.slots[slot] = new ItemStack(nbttagcompound);
                }
            }
        }

        hasPlateEast = compound.getBoolean(EnumHammerPlate.EAST.key);
        hasPlateWest = compound.getBoolean(EnumHammerPlate.WEST.key);

        if (compound.hasKey(EnumHammerConfig.propE.getName())) {
            String enumName = compound.getString(EnumHammerConfig.propE.getName());
            if (EnumUtils.isValidEnum(EnumHammerConfig.class, enumName)) {
                configEast = EnumHammerConfig.valueOf(enumName);
            }
        }

        if (compound.hasKey(EnumHammerConfig.propW.getName())) {
            String enumName = compound.getString(EnumHammerConfig.propW.getName());
            if (EnumUtils.isValidEnum(EnumHammerConfig.class, enumName)) {
                configWest = EnumHammerConfig.valueOf(enumName);
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        writeCells(compound, slots);

        writePlate(compound, EnumHammerPlate.EAST, hasPlateEast);
        writePlate(compound, EnumHammerPlate.WEST, hasPlateWest);

        writeConfig(compound, configEast, configWest);

        return compound;
    }

    public static void writeCells(NBTTagCompound compound, ItemStack... cells) {
        NBTTagList nbttaglist = new NBTTagList();
        for (int i = 0; i < cells.length; i++) {
            if (cells[i] != null) {
                NBTTagCompound nbttagcompound = new NBTTagCompound();

                nbttagcompound.setByte(indexKey, (byte) i);
                cells[i].writeToNBT(nbttagcompound);

                nbttaglist.appendTag(nbttagcompound);
            }
        }
        compound.setTag(slotsKey, nbttaglist);
    }

    public static void writePlate(NBTTagCompound compound, EnumHammerPlate plate, boolean hasPlate) {
        compound.setBoolean(plate.key, hasPlate);
    }

    public static void writeConfig(NBTTagCompound compound, EnumHammerConfig configEast, EnumHammerConfig configWest) {
        compound.setString(EnumHammerConfig.propE.getName(), configEast.toString());
        compound.setString(EnumHammerConfig.propW.getName(), configWest.toString());
    }

}
