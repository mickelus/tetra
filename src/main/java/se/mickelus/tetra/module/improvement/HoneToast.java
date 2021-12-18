package se.mickelus.tetra.module.improvement;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.module.schematic.SchematicRarity;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
public class HoneToast implements Toast {
    private static final ResourceLocation texture = new ResourceLocation(TetraMod.MOD_ID,"textures/gui/toasts.png");

    private boolean hasPlayedSound = false;
    private ItemStack itemStack;

    public HoneToast(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    public Visibility render(PoseStack matrixStack, ToastComponent toastGui, long delta) {
        if (itemStack != null) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, texture);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            toastGui.blit(matrixStack, 0, 0, 0, 0, 160, 32);

            String itemName = toastGui.getMinecraft().font.plainSubstrByWidth(itemStack.getHoverName().getString(), 125);
            toastGui.getMinecraft().font.draw(matrixStack, I18n.get("tetra.hone.available"), 30, 7, SchematicRarity.hone.tint);
            toastGui.getMinecraft().font.draw(matrixStack, itemName, 30, 18, GuiColors.muted);

            if (!this.hasPlayedSound && delta > 0L) {
                this.hasPlayedSound = true;
                toastGui.getMinecraft().getSoundManager()
                        .play(SimpleSoundInstance.forUI(SoundEvents.PLAYER_LEVELUP, 0.6F, 0.7F));
            }

            // Lighting.turnBackOn();
            toastGui.getMinecraft().getItemRenderer().renderAndDecorateItem(itemStack, 8, 8);

            return delta > 5000 ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
        }

        return Toast.Visibility.HIDE;
    }
}
