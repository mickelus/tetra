package se.mickelus.tetra;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;

public class PotionEarthbound extends Potion {
    public static PotionEarthbound instance;

    public static final ResourceLocation texture = new ResourceLocation(TetraMod.MOD_ID,"textures/gui/potions.png");

    protected PotionEarthbound() {
        super(true, 0);
        setRegistryName("earthbound");
        setPotionName("earthbound");
        registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED, "dc6d6b51-a5da-4735-9277-41fd355829f5", -0.3, 2);
        registerPotionAttributeModifier(SharedMonsterAttributes.KNOCKBACK_RESISTANCE, "4134bd78-8b75-46fe-bd9e-cbddff983181", 1, 2);

        instance = this;
    }

    @Override
    public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
        if (mc.currentScreen != null) {
            mc.getTextureManager().bindTexture(texture);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableBlend();
            mc.currentScreen.drawTexturedModalRect(x + 8, y + 8, 0, 0, 16, 16);
        }
    }

    @Override
    public void renderHUDEffect(int x, int y, PotionEffect effect, Minecraft mc, float alpha) {
        mc.getTextureManager().bindTexture(texture);
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
        GlStateManager.enableBlend();
        mc.ingameGUI.drawTexturedModalRect(x + 4, y + 4, 0, 0, 16, 16);
    }
}
