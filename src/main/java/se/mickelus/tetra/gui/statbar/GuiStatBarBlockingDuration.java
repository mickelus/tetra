package se.mickelus.tetra.gui.statbar;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import se.mickelus.mgui.gui.GuiAlignment;
import se.mickelus.mgui.gui.GuiTexture;
import se.mickelus.tetra.blocks.workbench.gui.GuiCapability;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.gui.statbar.getter.*;
import se.mickelus.tetra.items.modular.ItemModular;
import se.mickelus.tetra.module.ItemEffect;
import se.mickelus.tetra.util.CastOptional;

public class GuiStatBarBlockingDuration extends GuiStatBar {
    private static final int efficiencyMax = 50;

    private static final IStatGetter durationGetter = new StatGetterEffectLevel(ItemEffect.blocking, 1);
    private static final IStatGetter cooldownGetter = new StatGetterEffectEfficiency(ItemEffect.blocking, 1);

    public GuiStatBarBlockingDuration(int x, int y, int width) {
        super(x, y, width, "", 0, efficiencyMax,
                false, durationGetter, LabelGetterBasic.integerLabel,
                new TooltipGetterBlockingDuration(durationGetter, cooldownGetter));
    }

    @Override
    public void update(PlayerEntity player, ItemStack currentStack, ItemStack previewStack, String slot, String improvement) {
        super.update(player, currentStack, previewStack, slot, improvement);

        if (cooldownGetter.getValue(player, currentStack) == 0 && cooldownGetter.getValue(player, previewStack) == 0) {
            labelString.setString(I18n.format("tetra.stats.blocking"));
            valueString.setString("");
            bar.setValue(max, max);
        }
    }
}
