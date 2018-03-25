package se.mickelus.tetra.items;

import com.google.common.collect.Multimap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.NBTHelper;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class ItemModularHandheld extends ItemModular {

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        applyDamage(1, stack, attacker);
        getAllModules(stack).forEach(module -> module.hitEntity(stack, target, attacker));

        return true;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
        setCooledAttackStrength(stack, player.getCooledAttackStrength(0.5f));
        return false;
    }

    public void setCooledAttackStrength(ItemStack itemStack, float strength) {
        NBTHelper.getTag(itemStack).setFloat(cooledStrengthKey, strength);
    }

    public static float getCooledAttackStrength(ItemStack itemStack) {
        return NBTHelper.getTag(itemStack).getFloat(cooledStrengthKey);
    }

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack itemStack) {
        Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot, itemStack);

        if (slot == EntityEquipmentSlot.MAINHAND) {
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
                new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", getDamageModifier(itemStack), 0));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(),
                new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", getSpeedModifier(itemStack), 0));
        }

        return multimap;
    }

    public double getDamageModifier(ItemStack itemStack) {
        if (isBroken(itemStack)) {
            return 0;
        }

        double damageModifier = getAllModules(itemStack).stream()
            .map(itemModule -> itemModule.getDamageModifier(itemStack))
            .reduce(0d, Double::sum);

        damageModifier = Arrays.stream(getSynergyData(itemStack))
            .map(synergyData -> synergyData.damage)
            .reduce(damageModifier, Double::sum);

        return getAllModules(itemStack).stream()
            .map(itemModule -> itemModule.getDamageMultiplierModifier(itemStack))
            .reduce(damageModifier, (a, b) -> a*b);
    }

    public static double getDamageModifierStatic(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ItemModularHandheld) {
            return ((ItemModularHandheld) itemStack.getItem()).getDamageModifier(itemStack);
        }
        return 0;
    }

    public double getSpeedModifier(ItemStack itemStack) {
        if (isBroken(itemStack)) {
            return 2;
        }

        double speedModifier = getAllModules(itemStack).stream()
            .map(itemModule -> itemModule.getSpeedModifier(itemStack))
            .reduce(-2.4d, Double::sum);

        speedModifier = Arrays.stream(getSynergyData(itemStack))
            .map(synergyData -> synergyData.speed)
            .reduce(speedModifier, Double::sum);

        speedModifier = getAllModules(itemStack).stream()
            .map(itemModule -> itemModule.getSpeedMultiplierModifier(itemStack))
            .reduce(speedModifier, (a, b) -> a*b);

        if (speedModifier < -4) {
            speedModifier = -3.9d;
        }

        return speedModifier;
    }

    public static double getSpeedModifierStatic(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ItemModularHandheld) {
            return ((ItemModularHandheld) itemStack.getItem()).getSpeedModifier(itemStack);
        }
        return 2;
    }

    @Override
    public Set<String> getToolClasses(ItemStack itemStack) {
        if (!isBroken(itemStack)) {
            return getCapabilities(itemStack).stream()
                .map(Enum::toString)
                .collect(Collectors.toSet());
        }
        return Collections.EMPTY_SET;
    }

    @Override
    public int getHarvestLevel(ItemStack itemStack, String toolClass, @Nullable EntityPlayer player, @Nullable IBlockState blockState) {
        if (!isBroken(itemStack)) {
            int capabilityLevel = getCapabilityLevel(itemStack, toolClass);
            if (capabilityLevel > 0) {
                return capabilityLevel - 1;
            }
        }
        return -1;
    }

    @Override
    public float getDestroySpeed(ItemStack itemStack, IBlockState blockState) {
        if (!isBroken(itemStack)) {
            int capabilityLevel = getCapabilityLevel(itemStack, blockState.getBlock().getHarvestTool(blockState));
            if (capabilityLevel > 0) {
                return capabilityLevel * 2;
            }
        }
        return -1;
    }
}
