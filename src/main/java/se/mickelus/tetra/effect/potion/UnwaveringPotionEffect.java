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
public class UnwaveringPotionEffect extends MobEffect {
    public static UnwaveringPotionEffect instance;

    public UnwaveringPotionEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x887700);

        setRegistryName("unwavering");

        addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE, "6531461a-9c46-4fb9-8c84-002f0b37def1", 1, AttributeModifier.Operation.ADDITION);

        instance = this;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<EffectRenderer> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new EffectTooltipRenderer(effect -> I18n.get("effect.tetra.unwavering.tooltip")));
    }
}
