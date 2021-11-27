package se.mickelus.tetra.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class FierySelfEffect {
    public static void perform(LivingEntity entity, ItemStack itemStack, double multiplier) {
        if (!entity.world.isRemote) {
            double fierySelfEfficiency = EffectHelper.getEffectEfficiency(itemStack, ItemEffect.fierySelf);
            if (fierySelfEfficiency > 0) {
                BlockPos pos = entity.getPosition();
                float temperature = entity.world.getBiome(pos).getTemperature(pos);
                if (entity.getRNG().nextDouble() < fierySelfEfficiency * temperature * multiplier) {
                    entity.setFire(EffectHelper.getEffectLevel(itemStack, ItemEffect.fierySelf));
                }
            }
        }
    }
}
