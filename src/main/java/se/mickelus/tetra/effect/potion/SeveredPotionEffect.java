package se.mickelus.tetra.effect.potion;

import com.mojang.math.Vector3f;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.EffectRenderer;
import se.mickelus.tetra.effect.gui.EffectTooltipRenderer;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class SeveredPotionEffect extends MobEffect {
    public static SeveredPotionEffect instance;

    public SeveredPotionEffect() {
        super(MobEffectCategory.HARMFUL, 0x880000);

        setRegistryName("severed");

        addAttributeModifier(Attributes.MAX_HEALTH, "7e68e993-e133-41c0-aea3-703afc401831", -0.1, AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(Attributes.ATTACK_DAMAGE, "3ca939c9-62fe-41a6-a722-22235066f808", -0.05, AttributeModifier.Operation.MULTIPLY_TOTAL);

        instance = this;
    }

    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.getCommandSenderWorld().isClientSide) {
            Random rand = entity.getRandom();
            ((ServerLevel) entity.level).sendParticles(new DustParticleOptions(new Vector3f(0.5f, 0, 0), 0.5f),
                    entity.getX() + entity.getBbWidth() * (0.3 + rand.nextGaussian() * 0.4),
                    entity.getY() + entity.getBbHeight() * (0.2 + rand.nextGaussian() * 0.4),
                    entity.getZ() + entity.getBbWidth() * (0.3 + rand.nextGaussian() * 0.4),
                    20,
                    0, 0, 0, 0f);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 10 == 0;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<EffectRenderer> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new EffectTooltipRenderer(effect -> {
            int amp = effect.getAmplifier() + 1;
            return I18n.get("effect.tetra.severed.tooltip", String.format("%d", amp * 10), String.format("%d", amp * 5));
        }));
    }
}
