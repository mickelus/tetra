package se.mickelus.tetra.effect.potion;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.EffectRenderer;
import se.mickelus.tetra.effect.EffectHelper;
import se.mickelus.tetra.effect.gui.EffectTooltipRenderer;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class SmallStrengthPotionEffect extends MobEffect {
    public static SmallStrengthPotionEffect instance;

    public SmallStrengthPotionEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x880000);

        setRegistryName("small_strength");

        addAttributeModifier(Attributes.ATTACK_DAMAGE, "fc8d272d-056c-43b4-9d18-f3d7f6cf3983", 1, AttributeModifier.Operation.ADDITION);

        instance = this;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<EffectRenderer> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new EffectTooltipRenderer(effect -> I18n.get("effect.tetra.small_strength.tooltip", effect.getAmplifier() + 1)));
    }
}
