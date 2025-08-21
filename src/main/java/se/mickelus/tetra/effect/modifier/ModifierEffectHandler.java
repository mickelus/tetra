package se.mickelus.tetra.effect.modifier;

import com.google.common.collect.ImmutableMap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import se.mickelus.tetra.data.ModifierEffectStore;
import se.mickelus.tetra.effect.EffectHelper;
import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.ItemEffectData;
import se.mickelus.tetra.effect.potion.UnstablePowerMobEffect;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.ItemModularHandheld;

import java.util.List;
import java.util.Optional;

public class ModifierEffectHandler {

    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (event.getPosition().isPresent()) {
            Optional.of(event.getEntity().getMainHandItem())
                    .filter(itemStack -> !itemStack.isEmpty())
                    .filter(itemStack -> itemStack.getItem() instanceof ItemModularHandheld)
                    .ifPresent(itemStack -> {
                        List<ModifierEffect> presentEffects = ((IModularItem) itemStack.getItem()).getEffects(itemStack).stream()
                                .flatMap(effect -> ModifierEffectStore.breakSpeedModifiers.get(effect).stream())
                                .toList();

                        ItemEffectContext context = null;
                        if (!presentEffects.isEmpty()) {
                            context = new ItemEffectContext(event.getEntity(), itemStack, event.getEntity().level())
                                    .withNumbers(ImmutableMap.of("unmodifiedSpeed", event.getOriginalSpeed(), "speed", event.getNewSpeed()))
                                    .withEntities(ImmutableMap.of("miner", event.getEntity()))
                                    .withVectors(ImmutableMap.of("target",
                                            event.getPosition().map(Vec3::atLowerCornerOf).orElse(event.getEntity().position())));
                        }

                        for (ModifierEffect effect : presentEffects) {
                            ItemEffectContext localContext = context.withMergedNumbers(ImmutableMap.of(
                                    "level", (float) EffectHelper.getEffectLevel(itemStack, effect.effect()),
                                    "efficiency", EffectHelper.getEffectEfficiency(itemStack, effect.effect())));
                            if (effect.data() != null) {
                                localContext = localContext.withMergedNumbers(ItemEffectData.calculateNumbers(effect.data(), localContext));
                                localContext = localContext.withMergedVectors(ItemEffectData.calculateVectors(effect.data(), localContext));
                                localContext = localContext.withMergedEntities(ItemEffectData.calculateEntities(effect.data(), localContext));
                            }
                            if (effect.condition() == null || effect.condition().test(localContext)) {
                                context = context.withMergedNumbers(ImmutableMap.of("speed", effect.result().getValue(localContext)));
                            }
                        }

                        if (context != null) {
                            event.setNewSpeed(context.getNumbers().get("speed"));
                        }
                    });
        }
    }

    public static void onLivingDamage(ItemStack itemStack, LivingDamageEvent event) {
        List<ModifierEffect> presentEffects = ((IModularItem) itemStack.getItem()).getEffects(itemStack).stream()
                .flatMap(effect -> ModifierEffectStore.hitDamageModifiers.get(effect).stream())
                .toList();

        ItemEffectContext context = null;
        if (!presentEffects.isEmpty()) {
            context = new ItemEffectContext(event.getEntity(), itemStack, event.getEntity().level())
                    .withNumbers(ImmutableMap.of("damage", event.getAmount()))
                    .withEntities(ImmutableMap.of("attacker", event.getSource().getEntity(), "target", event.getEntity()));
        }

        for (ModifierEffect effect : presentEffects) {
            try {
                ItemEffectContext localContext = context.withMergedNumbers(ImmutableMap.of(
                        "level", (float) EffectHelper.getEffectLevel(itemStack, effect.effect),
                        "efficiency", EffectHelper.getEffectEfficiency(itemStack, effect.effect)));
                if (effect.data != null) {
                    localContext = localContext.withMergedNumbers(ItemEffectData.calculateNumbers(effect.data, localContext));
                    localContext = localContext.withMergedVectors(ItemEffectData.calculateVectors(effect.data, localContext));
                    localContext = localContext.withMergedEntities(ItemEffectData.calculateEntities(effect.data, localContext));
                }
                if (effect.condition == null || effect.condition.test(localContext)) {
                    context = context.withMergedNumbers(ImmutableMap.of("damage", effect.result.getValue(localContext)));
                }
            } catch (Exception e) {
                logger.error("An error occured when calculating living damage for modifier effect '{}': {}", effect.key, e.getMessage());
                logger.debug(e.getMessage(), e);
            }
        }

        if (context != null) {
            event.setAmount(context.getNumbers().get("damage"));
        }
    }
}
