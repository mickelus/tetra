package se.mickelus.tetra.effect;

import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import se.mickelus.tetra.items.modular.ModularItem;

public class SkeweringEffect {
    public static void onLivingDamage(LivingDamageEvent event, int skeweringLevel, ItemStack itemStack) {
        if (event.getEntityLiving().getTotalArmorValue() <= EffectHelper.getEffectEfficiency(itemStack, ItemEffect.skewering)) {
            event.setAmount(event.getAmount()  + skeweringLevel);
        }
    }
}
