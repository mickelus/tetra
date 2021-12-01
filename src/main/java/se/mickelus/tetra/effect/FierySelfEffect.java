package se.mickelus.tetra.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class FierySelfEffect {
    public static void perform(LivingEntity entity, ItemStack itemStack, double multiplier) {
        if (!entity.level.isClientSide) {
            double fierySelfEfficiency = EffectHelper.getEffectEfficiency(itemStack, ItemEffect.fierySelf);
            if (fierySelfEfficiency > 0) {
                BlockPos pos = entity.blockPosition();
                float temperature = entity.level.getBiome(pos).getTemperature(pos);
                if (entity.getRandom().nextDouble() < fierySelfEfficiency * temperature * multiplier) {
                    entity.setSecondsOnFire(EffectHelper.getEffectLevel(itemStack, ItemEffect.fierySelf));
                }
            }
        }
    }
}
