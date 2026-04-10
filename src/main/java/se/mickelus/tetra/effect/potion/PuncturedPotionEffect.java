package se.mickelus.tetra.effect.potion;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class PuncturedPotionEffect extends MobEffect {
    public static final String identifier = "punctured";
    public static PuncturedPotionEffect instance;

    public PuncturedPotionEffect() {
        super(MobEffectCategory.HARMFUL, 0x880000);

        addAttributeModifier(Attributes.ARMOR, ResourceLocation.fromNamespaceAndPath("tetra", "punctured_armor"), -0.05,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        instance = this;
    }

    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.getCommandSenderWorld().isClientSide) {
            RandomSource rand = entity.getRandom();
            EquipmentSlot slot = EquipmentSlot.values()[2 + rand.nextInt(4)];
            ItemStack itemStack = entity.getItemBySlot(slot);
            if (!itemStack.isEmpty()) {
                ((ServerLevel) entity.level()).sendParticles(new ItemParticleOption(ParticleTypes.ITEM, itemStack),
                        entity.getX() + entity.getBbWidth() * (0.3 + rand.nextGaussian() * 0.4),
                        entity.getY() + entity.getBbHeight() * (0.2 + rand.nextGaussian() * 0.4),
                        entity.getZ() + entity.getBbWidth() * (0.3 + rand.nextGaussian() * 0.4),
                        10,
                        0, 0, 0, 0f);
            }
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 10 == 0;
    }
}
