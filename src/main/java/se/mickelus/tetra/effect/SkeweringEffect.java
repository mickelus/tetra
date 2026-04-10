package se.mickelus.tetra.effect;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class SkeweringEffect {
    public static void onLivingDamage(LivingDamageEvent.Pre event, int skeweringLevel, ItemStack itemStack) {
        if (event.getEntity().getArmorValue() <= EffectHelper.getEffectEfficiency(itemStack, ItemEffect.skewering)) {
            event.setNewDamage(event.getNewDamage() + skeweringLevel);
        }
    }
}
