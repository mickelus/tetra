package se.mickelus.tetra.effect.potion;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;
import se.mickelus.mutil.effect.EffectTooltipRenderer;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class SatiatedPotionEffect extends MobEffect {
    public static final String identifier = "satiated";
    public static SatiatedPotionEffect instance;

    public SatiatedPotionEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xffffff);

        instance = this;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new EffectTooltipRenderer(effect -> I18n.get("effect.tetra.satiated.tooltip", effect.getAmplifier() + 1)));
    }
}
