package se.mickelus.tetra.items.sword;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import se.mickelus.tetra.DataHandler;
import se.mickelus.tetra.items.ItemModularHandheld;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.HandheldModuleData;

public class HeavyBladeModule extends ItemModuleMajor<HandheldModuleData> {

    public static final String key = "sword/heavy_blade";

    public static HeavyBladeModule instance;

    public HeavyBladeModule(String slotKey) {
        super(slotKey, key);

        data = DataHandler.instance.getModuleData(key, HandheldModuleData[].class);

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
        float cooldown = 1;
        if (attacker instanceof EntityPlayer) {
            cooldown = ItemModularHandheld.getCooledAttackStrength(stack);
        }

        if (cooldown > 0.9) {
            attacker.world.getEntitiesWithinAABB(EntityLivingBase.class,
                    target.getEntityBoundingBox().expand(1.0D, 0.25D, 1.0D))
                    .stream()
                    .filter(entity -> entity != attacker)
                    .filter(entity -> !attacker.isOnSameTeam(entity))
                    .filter(entity -> attacker.getDistanceSq(entity) < 9.0D)
                    .forEach(entity -> {
                        entity.knockBack(attacker, 0.4F,
                                MathHelper.sin(attacker.rotationYaw * 0.017453292F),
                                -MathHelper.cos(attacker.rotationYaw * 0.017453292F));
                        if (attacker instanceof EntityPlayer) {
                            entity.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) attacker), 3);
                        } else {
                            entity.attackEntityFrom(DamageSource.causeIndirectDamage(attacker, entity), 3);
                        }
                    });

            attacker.world.playSound(null, attacker.posX, attacker.posY, attacker.posZ,
                    SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, attacker.getSoundCategory(), 1.0F, 1.0F);
            spawnSweepParticles(attacker);
        }
    }

    private void spawnSweepParticles(EntityLivingBase attacker) {
        double d0 = (double)(-MathHelper.sin(attacker.rotationYaw * 0.017453292F));
        double d1 = (double)MathHelper.cos(attacker.rotationYaw * 0.017453292F);

        if (attacker.world instanceof WorldServer) {
            ((WorldServer)attacker.world).spawnParticle(EnumParticleTypes.SWEEP_ATTACK,
                    attacker.posX + d0 * 1.5,
                    attacker.posY + attacker.height * 0.5D,
                    attacker.posZ + d1 * 1.5,
                    0,
                    d0 * 1.5, -1, d1 * 1.5,
                    2); // particle speed is actually size in this case
        }
    }
}