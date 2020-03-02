package se.mickelus.tetra.effects;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import se.mickelus.tetra.TetraMod;

public class EarthboundEffect extends Effect {
    public static EarthboundEffect instance;

    public static final ResourceLocation texture = new ResourceLocation(TetraMod.MOD_ID,"textures/gui/potions.png");

    public EarthboundEffect() {
        super(EffectType.HARMFUL, 0x006600);
        setRegistryName("earthbound");

        addAttributesModifier(SharedMonsterAttributes.MOVEMENT_SPEED, "dc6d6b51-a5da-4735-9277-41fd355829f5", -0.3, AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributesModifier(SharedMonsterAttributes.KNOCKBACK_RESISTANCE, "4134bd78-8b75-46fe-bd9e-cbddff983181", 1, AttributeModifier.Operation.MULTIPLY_TOTAL);

        instance = this;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderInventoryEffect(EffectInstance effect, DisplayEffectsScreen<?> gui, int x, int y, float z) {
        gui.getMinecraft().getTextureManager().bindTexture(texture);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        gui.blit(x + 8, y + 8, 0, 0, 16, 16);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderHUDEffect(EffectInstance effect, AbstractGui gui, int x, int y, float z, float alpha) {
        Minecraft.getInstance().getTextureManager().bindTexture(texture);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, alpha);
        GlStateManager.enableBlend();
        gui.blit(x + 4, y + 4, 0, 0, 16, 16);
    }
}
