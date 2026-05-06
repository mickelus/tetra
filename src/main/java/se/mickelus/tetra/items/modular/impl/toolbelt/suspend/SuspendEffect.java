package se.mickelus.tetra.items.modular.impl.toolbelt.suspend;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.phys.Vec3;
import se.mickelus.tetra.effect.EffectHelper;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.impl.toolbelt.ToolbeltHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
public class SuspendEffect {
    private static final Set<Holder<MobEffect>> enablingEffects = Stream.concat(
            BeaconBlockEntity.BEACON_EFFECTS.stream().flatMap(java.util.Collection::stream),
            Stream.of(MobEffects.CONDUIT_POWER)
    ).collect(Collectors.toSet());

    public static void toggleSuspend(Player entity, boolean toggleOn) {
        if (toggleOn) {
            if (canSuspend(entity)) {
                Vec3 motion = entity.getDeltaMovement();
                entity.setDeltaMovement(motion.x, 0, motion.z);
                entity.hurtMarked = true;
                entity.addEffect(new MobEffectInstance(EffectHelper.effectHolder(SuspendPotionEffect.instance), 100, 0, false, false));
            }
        } else {
            entity.removeEffect(EffectHelper.effectHolder(SuspendPotionEffect.instance));
        }
    }

    public static boolean canSuspend(Player entity) {
        ItemStack itemStack = ToolbeltHelper.findToolbelt(entity);
        boolean hasEffect = !itemStack.isEmpty() && ((IModularItem) itemStack.getItem()).getEffectLevel(itemStack, ItemEffect.suspendSelf) > 0;

        return hasEffect && entity.getActiveEffects().stream()
                .filter(MobEffectInstance::isAmbient)
                .map(MobEffectInstance::getEffect)
                .anyMatch(enablingEffects::contains);
    }
}
