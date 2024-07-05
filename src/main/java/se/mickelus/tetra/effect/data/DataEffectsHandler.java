package se.mickelus.tetra.effect.data;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.data.ItemEffectStore;
import se.mickelus.tetra.items.modular.IModularItem;

public class DataEffectsHandler {
    public static void applyOnHitEffects(ItemStack itemStack, LivingEntity target, LivingEntity attacker) {
        ItemEffectContext context = new ItemEffectContext(attacker, itemStack, attacker.level(), target);
        ((IModularItem) itemStack.getItem()).getEffects(itemStack).stream()
                .flatMap(effect -> ItemEffectStore.onHitEffects.get(effect).stream())
                .forEach(effect -> effect.outcome.perform(context.withData(calculateData(effect, context))));
    }

    private static Map<String, Float> calculateData(ItemEffectData effectData, ItemEffectContext context) {
        return Optional.ofNullable(effectData.data)
                .map(Map::entrySet)
                .orElseGet(Collections::emptySet)
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getValue(context)));
    }
}
