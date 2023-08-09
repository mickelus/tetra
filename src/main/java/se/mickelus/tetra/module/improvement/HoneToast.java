package se.mickelus.tetra.module.improvement;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.TetraSounds;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.module.schematic.SchematicRarity;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class HoneToast implements Toast {
    private static final ResourceLocation texture = new ResourceLocation(TetraMod.MOD_ID, "textures/gui/toasts.png");
    private final ItemStack itemStack;
    private boolean hasPlayedSound = false;

    public HoneToast(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    public Visibility render(GuiGraphics graphics, ToastComponent toastGui, long delta) {
        if (itemStack != null) {
            graphics.blit(texture, 0, 0, 0, 0, 160, 32);

            String itemName = toastGui.getMinecraft().font.plainSubstrByWidth(itemStack.getHoverName().getString(), 125);
            graphics.drawString(toastGui.getMinecraft().font, I18n.get("tetra.hone.available"), 30, 7, SchematicRarity.hone.tint);
            graphics.drawString(toastGui.getMinecraft().font, itemName, 30, 18, GuiColors.muted);

            graphics.renderItem(itemStack, 8, 8);
            graphics.renderItemDecorations(toastGui.getMinecraft().font, itemStack, 8, 8);

            if (!this.hasPlayedSound && delta > 0L) {
                toastGui.getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(TetraSounds.honeGain, 1, 1));
                this.hasPlayedSound = true;
            }

            return delta > 5000 ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
        }

        return Toast.Visibility.HIDE;
    }
}
