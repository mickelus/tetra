package se.mickelus.tetra.items.modular.impl.toolbelt.suspend;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tileentity.BeaconTileEntity;
import net.minecraft.util.math.vector.Vector3d;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.impl.toolbelt.ToolbeltHelper;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SuspendEffect {
    private static final Set<Effect> enablingEffects = Stream.concat(
            Arrays.stream(BeaconTileEntity.BEACON_EFFECTS).flatMap(Arrays::stream),
            Stream.of(Effects.CONDUIT_POWER)
    ).collect(Collectors.toSet());

    public static void toggleSuspend(PlayerEntity entity, boolean toggleOn) {
        if (toggleOn) {
            if (canSuspend(entity)) {
                Vector3d motion = entity.getDeltaMovement();
                entity.setDeltaMovement(motion.x, 0, motion.z);
                entity.hurtMarked = true;
                entity.addEffect(new EffectInstance(SuspendPotionEffect.instance, 100, 0, false, false));
            }
        } else {
            entity.removeEffect(SuspendPotionEffect.instance);
        }
    }

    public static boolean canSuspend(PlayerEntity entity) {
        ItemStack itemStack = ToolbeltHelper.findToolbelt(entity);
        boolean hasEffect = !itemStack.isEmpty() && ((IModularItem) itemStack.getItem()).getEffectLevel(itemStack, ItemEffect.suspendSelf) > 0;

        return hasEffect && entity.getActiveEffects().stream()
                .filter(EffectInstance::isAmbient)
                .map(EffectInstance::getEffect)
                .anyMatch(enablingEffects::contains);
    }
}
