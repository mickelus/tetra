package se.mickelus.tetra.module.improvement;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import se.mickelus.mutil.util.CastOptional;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.TetraSounds;
import se.mickelus.tetra.blocks.workbench.gui.GuiModuleGlyph;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.schematic.SchematicRarity;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
public class SettleToast implements Toast {
    private static final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(TetraMod.MOD_ID, "textures/gui/toasts.png");
    private final ItemStack itemStack;
    private final String moduleName;
    private final GuiModuleGlyph glyph;
    private boolean hasPlayedSound = false;

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
    public Visibility render(GuiGraphics graphics, ToastComponent toastGui, long delta) {
        if (itemStack != null) {
            graphics.blit(texture, 0, 0, 0, 0, 160, 32);

            if (glyph != null) {
                graphics.blit(texture, 20, 14, 160, 0, 15, 15);
                glyph.draw(graphics, 19, 14, 260, 43, -1, -1, 1);
            }

            graphics.drawString(toastGui.getMinecraft().font, I18n.get(TetraMod.MOD_ID + ".settled.toast"), 30, 7, SchematicRarity.hone.tint);
            graphics.drawString(toastGui.getMinecraft().font, toastGui.getMinecraft().font.plainSubstrByWidth(moduleName, 118), 37, 18, GuiColors.muted);

            graphics.renderItem(itemStack, 8, 8);
            graphics.renderItemDecorations(toastGui.getMinecraft().font, itemStack, 8, 8);

            if (!this.hasPlayedSound && delta > 0L) {
                toastGui.getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(TetraSounds.settle, 1, 1));
                this.hasPlayedSound = true;
            }

            return delta > 5000 ? Visibility.HIDE : Visibility.SHOW;
        }

        return Visibility.HIDE;
    }
}
