package se.mickelus.tetra.items.sword;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import se.mickelus.tetra.DataHandler;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.WeaponModuleData;

public class ShortBladeModule extends ItemModuleMajor<WeaponModuleData> {

    public static final String key = "sword/short_blade";

    public static final String hookedImprovement = key + ".hooked";
    public static final String temperedImprovement = key + ".tempered";
    public static final String serratedImprovement = key + ".serrated";

    public static ShortBladeModule instance;

    public ShortBladeModule(String slotKey) {
        super(slotKey, key);

        data = DataHandler.instance.getModuleData(key, WeaponModuleData[].class);

        improvements = new String[] {
            hookedImprovement,
            temperedImprovement,
            serratedImprovement
        };

        instance = this;
        ItemUpgradeRegistry.instance.registerModule(key, this);
    }

    @Override
    public double getDamageModifier(ItemStack itemStack) {
        return getData(itemStack).damage;
    }

    @Override
    public double getSpeedModifier(ItemStack itemStack) {
        return getData(itemStack).attackSpeed;
    }

    @Override
    public void hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        if (getImprovementLevel(hookedImprovement, stack) > 0) {
            backstabEntity(stack, target, attacker);
        }
    }

    private void backstabEntity(ItemStack itemStack, EntityLivingBase target, EntityLivingBase attacker) {
        if (180 - Math.abs(Math.abs(attacker.rotationYawHead - target.rotationYawHead) - 180) < 60) {
            attacker.world.playSound(null, attacker.posX, attacker.posY, attacker.posZ,
                SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, attacker.getSoundCategory(), 1.0F, 1.0F);
            if (attacker instanceof EntityPlayer) {
                ((EntityPlayer) attacker).onCriticalHit(target);
                target.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) attacker), 1);
            } else {
                target.attackEntityFrom(DamageSource.causeMobDamage(attacker), 1);
            }
        }
    }
}
