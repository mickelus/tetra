package se.mickelus.tetra.effect.potion;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.EffectRenderer;
import se.mickelus.tetra.TetraMod;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class EarthboundPotionEffect extends MobEffect {
    public static EarthboundPotionEffect instance;

    public static final ResourceLocation texture = new ResourceLocation(TetraMod.MOD_ID,"textures/gui/potions.png");

    public EarthboundPotionEffect() {
        super(MobEffectCategory.HARMFUL, 0x006600);
        setRegistryName("earthbound");

        addAttributeModifier(Attributes.MOVEMENT_SPEED, "dc6d6b51-a5da-4735-9277-41fd355829f5", -0.3, AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE, "4134bd78-8b75-46fe-bd9e-cbddff983181", 1, AttributeModifier.Operation.MULTIPLY_TOTAL);

        instance = this;
    }


    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<EffectRenderer> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new EffectRenderer() {
            @Override
            public void renderInventoryEffect(MobEffectInstance effect, EffectRenderingInventoryScreen<?> gui, PoseStack mStack, int x, int y, float z) {

            }

            @Override
            public void renderHUDEffect(MobEffectInstance effect, GuiComponent gui, PoseStack matrixStack, int x, int y, float z, float alpha) {
                Minecraft.getInstance().getTextureManager().bindForSetup(texture);
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
                GlStateManager._enableBlend();
                gui.blit(matrixStack, x + 4, y + 4, 0, 0, 16, 16);
            }
        });
    }

}
