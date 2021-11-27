package se.mickelus.tetra.effect.potion;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import se.mickelus.tetra.effect.EffectHelper;
import se.mickelus.tetra.util.CastOptional;

import java.util.Random;

public class SeveredPotionEffect extends Effect {
    public static SeveredPotionEffect instance;
    public SeveredPotionEffect() {
        super(EffectType.HARMFUL, 0x880000);

        setRegistryName("severed");

        addAttributesModifier(Attributes.MAX_HEALTH, "7e68e993-e133-41c0-aea3-703afc401831", -0.1, AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributesModifier(Attributes.ATTACK_DAMAGE, "3ca939c9-62fe-41a6-a722-22235066f808", -0.05, AttributeModifier.Operation.MULTIPLY_TOTAL);

        instance = this;
    }

    public void performEffect(LivingEntity entity, int amplifier) {
        if (!entity.getEntityWorld().isRemote) {
            Random rand = entity.getRNG();
            ((ServerWorld) entity.world).spawnParticle(new RedstoneParticleData(0.5f, 0, 0, 0.5f),
                    entity.getPosX() + entity.getWidth() * (0.3 + rand.nextGaussian() * 0.4),
                    entity.getPosY() + entity.getHeight() * (0.2 + rand.nextGaussian() * 0.4),
                    entity.getPosZ() + entity.getWidth() * (0.3 + rand.nextGaussian() * 0.4),
                    20,
                    0, 0, 0, 0f);
        }
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return duration % 10 == 0;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderInventoryEffect(EffectInstance effect, DisplayEffectsScreen<?> gui, MatrixStack mStack, int x, int y, float z) {
        super.renderInventoryEffect(effect, gui, mStack, x, y, z);

        int amp = effect.getAmplifier() + 1;
        EffectHelper.renderInventoryEffectTooltip(gui, mStack, x, y, () ->
                new StringTextComponent(I18n.format("effect.tetra.severed.tooltip", String.format("%d", amp * 10), String.format("%d", amp * 5))));
    }
}
