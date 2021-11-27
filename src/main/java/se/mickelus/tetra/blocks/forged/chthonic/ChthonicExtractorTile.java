package se.mickelus.tetra.blocks.forged.chthonic;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;

public class ChthonicExtractorTile extends TileEntity {
    @ObjectHolder(TetraMod.MOD_ID + ":" + ChthonicExtractorBlock.unlocalizedName)
    public static TileEntityType<ChthonicExtractorTile> type;

    private static final String damageKey = "dmg";
    private int damage = 0;

    public ChthonicExtractorTile() {
        super(type);
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
        markDirty();
    }

    public void damage(int amount) {
        int newDamage = getDamage() + amount;

        if (newDamage < ChthonicExtractorBlock.maxDamage) {
            setDamage(newDamage);
        } else {
            world.playEvent(null, 2001, getPos(), Block.getStateId(world.getBlockState(getPos())));
            world.setBlockState(getPos(), Blocks.AIR.getDefaultState(), 2);
        }
    }


    @Override
    public void read(BlockState blockState, CompoundNBT compound) {
        super.read(blockState, compound);

        if (compound.contains(damageKey)) {
            damage = compound.getInt(damageKey);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);

        compound.putInt(damageKey, damage);

        return compound;
    }
}
