package se.mickelus.tetra.items.modular.impl.holo.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.gui.GuiSpinner;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.impl.holo.HoloPage;
import se.mickelus.tetra.items.modular.impl.holo.gui.craft.HoloCraftRootGui;
import se.mickelus.tetra.items.modular.impl.holo.gui.scan.HoloScanRootGui;
import se.mickelus.tetra.items.modular.impl.holo.gui.system.HoloSystemRootGui;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
@OnlyIn(Dist.CLIENT)
public class HoloGui extends Screen {
    private static final Logger logger = LogManager.getLogger();

    private final HoloHeaderGui header;

    private final HoloRootBaseGui[] pages;
    private HoloRootBaseGui currentPage;

    private GuiElement defaultGui;

    private GuiElement spinner;

    private static HoloGui instance = null;

    private static boolean hasListener = false;

    private Runnable closeCallback;

    public HoloGui() {
        super(new TextComponent("tetra:holosphere"));

        width = 320;
        height = 240;

        // fontRenderer = Minecraft.getInstance().fontRenderer;
        defaultGui = new GuiElement(0, 0, width, height);

        header = new HoloHeaderGui(0, 0, width, this::changePage);
        defaultGui.addChild(header);

        pages = new HoloRootBaseGui[HoloPage.values().length];
        pages[0] = new HoloCraftRootGui(0, 18);
        defaultGui.addChild(pages[0]);
        pages[1] = new HoloScanRootGui(0, 18);
        pages[1].setVisible(false);
        defaultGui.addChild(pages[1]);
        pages[2] = new HoloSystemRootGui(0, 18);
        pages[2].setVisible(false);
        defaultGui.addChild(pages[2]);

        currentPage = pages[0];

        spinner = new GuiSpinner(-8, 6);
        spinner.setVisible(false);
        defaultGui.addChild(spinner);

        if (ConfigHandler.development.get() && !hasListener) {
            DataManager.featureData.onReload(() -> {
                Minecraft.getInstance().executeBlocking(HoloGui::onReload);
            });
            hasListener = true;
        }
    }

    public void openSchematic(IModularItem item, String slot, UpgradeSchematic schematic, Runnable closeCallback) {
        changePage(HoloPage.craft);

        ((HoloCraftRootGui) pages[0]).updateState(item, slot, schematic);
        this.closeCallback = closeCallback;
    }

    @Override
    public void removed() {
        super.removed();
        if (closeCallback != null) {
            // onClose is called in Minecarft.displayGuiScreen, pre-null-assignement prevents gui chaining from getting stuck in recursion
            Runnable callback = closeCallback;
            this.closeCallback = null;
            callback.run();
        }
    }

    public static HoloGui getInstance() {
        if (instance == null) {
            instance = new HoloGui();
        }

        return instance;
    }

    public void onShow() {
        header.onShow();
        currentPage.animateOpen();
    }

    private void changePage(HoloPage page) {
        header.changePage(page);

        for (int i = 0; i < pages.length; i++) {
            pages[i].setVisible(page.ordinal() == i);
        }

        currentPage = pages[page.ordinal()];
    }

    @Override
    public void render(PoseStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        renderBackground(matrixStack, 0);
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        defaultGui.draw(matrixStack, (width - defaultGui.getWidth()) / 2, (height - defaultGui.getHeight()) / 2,
                width, height, mouseX, mouseY, 1);

        renderHoveredToolTip(matrixStack, mouseX, mouseY);
    }

    protected void renderHoveredToolTip(PoseStack matrixStack, int mouseX, int mouseY) {
        List<String> tooltipLines = defaultGui.getTooltipLines();
        if (tooltipLines != null) {
            List<Component> textComponents = tooltipLines.stream()
                    .map(line -> line.replace("\\n", "\n"))
                    .flatMap(line -> Arrays.stream(line.split("\n")))
                    .map(TextComponent::new)
                    .collect(Collectors.toList());

            GuiUtils.drawHoveringText(matrixStack, textComponents, mouseX, mouseY, width, height, 280, font);
        }
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (defaultGui.onMouseClick((int) x, (int) y, button)) {
            return true;
        }

        return super.mouseClicked(x, y, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double distance) {
        if (currentPage.onMouseScroll(mouseX, mouseY, distance)) {
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, distance);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (currentPage.onKeyPress(keyCode, scanCode, modifiers)) {
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (currentPage.onKeyRelease(keyCode, scanCode, modifiers)) {
            return true;
        }

        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        if (currentPage.onCharType(typedChar, keyCode)) {
            return true;
        }

        if (ConfigHandler.development.get()) {
            switch (typedChar) {
                case 'r':
                    instance = null;
                    Minecraft.getInstance().setScreen(null);

                    HoloGui gui = HoloGui.getInstance();
                    Minecraft.getInstance().setScreen(gui);
                    gui.onShow();
                    break;
                case 't':
                    getMinecraft().player.chat("/reload");
                    spinner.setVisible(true);
                    break;
            }
        }

        return false;
    }

    private static void onReload() {
        if (instance != null && instance.getMinecraft().screen == instance) {
            logger.info("Refreshing holosphere gui data");
            instance.spinner.setVisible(false);
            if (instance.currentPage != null) {
                instance.currentPage.onReload();
            }
        }
    }
}
