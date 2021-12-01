package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.client.resources.language.I18n;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiString;
import se.mickelus.mgui.gui.GuiStringOutline;
import se.mickelus.mgui.gui.GuiTexture;
import se.mickelus.tetra.gui.GuiTextures;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
@ParametersAreNonnullByDefault
public class GuiExperience extends GuiElement {
    private static final int positiveColor = 0xc8ff8f;
    private static final int negativeColor = 0x8c605d;

    private GuiTexture indicator;
    private GuiString levelString;

    private String unlocalizedTooltip;
    private List<String> formattedTooltip;

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
            formattedTooltip = Collections.singletonList(I18n.get(unlocalizedTooltip, level));
        }
    }

    @Override
    public List<String> getTooltipLines() {
        if (formattedTooltip != null && hasFocus()) {
            return formattedTooltip;
        }

        return super.getTooltipLines();
    }
}
