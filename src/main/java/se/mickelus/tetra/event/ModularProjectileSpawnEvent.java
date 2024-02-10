package se.mickelus.tetra.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;

public class ModularProjectileSpawnEvent extends Event {
    private final ItemStack firingStack;
    private final ItemStack ammoStack;
    private final LivingEntity shooter;
    private final AbstractArrow projectileEntity;
    private final Level level;
    private final int drawProgress;

    public ModularProjectileSpawnEvent(ItemStack firingStack, ItemStack ammoStack, LivingEntity shooter, AbstractArrow projectileEntity, Level level, int drawProgress) {
        this.firingStack = firingStack;
        this.ammoStack = ammoStack;
        this.shooter = shooter;
        this.projectileEntity = projectileEntity;
        this.level = level;
        this.drawProgress = drawProgress;
    }

    public ItemStack getFiringStack() {
        return firingStack;
    }

    public ItemStack getAmmoStack() {
        return ammoStack;
    }

    public LivingEntity getShooter() {
        return shooter;
    }

    public int getDrawProgress() {
        return drawProgress;
    }

    public Level getLevel() {
        return level;
    }

    public AbstractArrow getProjectileEntity() {
        return projectileEntity;
    }
}
