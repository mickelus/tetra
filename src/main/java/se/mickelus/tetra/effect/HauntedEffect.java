package se.mickelus.tetra.effect;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.util.CastOptional;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public class HauntedEffect {
    public static void perform(LivingEntity entity, ItemStack itemStack, double multiplier) {
        if (!entity.level.isClientSide) {
            double effectProbability = EffectHelper.getEffectEfficiency(itemStack, ItemEffect.haunted);
            if (effectProbability > 0) {
                if (entity.getRandom().nextDouble() < effectProbability * multiplier) {
                    int effectLevel = EffectHelper.getEffectLevel(itemStack, ItemEffect.haunted);

                    Vex vex = EntityType.VEX.create(entity.level);
                    vex.setLimitedLife(effectLevel * 20);
                    vex.moveTo(entity.getX(), entity.getY() + 1, entity.getZ(), entity.yRot, 0.0F);
                    vex.setItemInHand(InteractionHand.MAIN_HAND, itemStack.copy());
                    vex.setDropChance(EquipmentSlot.MAINHAND, 0);
                    vex.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 2000 + effectLevel * 20));
                    entity.level.addFreshEntity(vex);

                    // todo: use temporary modules for this instead once implemented
                    CastOptional.cast(itemStack.getItem(), IModularItem.class)
                            .map(item -> Arrays.stream(item.getMajorModules(itemStack)))
                            .orElse(Stream.empty())
                            .filter(Objects::nonNull)
                            .filter(module -> module.getImprovement(itemStack, ItemEffect.hauntedKey) != null)
                            .findAny()
                            .ifPresent(module -> {
                                int level = module.getImprovementLevel(itemStack, ItemEffect.hauntedKey);
                                if (level > 0) {
                                    module.addImprovement(itemStack, ItemEffect.hauntedKey, level - 1);
                                } else {
                                    module.removeImprovement(itemStack, ItemEffect.hauntedKey);
                                }
                            });

                    entity.level.playSound(null, entity.blockPosition(), SoundEvents.WITCH_AMBIENT, SoundSource.PLAYERS, 2f, 2);
                }
            }
        }
    }
}
