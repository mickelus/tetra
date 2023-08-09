package se.mickelus.tetra.effect;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import se.mickelus.tetra.items.modular.ItemModularHandheld;

import java.util.Optional;

public class ReachingEffect {
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (event.getPosition().isPresent()) {
            Optional.of(event.getEntity().getMainHandItem())
                    .filter(itemStack -> !itemStack.isEmpty())
                    .filter(itemStack -> itemStack.getItem() instanceof ItemModularHandheld)
                    .ifPresent(itemStack -> {
                        int level = EffectHelper.getEffectLevel(itemStack, ItemEffect.reaching);
                        if (level > 0) {
                            double distance = event.getEntity().position().distanceToSqr(Vec3.atCenterOf(event.getPosition().get()));
                            if (distance > 1) {
                                event.setNewSpeed(event.getNewSpeed() * getMultiplier(level, distance, 1));
                            }
                        }
                    });
        }
    }

    public static void onLivingDamage(LivingDamageEvent event, int level, float efficiency) {
        double distance = event.getSource().getEntity().distanceToSqr(event.getEntity());
        float multiplier = event.getSource().is(DamageTypeTags.IS_PROJECTILE)
                ? efficiency
                : 1;
        if (distance > 1) {
            event.setAmount(event.getAmount() * getMultiplier(level, distance, multiplier));
        }
    }

    public static float getMultiplier(int level, double squareDistance, float offsetMultiplier) {
        return level > 0
                ? 1 + getOffset(level, squareDistance) * offsetMultiplier
                : 1;
    }

    public static float getOffset(int level, double squareDistance) {
        return level > 0
                ? (float) (level / 100f * Math.log(squareDistance * squareDistance))
                : 0;
    }
}
