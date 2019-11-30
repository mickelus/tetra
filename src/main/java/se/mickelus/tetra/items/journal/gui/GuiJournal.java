package se.mickelus.tetra.items.journal.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.config.GuiUtils;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.tetra.items.journal.GuiJournalRootBase;
import se.mickelus.tetra.items.journal.JournalPage;
import se.mickelus.tetra.items.journal.gui.blueprint.GuiJournalBlueprintRoot;
import se.mickelus.tetra.items.journal.gui.craft.GuiJournalCraftRoot;
import se.mickelus.tetra.items.journal.gui.system.GuiJournalSystemRoot;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class GuiJournal extends Screen {

    private final GuiJournalHeader header;

    private final GuiJournalRootBase[] pages;
    private GuiJournalRootBase currentPage;

    private GuiElement defaultGui;

    private static GuiJournal instance;

    public GuiJournal() {
        super(new StringTextComponent("tetra:holosphere"));

        width = 320;
        height = 240;

        // fontRenderer = Minecraft.getInstance().fontRenderer;
        defaultGui = new GuiElement(0, 0, width, height);

        header = new GuiJournalHeader(0, 0, width, this::changePage);
        defaultGui.addChild(header);

        pages = new GuiJournalRootBase[JournalPage.values().length];
        pages[0] = new GuiJournalCraftRoot(0, 18);
        defaultGui.addChild(pages[0]);
        pages[1] = new GuiJournalBlueprintRoot(0, 18);
        pages[1].setVisible(false);
        defaultGui.addChild(pages[1]);
        pages[2] = new GuiJournalSystemRoot(0, 18);
        pages[2].setVisible(false);
        defaultGui.addChild(pages[2]);

        currentPage = pages[0];
    }

    public static GuiJournal getInstance() {
        if (instance == null || ConfigHandler.development) {
            instance = new GuiJournal();
        }

        return instance;
    }

    public void onShow() {
        header.onShow();
        currentPage.animateOpen();
    }

    private void changePage(JournalPage page) {
        header.changePage(page);

        for (int i = 0; i < pages.length; i++) {
            pages[i].setVisible(page.ordinal() == i);
        }

        currentPage = pages[page.ordinal()];
    }

    @Override
    public void render(final int mouseX, final int mouseY, final float partialTicks) {
        renderBackground();
        super.render(mouseX, mouseY, partialTicks);

        defaultGui.draw((width - defaultGui.getWidth()) / 2, (height - defaultGui.getHeight()) / 2,
                width, height, mouseX, mouseY, 1);

        renderHoveredToolTip(mouseX, mouseY);
    }

    protected void renderHoveredToolTip(int mouseX, int mouseY) {
        List<String> tooltipLines = defaultGui.getTooltipLines();
        if (tooltipLines != null) {
            tooltipLines = tooltipLines.stream()
                    .map(line -> line.replace("\\n", "\n"))
                    .flatMap(line -> Arrays.stream(line.split("\n")))
                    .collect(Collectors.toList());

            GuiUtils.drawHoveringText(tooltipLines, mouseX, mouseY, width, height, 300, font);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        defaultGui.onClick((int) mouseX, (int) mouseY);

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean charTyped(char typecChar, int keyCode) {
        currentPage.charTyped(typecChar);
        return false;
    }
}
