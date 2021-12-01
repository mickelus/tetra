package se.mickelus.tetra.blocks.forged.chthonic;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;

public class ChthonicExtractorTile extends BlockEntity {
    @ObjectHolder(TetraMod.MOD_ID + ":" + ChthonicExtractorBlock.unlocalizedName)
    public static BlockEntityType<ChthonicExtractorTile> type;

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
        setChanged();
    }

    public void damage(int amount) {
        int newDamage = getDamage() + amount;

        if (newDamage < ChthonicExtractorBlock.maxDamage) {
            setDamage(newDamage);
        } else {
            level.levelEvent(null, 2001, getBlockPos(), Block.getId(level.getBlockState(getBlockPos())));
            level.setBlock(getBlockPos(), Blocks.AIR.defaultBlockState(), 2);
        }
    }


    @Override
    public void load(BlockState blockState, CompoundTag compound) {
        super.load(blockState, compound);

        if (compound.contains(damageKey)) {
            damage = compound.getInt(damageKey);
        }
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        super.save(compound);

        compound.putInt(damageKey, damage);

        return compound;
    }
}
