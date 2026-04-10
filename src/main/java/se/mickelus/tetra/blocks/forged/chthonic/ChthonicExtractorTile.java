package se.mickelus.tetra.blocks.forged.chthonic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import se.mickelus.tetra.compat.forge.registries.RegistryObject;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ChthonicExtractorTile extends BlockEntity {
    private static final String damageKey = "dmg";
    public static RegistryObject<BlockEntityType<ChthonicExtractorTile>> type;
    private int damage = 0;

    public ChthonicExtractorTile(BlockPos p_155268_, BlockState p_155269_) {
        super(type.get(), p_155268_, p_155269_);
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
    protected void loadAdditional(CompoundTag compound, HolderLookup.Provider registries) {
        super.loadAdditional(compound, registries);

        if (compound.contains(damageKey)) {
            damage = compound.getInt(damageKey);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compound, HolderLookup.Provider registries) {
        super.saveAdditional(compound, registries);
        compound.putInt(damageKey, damage);
    }
}
