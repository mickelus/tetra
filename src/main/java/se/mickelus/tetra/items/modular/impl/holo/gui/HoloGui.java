package se.mickelus.tetra.items.modular.impl.holo.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.gui.GuiUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.gui.GuiSpinner;
import se.mickelus.tetra.items.modular.impl.holo.HoloPage;
import se.mickelus.tetra.items.modular.impl.holo.gui.craft.HoloCraftRootGui;
import se.mickelus.tetra.items.modular.impl.holo.gui.scan.HoloScanRootGui;
import se.mickelus.tetra.items.modular.impl.holo.gui.system.HoloSystemRootGui;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    public HoloGui() {
        super(new StringTextComponent("tetra:holosphere"));

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
                Minecraft.getInstance().runImmediately(HoloGui::onReload);
            });
            hasListener = true;
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
    public void render(MatrixStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        renderBackground(matrixStack, 0);
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        defaultGui.draw(matrixStack, (width - defaultGui.getWidth()) / 2, (height - defaultGui.getHeight()) / 2,
                width, height, mouseX, mouseY, 1);

        renderHoveredToolTip(matrixStack, mouseX, mouseY);
    }

    protected void renderHoveredToolTip(MatrixStack matrixStack, int mouseX, int mouseY) {
        List<String> tooltipLines = defaultGui.getTooltipLines();
        if (tooltipLines != null) {
            List<ITextComponent> textComponents = tooltipLines.stream()
                    .map(line -> line.replace("\\n", "\n"))
                    .flatMap(line -> Arrays.stream(line.split("\n")))
                    .map(StringTextComponent::new)
                    .collect(Collectors.toList());

            GuiUtils.drawHoveringText(matrixStack, textComponents, mouseX, mouseY, width, height, 300, font);
        }
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        defaultGui.onMouseClick((int) x, (int) y, button);

        return super.mouseClicked(x, y, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double distance) {
        return defaultGui.onMouseScroll(mouseX, mouseY, distance);
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        if (ConfigHandler.development.get()) {
            switch (typedChar) {
                case 'r':
                    instance = null;
                    Minecraft.getInstance().displayGuiScreen(null);

                    HoloGui gui = HoloGui.getInstance();
                    Minecraft.getInstance().displayGuiScreen(gui);
                    gui.onShow();
                    break;
                case 't':
                    getMinecraft().player.sendChatMessage("/reload");
                    spinner.setVisible(true);
                    break;
            }
        }

        currentPage.charTyped(typedChar);
        return false;
    }


    private static void onReload() {
        if (instance != null && instance.getMinecraft().currentScreen == instance) {
            logger.info("Refreshing holosphere gui data");
            instance.spinner.setVisible(false);
            if (instance.currentPage != null) {
                instance.currentPage.onReload();
            }
        }
    }
}
