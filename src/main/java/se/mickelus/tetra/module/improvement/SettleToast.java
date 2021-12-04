package se.mickelus.tetra.module.improvement;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.workbench.gui.GuiModuleGlyph;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.schematic.SchematicRarity;
import se.mickelus.tetra.util.CastOptional;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
@ParametersAreNonnullByDefault
public class SettleToast implements Toast {
    private static final ResourceLocation texture = new ResourceLocation(TetraMod.MOD_ID,"textures/gui/toasts.png");

    private boolean hasPlayedSound = false;
    private ItemStack itemStack;
    private String moduleName;
    private GuiModuleGlyph glyph;

    public SettleToast(ItemStack itemStack, String slot) {
        this.itemStack = itemStack;

        ItemModule itemModule = CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .map(item -> item.getModuleFromSlot(itemStack, slot))
                .orElse(null);

        glyph = Optional.ofNullable(itemModule)
                .map(module -> module.getVariantData(itemStack))
                .map(data -> data.glyph)
                .map(glyphData -> new GuiModuleGlyph(0, 0, 16, 16, glyphData).setShift(false))
                .orElse(null);

        moduleName = Optional.ofNullable(itemModule)
                .map(module -> module.getName(itemStack))
                .orElse(slot);
    }

    @Override
    public Visibility render(PoseStack matrixStack, ToastComponent toastGui, long delta) {


        if (itemStack != null) {
            toastGui.getMinecraft().getTextureManager().bindForSetup(texture);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            toastGui.blit(matrixStack, 0, 0, 0, 0, 160, 32);

            if (!this.hasPlayedSound && delta > 0L) {
                this.hasPlayedSound = true;

                toastGui.getMinecraft().getSoundManager()
                        .play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 2F, 0.7F));
            }

            if (glyph != null) {
                toastGui.blit(matrixStack, 20, 14, 160, 0, 15, 15);
                glyph.draw(new PoseStack(), 19, 14, 260, 43, -1, -1, 1);
            }

            toastGui.getMinecraft().font.draw(matrixStack, I18n.get(TetraMod.MOD_ID + ".settled.toast"), 30, 7, SchematicRarity.hone.tint);
            toastGui.getMinecraft().font.draw(matrixStack, toastGui.getMinecraft().font.plainSubstrByWidth(moduleName, 118), 37, 18, GuiColors.muted);


            // todo 1.18: still lit correctly?
//            Lighting.turnBackOn();
            toastGui.getMinecraft().getItemRenderer().renderAndDecorateItem(itemStack, 8, 8);

            return delta > 5000 ? Visibility.HIDE : Visibility.SHOW;
        }

        return Visibility.HIDE;
    }
}
