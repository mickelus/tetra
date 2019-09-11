package se.mickelus.tetra.module.improvement;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.gui.impl.GuiColors;
import se.mickelus.tetra.module.schema.SchemaRarity;

public class HoneToast implements IToast {
    private static final ResourceLocation texture = new ResourceLocation(TetraMod.MOD_ID,"textures/gui/toasts.png");

    private boolean hasPlayedSound = false;
    private ItemStack itemStack;

    public HoneToast(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public IToast.Visibility draw(GuiToast toastGui, long delta) {
        if (itemStack != null) {
            toastGui.getMinecraft().getTextureManager().bindTexture(texture);
            GlStateManager.color(1.0F, 1.0F, 1.0F);
            toastGui.drawTexturedModalRect(0, 0, 0, 0, 160, 32);

            String itemName = toastGui.getMinecraft().fontRenderer.trimStringToWidth(itemStack.getDisplayName(), 125);
            toastGui.getMinecraft().fontRenderer.drawString(I18n.format("hone.available"), 30, 7, SchemaRarity.hone.tint);
            toastGui.getMinecraft().fontRenderer.drawString(itemName, 30, 18, GuiColors.muted);

            if (!this.hasPlayedSound && delta > 0L) {
                this.hasPlayedSound = true;
                toastGui.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_PLAYER_LEVELUP, 0.6F, 0.7F));
            }

            RenderHelper.enableGUIStandardItemLighting();
            toastGui.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(null, itemStack, 8, 8);

            return delta > 5000 ? IToast.Visibility.HIDE : IToast.Visibility.SHOW;
        }

        return IToast.Visibility.HIDE;
    }
}
