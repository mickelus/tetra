package se.mickelus.tetra.items.sword;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.ArrayUtils;
import se.mickelus.tetra.DataHandler;
import se.mickelus.tetra.PotionBleeding;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.ItemModularHandheld;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.HandheldModuleData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

public class ShortBladeModule extends ItemModuleMajor<HandheldModuleData> {

    public static final String key = "sword/short_blade";

    public static final String hookedImprovement = "short_blade/hooked";
    public static final String temperedImprovement = "short_blade/tempered";
    public static final String serratedImprovement = "short_blade/serrated";

    public static ShortBladeModule instance;

    public ShortBladeModule(String slotKey) {
        super(slotKey, key);

        data = DataHandler.instance.getModuleData(key, HandheldModuleData[].class);

        improvements = new String[] {
            hookedImprovement,
            temperedImprovement,
            serratedImprovement
        };

        instance = this;
        ItemUpgradeRegistry.instance.registerModule(key, this);
    }

    @Override
    public void hitEntity(ItemStack itemStack, EntityLivingBase target, EntityLivingBase attacker) {
        int hookedLevel = getImprovementLevel(hookedImprovement, itemStack);
        int serratedLevel = getImprovementLevel(serratedImprovement, itemStack);
        int temperedLevel = getImprovementLevel(temperedImprovement, itemStack);

        if (hookedLevel > 0) {
            backstabEntity(itemStack, target, attacker);
        }
        if (serratedLevel > 0) {
            if (!EnumCreatureAttribute.UNDEAD.equals(target.getCreatureAttribute()) && Math.random() > 0.7f) {
                target.addPotionEffect(new PotionEffect(PotionBleeding.instance, 40, serratedLevel));
            }
        }
        if (temperedLevel > 0 && ItemModularHandheld.getCooledAttackStrength(itemStack) > 0.9) {
            penetrateEntityArmor(itemStack, target, attacker);
        }
    }

    private void backstabEntity(ItemStack itemStack, EntityLivingBase target, EntityLivingBase attacker) {
        if (180 - Math.abs(Math.abs(attacker.rotationYawHead - target.rotationYawHead) - 180) < 60) {
            // + 1 as it is the default bonus damage dealt by players
            // todo: use damage attribute from attacker
            float damage = (float) (ItemModularHandheld.getDamageModifierStatic(itemStack) + 1) * 2;
            attacker.world.playSound(null, attacker.posX, attacker.posY, attacker.posZ,
                SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, attacker.getSoundCategory(), 1.0F, 1.0F);
            if (attacker instanceof EntityPlayer) {
                ((EntityPlayer) attacker).onCriticalHit(target);
                target.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) attacker), damage);
            } else {
                target.attackEntityFrom(DamageSource.causeMobDamage(attacker), damage);
            }
        }
    }

    private void penetrateEntityArmor(ItemStack itemStack, EntityLivingBase target, EntityLivingBase attacker) {
        if (target.getLastDamageSource() != null && attacker.equals(target.getLastDamageSource().getTrueSource())) {
            // + 1 as it is the default bonus damage dealt by players
            // todo: use damage attribute from attacker
            float damage = (float) (ItemModularHandheld.getDamageModifierStatic(itemStack) + 1);
            float reducedDamage = CombatRules.getDamageAfterAbsorb(damage, (float)target.getTotalArmorValue(), (float)target.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
            if (reducedDamage < damage / 2) {
                damage = damage / 2 - reducedDamage;
                target.hurtResistantTime = 0;
                if (attacker instanceof EntityPlayer) {
                    target.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer)attacker).setDamageBypassesArmor(), damage);
                } else {
                    target.attackEntityFrom(DamageSource.causeMobDamage(attacker).setDamageBypassesArmor(), damage);
                }
            }
        }
    }

    @Override
    public ResourceLocation[] getAllTextures() {
        return Stream.concat(
                Arrays.stream(super.getAllTextures()),
                Arrays.stream(improvements)
                        .map(improvement -> "items/" + improvement)
                        .map(resourceString -> new ResourceLocation(TetraMod.MOD_ID, resourceString)))
                .toArray(ResourceLocation[]::new);
    }

    @Override
    public ResourceLocation[] getTextures(ItemStack itemStack) {
        return Stream.concat(
                Arrays.stream(super.getTextures(itemStack)),
                Arrays.stream(getImprovements(itemStack))
                        .map(improvement -> "items/" + improvement)
                        .map(resourceString -> new ResourceLocation(TetraMod.MOD_ID, resourceString)))
                .toArray(ResourceLocation[]::new);
    }
}
