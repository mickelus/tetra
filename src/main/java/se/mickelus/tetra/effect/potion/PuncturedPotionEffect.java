package se.mickelus.tetra.effect.potion;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import se.mickelus.tetra.effect.EffectHelper;

import java.util.Random;

public class PuncturedPotionEffect extends Effect {
    public static PuncturedPotionEffect instance;

    public PuncturedPotionEffect() {
        super(EffectType.HARMFUL, 0x880000);

        setRegistryName("punctured");

        addAttributesModifier(Attributes.ARMOR, "69967662-e7e9-4671-8f48-81d0de9d2098", -0.1, AttributeModifier.Operation.MULTIPLY_TOTAL);

        instance = this;
    }

    public void performEffect(LivingEntity entity, int amplifier) {
        if (!entity.getEntityWorld().isRemote) {
            Random rand = entity.getRNG();
            EquipmentSlotType slot = EquipmentSlotType.values()[2 + rand.nextInt(4)];
            ItemStack itemStack = entity.getItemStackFromSlot(slot);
            if (!itemStack.isEmpty()) {
                ((ServerWorld) entity.world).spawnParticle(new ItemParticleData(ParticleTypes.ITEM, itemStack),
                        entity.getPosX() + entity.getWidth() * (0.3 + rand.nextGaussian() * 0.4),
                        entity.getPosY() + entity.getHeight() * (0.2 + rand.nextGaussian() * 0.4),
                        entity.getPosZ() + entity.getWidth() * (0.3 + rand.nextGaussian() * 0.4),
                        10,
                        0, 0, 0, 0f);
            }
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
        double armor = gui.getMinecraft().player.getTotalArmorValue();
        double armorReduction =  armor / (1 - amp * 0.1) - armor;
        EffectHelper.renderInventoryEffectTooltip(gui, mStack, x, y, () ->
                new StringTextComponent(I18n.format("effect.tetra.punctured.tooltip", String.format("%d", amp * 10), String.format("%.1f", armorReduction))));
    }
}
