package se.mickelus.tetra.effect.data;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.data.ItemEffectStore;
import se.mickelus.tetra.items.modular.IModularItem;

public class DataEffectsHandler {
    public static void applyOnHitEffects(ItemStack itemStack, LivingEntity target, LivingEntity attacker) {
        ItemEffectContext context = new ItemEffectContext(attacker, itemStack, attacker.level(), target, null, null);
        ((IModularItem) itemStack.getItem()).getEffects(itemStack).stream()
                .flatMap(effect -> ItemEffectStore.onHitEffects.get(effect).stream())
                .forEach(effect -> effect.outcome.perform(context));
    }
}
