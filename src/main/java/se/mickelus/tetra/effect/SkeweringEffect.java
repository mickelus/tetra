package se.mickelus.tetra.effect;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDamageEvent;

public class SkeweringEffect {
    public static void onLivingDamage(LivingDamageEvent event, int skeweringLevel, ItemStack itemStack) {
        if (event.getEntityLiving().getArmorValue() <= EffectHelper.getEffectEfficiency(itemStack, ItemEffect.skewering)) {
            event.setAmount(event.getAmount()  + skeweringLevel);
        }
    }
}
