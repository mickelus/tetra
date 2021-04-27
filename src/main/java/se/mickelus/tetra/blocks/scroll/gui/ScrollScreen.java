package se.mickelus.tetra.blocks.scroll.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.gui.GuiUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.mgui.gui.GuiAttachment;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiText;
import se.mickelus.mgui.gui.GuiTexture;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.gui.GuiSpinner;
import se.mickelus.tetra.items.modular.impl.holo.HoloPage;
import se.mickelus.tetra.items.modular.impl.holo.gui.HoloHeaderGui;
import se.mickelus.tetra.items.modular.impl.holo.gui.HoloRootBaseGui;
import se.mickelus.tetra.items.modular.impl.holo.gui.craft.HoloCraftRootGui;
import se.mickelus.tetra.items.modular.impl.holo.gui.scan.HoloScanRootGui;
import se.mickelus.tetra.items.modular.impl.holo.gui.system.HoloSystemRootGui;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class ScrollScreen extends Screen {
    private static int currentPage;
    private String[] pages;

    private GuiElement gui;
    private GuiText text;

    public ScrollScreen(String key) {
        super(new StringTextComponent("tetra:scroll"));

        pages = I18n.format("item.tetra.scroll." + key + ".details").split("\r");

        width = 320;
        height = 240;

        gui = new GuiElement(0, 0, width, height);
        gui.addChild(new GuiTexture(0, 0, 160, 186, new ResourceLocation(TetraMod.MOD_ID, "textures/gui/pamphlet.png")).setAttachment(GuiAttachment.middleCenter));

        text = new GuiText(2, -75, 124, "");
        text.setAttachmentAnchor(GuiAttachment.middleCenter);
        text.setAttachmentPoint(GuiAttachment.topCenter);
        text.setColor(0xffd738);

        gui.addChild(text);
        gui.addChild(new ScrollPageButtonGui(70, -10, true, () -> changePage(currentPage - 1)).setAttachment(GuiAttachment.bottomLeft));
        gui.addChild(new ScrollPageButtonGui(-70, -10, false, () -> changePage(currentPage + 1)).setAttachment(GuiAttachment.bottomRight));

        changePage(currentPage);
    }

    private void changePage(int index) {
        currentPage = MathHelper.clamp(index, 0, pages.length - 1);
        text.setString(pages[currentPage]);
    }

    @Override
    public void render(MatrixStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        renderBackground(matrixStack, 0);
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        gui.draw(matrixStack, (width - gui.getWidth()) / 2, (height - gui.getHeight()) / 2,
                width, height, mouseX, mouseY, 1);

        renderHoveredToolTip(matrixStack, mouseX, mouseY);
    }

    protected void renderHoveredToolTip(MatrixStack matrixStack, int mouseX, int mouseY) {
        List<String> tooltipLines = gui.getTooltipLines();
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
        gui.onMouseClick((int) x, (int) y, button);

        return super.mouseClicked(x, y, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double distance) {
        return gui.onMouseScroll(mouseX, mouseY, distance);
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        if (ConfigHandler.development.get()) {
            switch (typedChar) {
                case 'a':
                    changePage(currentPage - 1);
                    break;
                case 'd':
                    changePage(currentPage + 1);
                    break;
            }
        }

        return false;
    }
}
