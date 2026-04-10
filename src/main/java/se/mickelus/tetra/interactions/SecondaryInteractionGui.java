package se.mickelus.tetra.interactions;

import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyModifier;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.animation.Applier;
import se.mickelus.mutil.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.client.keymap.TetraKeyMappings;
import se.mickelus.tetra.gui.GuiKeybinding;

import java.util.Locale;

public class SecondaryInteractionGui extends GuiElement {
    KeyframeAnimation showAnimation;
    KeyframeAnimation hideanimation;

    public SecondaryInteractionGui(int x, int y, SecondaryInteraction interaction) {
        super(x, y, 100, 11);

        KeyMapping keybind = TetraKeyMappings.secondaryUseBinding;
        addChild(new GuiKeybinding(0, 0, keybind.getKey().getDisplayName().getString().toUpperCase(Locale.ROOT),
                keybind.getKeyModifier() != KeyModifier.NONE ? keybind.getKeyModifier().toString() : null,
                interaction.getLabel()));

        opacity = 0;
        this.showAnimation = new KeyframeAnimation(240, this)
                .applyTo(new Applier.Opacity(0.9f))
                .withDelay(100);
        this.hideanimation = new KeyframeAnimation(100, this)
                .applyTo(new Applier.Opacity(0))
                .onStop(complete -> this.remove());
    }

    public void show() {
        showAnimation.start();
    }

    public void hide() {
        showAnimation.stop();
        hideanimation.start();
    }
}
