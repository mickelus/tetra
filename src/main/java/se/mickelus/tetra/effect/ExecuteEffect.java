package se.mickelus.tetra.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.potion.EffectType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.util.CastOptional;

import java.util.Random;

public class ExecuteEffect extends ChargedAbilityEffect {

    public static final ExecuteEffect instance = new ExecuteEffect();

    ExecuteEffect() {
        super(20, 0.5f, 40, 8, ItemEffect.execute, TargetRequirement.entity, UseAction.SPEAR, "raised");
    }

    @Override
    public void perform(PlayerEntity attacker, Hand hand, ItemModularHandheld item, ItemStack itemStack, LivingEntity target, Vector3d hitVec, int chargedTicks) {
        target.applyKnockback(0.5f, target.getPosX() - attacker.getPosX(), target.getPosZ() - attacker.getPosZ());
        long harmfulCount = target.getActivePotionEffects().stream()
                .filter(effect -> effect.getPotion().getEffectType() == EffectType.HARMFUL)
                .count();

        float missingHealth = MathHelper.clamp(1 - target.getHealth() / target.getMaxHealth(), 0, 1);

        double damageMultiplier = (1 + missingHealth + harmfulCount * 0.1);
        item.hitEntity(itemStack, attacker, target, damageMultiplier, 0.5f, 0.2f);

        target.getEntityWorld().playSound(attacker, target.getPosition(), SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.PLAYERS, 1, 0.8f);

        Random rand = target.getRNG();
        CastOptional.cast(target.world, ServerWorld.class).ifPresent(world ->
                world.spawnParticle(new RedstoneParticleData(0.6f, 0, 0, 0.8f),
                        hitVec.x, hitVec.y, hitVec.z, 5 + (int) (damageMultiplier * 10),
                        rand.nextGaussian() * 0.3, rand.nextGaussian() * 0.3, rand.nextGaussian() * 0.3, 0.1f));

        attacker.addExhaustion(0.05f);
        attacker.swing(hand, false);
        attacker.getCooldownTracker().setCooldown(item, getCooldown(item, itemStack));

        item.tickProgression(attacker, itemStack, 2);
        item.applyDamage(2, itemStack, attacker);
    }
}
