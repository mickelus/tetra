package se.mickelus.tetra.effect.potion;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import se.mickelus.tetra.effect.EffectHelper;
import se.mickelus.tetra.util.ParticleHelper;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
public class PriedPotionEffect extends MobEffect {
    public static PriedPotionEffect instance;

    public PriedPotionEffect() {
        super(MobEffectCategory.HARMFUL, 0x880000);

        setRegistryName("pried");

        addAttributeModifier(Attributes.ARMOR, "8ce1d367-cb9f-48a3-a748-e6b73ef686e2", -1, AttributeModifier.Operation.ADDITION);

        instance = this;
    }

    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.getCommandSenderWorld().isClientSide) {
            ParticleHelper.spawnArmorParticles(((ServerLevel) entity.level), entity);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 10 == 0;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderInventoryEffect(MobEffectInstance effect, EffectRenderingInventoryScreen<?> gui, PoseStack mStack, int x, int y, float z) {
        super.renderInventoryEffect(effect, gui, mStack, x, y, z);

        int amount = effect.getAmplifier() + 1;
        EffectHelper.renderInventoryEffectTooltip(gui, mStack, x, y, () ->
                new TextComponent(I18n.get("effect.tetra.pried.tooltip", amount)));
    }
}
