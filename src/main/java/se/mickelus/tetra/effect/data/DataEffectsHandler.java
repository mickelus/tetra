package se.mickelus.tetra.effect.data;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.data.ItemEffectStore;
import se.mickelus.tetra.items.modular.IModularItem;

public class DataEffectsHandler {
    public static void applyOnHitEffects(ItemStack itemStack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel serverLevel) {
            ItemEffectContext context = new ItemEffectContext(attacker, itemStack, serverLevel, target);
            ((IModularItem) itemStack.getItem()).getEffects(itemStack).stream()
                    .flatMap(effect -> ItemEffectStore.onHitEffects.get(effect).stream())
                    .forEach(effect -> effect.outcome.perform(context.withData(ItemEffectData.calculateData(effect, context))));
        }
    }
}
