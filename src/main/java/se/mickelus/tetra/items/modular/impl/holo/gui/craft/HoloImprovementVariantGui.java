package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import com.google.common.collect.ImmutableList;
import se.mickelus.mgui.gui.*;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.module.schematic.OutcomePreview;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Consumer;
@ParametersAreNonnullByDefault
public class HoloImprovementVariantGui extends GuiClickable {

    private final GuiTexture backdrop;
    private final GuiString label;

    private List<String> tooltip;
    private boolean isMuted;

    private final OutcomePreview preview;
    private final Consumer<OutcomePreview> onVariantHover;
    private final Consumer<OutcomePreview> onVariantBlur;

    public HoloImprovementVariantGui(int x, int y, String name, int labelStart, OutcomePreview preview, boolean isConnected,
            Consumer<OutcomePreview> onVariantHover, Consumer<OutcomePreview> onVariantBlur, Consumer<OutcomePreview> onVariantSelect) {
        super(x, y, 19, 11, () -> onVariantSelect.accept(preview));

        this.preview = preview;
        this.onVariantHover = onVariantHover;
        this.onVariantBlur = onVariantBlur;

        String truncatedName = name;

        if (truncatedName.length() > labelStart) {
            truncatedName = truncatedName.substring(labelStart);
        }

        if (truncatedName.length() > 4) {
            truncatedName = truncatedName.substring(0, 4);
        }

        truncatedName = truncatedName.trim().toLowerCase();

        if (isConnected) {
            addChild(new GuiTexture(-2, 0, 11, 11, 193, 31, GuiTextures.workbench).setAttachmentAnchor(GuiAttachment.topRight));
        }

        backdrop = new GuiTexture(0, 0, 17, 11, 176, 31, GuiTextures.workbench);
        addChild(backdrop);

        label = new GuiStringOutline(9, 1, truncatedName);
        label.setAttachmentPoint(GuiAttachment.topCenter);
        addChild(label);

        tooltip = ImmutableList.of(name);
    }

    @Override
    protected void onFocus() {
        super.onFocus();
        onVariantHover.accept(preview);

        backdrop.setColor(GuiColors.hover);
        label.setColor(GuiColors.hover);
    }

    @Override
    protected void onBlur() {
        super.onBlur();

        onVariantBlur.accept(preview);

        backdrop.setColor(isMuted ? GuiColors.muted : GuiColors.normal);
        label.setColor(isMuted ? GuiColors.muted : GuiColors.normal);
    }

    @Override
    public List<String> getTooltipLines() {
        return hasFocus() ? tooltip : null;
    }

    public void setMuted(boolean muted) {
        isMuted = muted;

        backdrop.setColor(muted ? GuiColors.muted : GuiColors.normal);
        label.setColor(muted ? GuiColors.muted : GuiColors.normal);
    }
}
