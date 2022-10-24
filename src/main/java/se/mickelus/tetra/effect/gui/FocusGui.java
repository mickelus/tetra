package se.mickelus.tetra.effect.gui;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.mutil.gui.GuiAttachment;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.GuiTexture;
import se.mickelus.mutil.util.CastOptional;
import se.mickelus.tetra.effect.FocusEffect;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.gui.InvertColorGui;
import se.mickelus.tetra.items.modular.ModularItem;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class FocusGui extends GuiElement {
    private final GuiElement container;
    private final GuiTexture left;
    private final GuiTexture right;

    public FocusGui() {
        super(-1, 1, 0, 0);

        setAttachment(GuiAttachment.middleCenter);

        container = new InvertColorGui(0, 0, width, height)
                .setOpacity(0);
        addChild(container);

        left = new GuiTexture(-1, 0, 2, 2, 0, 1, GuiTextures.hud);
        left.setUseDefaultBlending(false);
        left.setAttachment(GuiAttachment.topRight);
        container.addChild(left);
        right = new GuiTexture(2, 0, 2, 2, 1, 1, GuiTextures.hud);
        right.setUseDefaultBlending(false);
        container.addChild(right);
    }

    public void setSpread(float progress, float cap) {
        if (progress != -1) {
            int offset = (int) Mth.lerp(progress, Math.min(6, 3 + (cap / 3)), 0f);
            left.setX(-1 - offset);
            left.setY(offset);
            right.setX(2 + offset);
            right.setY(offset);

            setVisible(true);
        } else {
            setVisible(false);
        }
    }

    public void update(Player player) {
        if (player.isCrouching() && FocusEffect.hasApplicableItem(player)) {
            float spread = getSpread(player.getMainHandItem());
            float spreadReduction;
            if (spread <= -1) {
                spread = getSpread(player.getOffhandItem());
                spreadReduction = FocusEffect.getSpreadReduction(player, player.getOffhandItem());
            } else {
                spreadReduction = FocusEffect.getSpreadReduction(player, player.getMainHandItem());
            }

            if (spread > 0) {
                setSpread(Mth.clamp(spreadReduction / (100 - spread), 0, 1), Math.max(0, 100 - spread));
                return;
            }
        }
        setSpread(-1, 0);
    }

    private float getSpread(ItemStack itemStack) {
        return CastOptional.cast(itemStack.getItem(), ModularItem.class)
                .map(item -> item.getEffectEfficiency(itemStack, ItemEffect.spread))
                .orElse(-1f);
    }
}
