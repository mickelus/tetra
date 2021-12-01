package se.mickelus.tetra.effect.howling;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import se.mickelus.tetra.effect.EffectHelper;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
public class HowlingPotionEffect extends MobEffect {
    public static HowlingPotionEffect instance;

    public HowlingPotionEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xeeeeee);

        setRegistryName("howling");

        addAttributeModifier(Attributes.MOVEMENT_SPEED, "f80b9432-480d-4846-b9f9-178157dbac07", -0.05, AttributeModifier.Operation.MULTIPLY_BASE);
        instance = this;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level.isClientSide) {
            double offset = Math.PI * 4 / (amplifier + 1);
            for (int i = 0; i < (amplifier + 1) / 2; i++) {
                double time = System.currentTimeMillis() / 1000d * Math.PI + offset * i;
                double xOffset = -Math.cos(time);
                double zOffset = Math.sin(time);
                Vec3 pos = entity.position().add(xOffset, 0.1 + Math.random() * entity.getBbHeight(), zOffset);
                entity.getCommandSenderWorld().addParticle(ParticleTypes.POOF, pos.x, pos.y, pos.z, -Math.cos(time - Math.PI / 2) * 0.1, 0.01, Math.sin(time - Math.PI / 2) * 0.1);
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 10 == 0;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderInventoryEffect(MobEffectInstance effect, EffectRenderingInventoryScreen<?> gui, PoseStack mStack, int x, int y, float z) {
        int amp = effect.getAmplifier() + 1;
        EffectHelper.renderInventoryEffectTooltip(gui, mStack, x, y, () ->
                new TextComponent(I18n.get("effect.tetra.howling.tooltip",
                        String.format("%d", amp * -5), String.format("%.01f", Math.min(amp * 12.5, 100)), String.format("%.01f", amp * 2.5))));
    }
}
