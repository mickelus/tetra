package se.mickelus.tetra.effect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import se.mickelus.mutil.util.CastOptional;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class EnderReverbEffect {
    public static void perform(LivingEntity entity, ItemStack itemStack, double multiplier) {
        if (!entity.level.isClientSide) {
            double effectProbability = EffectHelper.getEffectEfficiency(itemStack, ItemEffect.enderReverb);
            if (effectProbability > 0
                    && !CastOptional.cast(entity, Player.class).map(Player::isCreative).orElse(false)
                    && entity.getRandom().nextDouble() < effectProbability * multiplier) {
                AABB aabb = new AABB(entity.blockPosition()).inflate(24);
                List<LivingEntity> nearbyTargets = entity.level.getEntitiesOfClass(LivingEntity.class, aabb,
                        target -> target instanceof EnderMan || target instanceof Endermite
                                || target instanceof Shulker || target instanceof EnderDragon);
                if (nearbyTargets.size() > 0) {
                    nearbyTargets.get(entity.getRandom().nextInt(nearbyTargets.size())).setLastHurtByMob(entity);
                }
            }
        }
    }
}
