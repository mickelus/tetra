package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.GuiString;
import se.mickelus.mutil.gui.GuiStringOutline;
import se.mickelus.mutil.gui.GuiTexture;
import se.mickelus.tetra.gui.GuiTextures;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;

@ParametersAreNonnullByDefault
public class GuiExperience extends GuiElement {
    private static final int positiveColor = 0xc8ff8f;
    private static final int negativeColor = 0x8c605d;

    private final GuiTexture indicator;
    private final GuiString levelString;

    private final String unlocalizedTooltip;
    private List<Component> formattedTooltip;

    public GuiExperience(int x, int y) {
        this(x, y, null);
    }

    public GuiExperience(int x, int y, String unlocalizedTooltip) {
        super(x, y, 16, 16);

        indicator = new GuiTexture(0, 0, 16, 16, 0, 0, GuiTextures.workbench);
        addChild(indicator);

        levelString = new GuiStringOutline(8, 2, "");
        addChild(levelString);

        this.unlocalizedTooltip = unlocalizedTooltip;
    }

    public void update(int level, boolean positive) {
        indicator.setTextureCoordinates(Math.min(level, 3) * 16 + 112, positive ? 0 : 16);

        levelString.setString(level + "");
        levelString.setColor(positive ? positiveColor : negativeColor);

        if (unlocalizedTooltip != null) {
            formattedTooltip = Collections.singletonList(new TranslatableComponent(unlocalizedTooltip, level));
        }
    }

    @Override
    public List<Component> getTooltipLines() {
        if (formattedTooltip != null && hasFocus()) {
            return formattedTooltip;
        }

        return super.getTooltipLines();
    }
}
