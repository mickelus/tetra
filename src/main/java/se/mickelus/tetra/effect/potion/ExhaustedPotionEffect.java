package se.mickelus.tetra.effect.potion;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerEvent;
import se.mickelus.tetra.effect.EffectHelper;
import se.mickelus.tetra.util.ParticleHelper;

import java.util.Random;

public class ExhaustedPotionEffect extends Effect {
    public static ExhaustedPotionEffect instance;

    public ExhaustedPotionEffect() {
        super(EffectType.HARMFUL, 0x222222);

        setRegistryName("exhausted");

        addAttributeModifier(Attributes.MOVEMENT_SPEED, "19be7b9d-fff9-4ccf-a811-0a378da5a269", -0.1, AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(Attributes.ATTACK_SPEED, "05b3352c-4c10-4c52-92ce-9dc8a679e7f0", -0.05, AttributeModifier.Operation.MULTIPLY_TOTAL);

        instance = this;
    }

    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (event.getEntityLiving().hasEffect(instance)) {
            float multiplier = 1 - (event.getEntityLiving().getEffect(instance).getAmplifier() + 1) * 0.05f;
            event.setNewSpeed(event.getNewSpeed() * multiplier);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderInventoryEffect(EffectInstance effect, DisplayEffectsScreen<?> gui, MatrixStack mStack, int x, int y, float z) {
        super.renderInventoryEffect(effect, gui, mStack, x, y, z);

        int amount = effect.getAmplifier() + 1;
        EffectHelper.renderInventoryEffectTooltip(gui, mStack, x, y, () ->
                new StringTextComponent(I18n.get("effect.tetra.exhausted.tooltip", amount * 10, amount * 5)));
    }
}
