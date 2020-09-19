package se.mickelus.tetra.blocks.forged.hammer;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
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
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Map;

public class HammerBaseTile extends TileEntity {

    @ObjectHolder(TetraMod.MOD_ID + ":" + HammerBaseBlock.unlocalizedName)
    public static TileEntityType<HammerBaseTile> type;

//    private static final String modulesAKey = "modA";
//    private static final String modulesBKey = "modB";
//    private boolean[] modulesA;
//    private boolean[] modulesB;
//
//    private static final String currentAKey = "curA";
//    private static final String currentBKey = "curB";
//    private EnumHammerEffect currentA;
//    private EnumHammerEffect currentB;

    private static final String slotsKey = "slots";
    private static final String indexKey = "slot";
    private ItemStack[] slots;

    public HammerBaseTile() {
        super(type);
        slots = new ItemStack[2];

//        modulesA = new boolean[EnumHammerEffect.values().length];
//        modulesB = new boolean[EnumHammerEffect.values().length];

    }

    public boolean hasEffect(EnumHammerEffect effect) {
        return HammerBaseBlock.hasEffect(world, getBlockState(), effect);
    }

    public int getEffectLevel(EnumHammerEffect effect) {
        return HammerBaseBlock.getEffectLevel(world, getBlockState(), effect);
    }

    public int getHammerLevel() {
        return 5 + getEffectLevel(EnumHammerEffect.power);
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

            if (item.getCharge(slots[index]) <= 0) {
                BooleanProperty prop = index == 0 ? HammerBaseBlock.propCell1Charged : HammerBaseBlock.propCell2Charged;
                world.setBlockState(pos, getBlockState().with(prop, false), 3);
            }

        }
    }

    private void applyConsumeEffect() {
        Direction facing = getWorld().getBlockState(getPos()).get(HammerBaseBlock.propFacing);
        Vector3d pos = Vector3d.copyCentered(getPos());

        Vector3d oppositePos = pos.add(Vector3d.copy(facing.getOpposite().getDirectionVec()).scale(0.55));
        pos = pos.add(Vector3d.copy(facing.getDirectionVec()).scale(0.55));

        if (!world.isRemote) {
            if (hasEffect(EnumHammerEffect.power)) {
                spawnParticle(ParticleTypes.ENCHANTED_HIT, Vector3d.copy(getPos()).add(0.5, -0.9, 0.5), 15, 0.1f);
            }

            if (hasEffect(EnumHammerEffect.precise)) {
                int countCell0 = world.rand.nextInt(Math.min(16, getCellFuel(0)));
                int countCell1 = world.rand.nextInt(Math.min(16, getCellFuel(1)));
                consumeFuel(0, countCell0);
                consumeFuel(1, countCell1);

                if (countCell0 > 0 || countCell1 > 0) {
                    // particles cell 1
                    spawnParticle(ParticleTypes.LAVA, pos, countCell0 * 2, 0.06f);
                    spawnParticle(ParticleTypes.LARGE_SMOKE, pos, 2, 0f);

                    // particles cell 2
                    spawnParticle(ParticleTypes.LAVA, oppositePos, countCell1 * 2, 0.06f);
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
                            .limit(countCell0 + countCell1)
                            .forEach(blockPos -> world.setBlockState(blockPos, Blocks.FIRE.getDefaultState(), 11));
                }
            }

            if (hasEffect(EnumHammerEffect.reliable)) {
                spawnParticle(ParticleTypes.POOF, Vector3d.copy(getPos()).add(0.5, -0.9, 0.5), 3, 0.1f);
            }
        }
    }

    private int fuelUsage() {
        int usage = 5;
        if (!getBlockState().get(EnumHammerPlate.east.prop)) {
            usage += 2;
        }
        if (!getBlockState().get(EnumHammerPlate.west.prop)) {
            usage += 2;
        }

        if (hasEffect(EnumHammerEffect.power)) {
            usage += 4;
        }

        if (hasEffect(EnumHammerEffect.efficient)) {
            usage -= 3;
        }

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

            if (index == 0) {
                world.setBlockState(pos, getBlockState()
                        .with(HammerBaseBlock.propCell1, false)
                        .with(HammerBaseBlock.propCell1Charged, false),
                        3);
            } else {
                world.setBlockState(pos, getBlockState()
                        .with(HammerBaseBlock.propCell2, false)
                        .with(HammerBaseBlock.propCell2Charged, false),
                        3);
            }

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

            boolean hasCharge = ((ItemCellMagmatic) itemStack.getItem()).getCharge(itemStack) > 0;
            if (index == 0) {
                world.setBlockState(pos, getBlockState()
                        .with(HammerBaseBlock.propCell1, true)
                        .with(HammerBaseBlock.propCell1Charged, hasCharge),
                        3);
            } else {
                world.setBlockState(pos, getBlockState()
                        .with(HammerBaseBlock.propCell2, true)
                        .with(HammerBaseBlock.propCell2Charged, hasCharge),
                        3);
            }

            return true;
        }

        return false;
    }

    public void applyReconfigurationEffect() {
        Direction facing = getWorld().getBlockState(getPos()).get(HammerBaseBlock.propFacing);
        Vector3d pos = Vector3d.copyCentered(getPos());

        Vector3d oppositePos = pos.add(Vector3d.copy(facing.getOpposite().getDirectionVec()).scale(0.55));
        pos = pos.add(Vector3d.copy(facing.getDirectionVec()).scale(0.55));


        if (hasEffect(EnumHammerEffect.efficient)) {
            spawnParticle(ParticleTypes.SMOKE, pos, 5, 0.02f);

            spawnParticle(ParticleTypes.SMOKE, oppositePos, 5, 0.02f);
        }

        if (hasEffect(EnumHammerEffect.power)) {
            spawnParticle(ParticleTypes.ENCHANTED_HIT, pos, 15, 0.1f);

            spawnParticle(ParticleTypes.ENCHANTED_HIT, oppositePos, 15, 0.1f);
        }

        if (hasEffect(EnumHammerEffect.precise)) {
            spawnParticle(ParticleTypes.LAVA, pos, 3, 0.03f);
            spawnParticle(ParticleTypes.LARGE_SMOKE, pos, 1, 0f);

            spawnParticle(ParticleTypes.LAVA, oppositePos, 3, 0.03f);
            spawnParticle(ParticleTypes.LARGE_SMOKE, oppositePos, 1, 0f);
        }

        if (hasEffect(EnumHammerEffect.reliable)) {
            spawnParticle(ParticleTypes.POOF, pos, 1, 0.05f);

            spawnParticle(ParticleTypes.POOF, oppositePos, 1, 0.05f);
        }
    }

    /**
     * Utility for spawning particles on the server
     */
    private void spawnParticle(IParticleData particle, Vector3d pos, int count, float speed) {
        if (world instanceof ServerWorld) {
            ((ServerWorld) world).spawnParticle(particle, pos.x, pos.y, pos.z, count, 0, 0, 0, speed);
        }
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
//
//        if (compound.contains(modulesAKey)) {
//            byte data = compound.getByte(modulesAKey);
//            modulesA[0] = ((data & 0x01) != 0);
//            modulesA[1] = ((data & 0x02) != 0);
//            modulesA[2] = ((data & 0x04) != 0);
//            modulesA[3] = ((data & 0x08) != 0);
//        }
//
//        if (compound.contains(modulesBKey)) {
//            byte data = compound.getByte(modulesBKey);
//            modulesB[0] = ((data & 0x01) != 0);
//            modulesB[1] = ((data & 0x02) != 0);
//            modulesB[2] = ((data & 0x04) != 0);
//            modulesB[3] = ((data & 0x08) != 0);
//        }
//
//        if (compound.contains(currentAKey)) {
//            byte data = compound.getByte(currentAKey);
//            if (data > 0 && data < EnumHammerEffect.values().length) {
//                currentA = EnumHammerEffect.values()[data];
//            }
//        }
//
//        if (compound.contains(currentBKey)) {
//            byte data = compound.getByte(currentBKey);
//            if (data > 0 && data < EnumHammerEffect.values().length) {
//                currentB = EnumHammerEffect.values()[data];
//            }
//        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);

        writeCells(compound, slots);

        return compound;
    }

//    public static void writeModules(CompoundNBT compound, boolean[] modulesA, boolean[] modulesB, EnumHammerEffect currentA, EnumHammerEffect currentB) {
//        byte modAByte = 0;
//        for(int i = 0; i < modulesA.length ;i++) {
//            if (modulesA[i]) {
//                modAByte |= (128 >> i);
//            }
//        }
//        compound.put(modulesAKey, ByteNBT.valueOf(modAByte));
//
//        byte modBByte = 0;
//        for(int i = 0; i < modulesB.length ;i++) {
//            if (modulesA[i]) {
//                modBByte |= (128 >> i);
//            }
//        }
//        compound.put(modulesAKey, ByteNBT.valueOf(modBByte));
//
//        if (currentA != null) {
//            compound.put(currentAKey, ByteNBT.valueOf((byte) currentA.ordinal()));
//        }
//
//        if (currentB != null) {
//            compound.put(currentBKey, ByteNBT.valueOf((byte) currentB.ordinal()));
//        }
//    }

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
