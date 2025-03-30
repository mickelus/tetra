package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.GuiStringSmall;
import se.mickelus.mutil.gui.animation.Applier;
import se.mickelus.mutil.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.SchematicRegistry;
import se.mickelus.tetra.module.schematic.CraftingContext;
import se.mickelus.tetra.module.schematic.SchematicType;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class HoloSchematicListGui extends GuiElement {

    private final Consumer<UpgradeSchematic> onSchematicSelect;

    private final KeyframeAnimation openAnimation;

    private final KeyframeAnimation showAnimation;
    private final KeyframeAnimation hideAnimation;

    boolean displayAllSchematics = false;
    UpgradeSchematic[] availableSchematics;
    UpgradeSchematic[] allSchematics;

    private final GuiElement schematicContainer;
    private final HoloToggleVisibilityButtonGui toggleButton;

    public HoloSchematicListGui(int x, int y, int width, int height, Consumer<UpgradeSchematic> onSchematicSelect) {
        super(x, y, width, height);

        this.onSchematicSelect = onSchematicSelect;

        toggleButton = new HoloToggleVisibilityButtonGui(0, 2, this::toggleDisplay);
        addChild(toggleButton);

        addChild(new GuiStringSmall(0, 17, "Schematics", GuiColors.muted));

        schematicContainer = new GuiElement(0, 23, width, height);
        addChild(schematicContainer);

        openAnimation = new KeyframeAnimation(80, this)
                .applyTo(new Applier.Opacity(0, 1), new Applier.TranslateY(y - 5, y))
                .withDelay(120);

        showAnimation = new KeyframeAnimation(80, this)
                .applyTo(new Applier.Opacity(1), new Applier.TranslateY(y));

        hideAnimation = new KeyframeAnimation(60, this)
                .applyTo(new Applier.Opacity(0), new Applier.TranslateY(y - 5))
                .onStop(complete -> this.isVisible = false);
    }

    private void toggleDisplay() {
        displayAllSchematics = !displayAllSchematics;
        toggleButton.update(displayAllSchematics);
        updateSchematics();
    }

    public void update(IModularItem item, String slot) {
        Minecraft mc = Minecraft.getInstance();
        CraftingContext context = new CraftingContext(mc.player.level(), mc.player.getOnPos(), null, mc.player, new ItemStack(item.getItem()), slot, new ResourceLocation[0]);
        allSchematics = Arrays.stream(SchematicRegistry.getPreviewSchematics(context, true))
                .filter(schematic -> !schematic.isHoning())
                .filter(schematic -> schematic.getType() != SchematicType.improvement)
                .sorted(Comparator.comparing(UpgradeSchematic::getName))
                .toArray(UpgradeSchematic[]::new);
        availableSchematics = Arrays.stream(SchematicRegistry.getPreviewSchematics(context, false))
                .filter(schematic -> !schematic.isHoning())
                .filter(schematic -> schematic.getType() != SchematicType.improvement)
                .sorted(Comparator.comparing(UpgradeSchematic::getName))
                .toArray(UpgradeSchematic[]::new);

        updateSchematics();
    }

    private void updateSchematics() {
        int offset = 0;
        int pageLines = 8;

        schematicContainer.clearChildren();

        UpgradeSchematic[] schematics = displayAllSchematics ? allSchematics : availableSchematics;

        for (int i = 0; i < schematics.length; i++) {
            UpgradeSchematic schematic = schematics[i + offset];
            schematicContainer.addChild(new HoloSchematicListItemGui(
                    i / pageLines * 106,
                    i % pageLines * 14,
                    103,
                    schematic, () -> onSchematicSelect.accept(schematic)));
        }
    }

    public void animateOpen() {
        openAnimation.start();
    }

    @Override
    protected void onShow() {
        super.onShow();
        hideAnimation.stop();
        showAnimation.start();
    }

    @Override
    protected boolean onHide() {
        showAnimation.stop();
        hideAnimation.start();

        return false;
    }
}
