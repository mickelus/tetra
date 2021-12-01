package se.mickelus.tetra.effect.potion;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import se.mickelus.tetra.effect.EffectHelper;

import java.util.Random;

public class PuncturedPotionEffect extends MobEffect {
    public static PuncturedPotionEffect instance;

    public PuncturedPotionEffect() {
        super(MobEffectCategory.HARMFUL, 0x880000);

        setRegistryName("punctured");

        addAttributeModifier(Attributes.ARMOR, "69967662-e7e9-4671-8f48-81d0de9d2098", -0.05, AttributeModifier.Operation.MULTIPLY_TOTAL);

        instance = this;
    }

    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.getCommandSenderWorld().isClientSide) {
            Random rand = entity.getRandom();
            EquipmentSlot slot = EquipmentSlot.values()[2 + rand.nextInt(4)];
            ItemStack itemStack = entity.getItemBySlot(slot);
            if (!itemStack.isEmpty()) {
                ((ServerLevel) entity.level).sendParticles(new ItemParticleOption(ParticleTypes.ITEM, itemStack),
                        entity.getX() + entity.getBbWidth() * (0.3 + rand.nextGaussian() * 0.4),
                        entity.getY() + entity.getBbHeight() * (0.2 + rand.nextGaussian() * 0.4),
                        entity.getZ() + entity.getBbWidth() * (0.3 + rand.nextGaussian() * 0.4),
                        10,
                        0, 0, 0, 0f);
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
        super.renderInventoryEffect(effect, gui, mStack, x, y, z);

        int amp = effect.getAmplifier() + 1;
        double armor = gui.getMinecraft().player.getArmorValue();
        double armorReduction =  armor / (1 - amp * 0.1) - armor;
        EffectHelper.renderInventoryEffectTooltip(gui, mStack, x, y, () ->
                new TextComponent(I18n.get("effect.tetra.punctured.tooltip", String.format("%d", amp * 10), String.format("%.1f", armorReduction))));
    }
}
