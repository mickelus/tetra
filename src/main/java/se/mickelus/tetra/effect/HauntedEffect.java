package se.mickelus.tetra.effect;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.VexEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.util.CastOptional;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public class HauntedEffect {
    public static void perform(LivingEntity entity, ItemStack itemStack, double multiplier) {
        if (!entity.world.isRemote) {
            double effectProbability = EffectHelper.getEffectEfficiency(itemStack, ItemEffect.haunted);
            if (effectProbability > 0) {
                if (entity.getRNG().nextDouble() < effectProbability * multiplier) {
                    int effectLevel = EffectHelper.getEffectLevel(itemStack, ItemEffect.haunted);

                    VexEntity vex = EntityType.VEX.create(entity.world);
                    vex.setLimitedLife(effectLevel * 20);
                    vex.setLocationAndAngles(entity.getPosX(), entity.getPosY() + 1, entity.getPosZ(), entity.rotationYaw, 0.0F);
                    vex.setHeldItem(Hand.MAIN_HAND, itemStack.copy());
                    vex.setDropChance(EquipmentSlotType.MAINHAND, 0);
                    vex.addPotionEffect(new EffectInstance(Effects.INVISIBILITY, 2000 + effectLevel * 20));
                    entity.world.addEntity(vex);

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

                    entity.world.playSound(null, entity.getPosition(), SoundEvents.ENTITY_WITCH_AMBIENT, SoundCategory.PLAYERS, 2f, 2);
                }
            }
        }
    }
}
