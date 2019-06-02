package se.mickelus.tetra.items.journal.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import se.mickelus.tetra.gui.GuiElement;
import se.mickelus.tetra.items.journal.GuiJournalRootBase;
import se.mickelus.tetra.items.journal.JournalPage;
import se.mickelus.tetra.items.journal.gui.blueprint.GuiJournalBlueprintRoot;
import se.mickelus.tetra.items.journal.gui.craft.GuiJournalCraftRoot;
import se.mickelus.tetra.items.journal.gui.system.GuiJournalSystemRoot;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@SideOnly(Side.CLIENT)
public class GuiJournal extends GuiScreen {
    private static final String WORKBENCH_TEXTURE = "textures/gui/workbench.png";
    private static final String INVENTORY_TEXTURE = "textures/gui/player-inventory.png";

    private final GuiJournalHeader header;

    private final GuiJournalRootBase[] pages;
    private GuiJournalRootBase currentPage;

    private GuiElement defaultGui;

    public GuiJournal() {
        super();

        this.allowUserInput = false;
        width = 320;
        height = 240;

        fontRenderer = Minecraft.getMinecraft().fontRenderer;
        defaultGui = new GuiElement(0, 0, width, height);

        header = new GuiJournalHeader(0, 0, width, this::changePage);
        defaultGui.addChild(header);
        header.onShow();

        pages = new GuiJournalRootBase[JournalPage.values().length];
        pages[0] = new GuiJournalCraftRoot(0, 18);
        pages[0].animateOpen();
        defaultGui.addChild(pages[0]);
        pages[1] = new GuiJournalBlueprintRoot(0, 18);
        pages[1].setVisible(false);
        defaultGui.addChild(pages[1]);
        pages[2] = new GuiJournalSystemRoot(0, 18);
        pages[2].setVisible(false);
        defaultGui.addChild(pages[2]);

        currentPage = pages[0];
    }

    private void changePage(JournalPage page) {
        header.changePage(page);

        for (int i = 0; i < pages.length; i++) {
            pages[i].setVisible(page.ordinal() == i);
        }

        currentPage = pages[page.ordinal()];
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        drawDefaultBackground();

        defaultGui.draw((width - defaultGui.getWidth()) / 2, (height - defaultGui.getHeight()) / 2,
                width, height, mouseX, mouseY, 1);

        renderHoveredToolTip(mouseX, mouseY);
    }

    protected void renderHoveredToolTip(int mouseX, int mouseY) {
        List<String> tooltipLines = defaultGui.getTooltipLines();
        if (tooltipLines != null) {
            tooltipLines = tooltipLines.stream()
                    .map(line -> line.replace("\\n", "\n"))
                    .collect(Collectors.toList());

            GuiUtils.drawHoveringText(tooltipLines, mouseX, mouseY, width, height, -1, fontRenderer);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        defaultGui.onClick(mouseX, mouseY);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);

        currentPage.onKeyTyped(typedChar);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

    }
}
