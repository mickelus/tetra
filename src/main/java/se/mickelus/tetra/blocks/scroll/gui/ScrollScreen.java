package se.mickelus.tetra.blocks.scroll.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import se.mickelus.mutil.gui.GuiAttachment;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.GuiText;
import se.mickelus.mutil.gui.GuiTexture;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
@OnlyIn(Dist.CLIENT)
public class ScrollScreen extends Screen {
    private static int currentPage;
    private final String[] pages;

    private final GuiElement gui;
    private final GuiText text;

    public ScrollScreen(String key) {
        super(new TextComponent("tetra:scroll"));

        pages = I18n.get("item.tetra.scroll." + key + ".details").split("\r");

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
        currentPage = Mth.clamp(index, 0, pages.length - 1);
        text.setString(pages[currentPage]);
    }

    @Override
    public void render(PoseStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        renderBackground(matrixStack, 0);
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        gui.draw(matrixStack, (width - gui.getWidth()) / 2, (height - gui.getHeight()) / 2,
                width, height, mouseX, mouseY, 1);

        renderHoveredToolTip(matrixStack, mouseX, mouseY);
    }

    protected void renderHoveredToolTip(PoseStack matrixStack, int mouseX, int mouseY) {
        List<String> tooltipLines = gui.getTooltipLines();
        if (tooltipLines != null) {
            List<Component> textComponents = tooltipLines.stream()
                    .map(line -> line.replace("\\n", "\n"))
                    .flatMap(line -> Arrays.stream(line.split("\n")))
                    .map(TextComponent::new)
                    .collect(Collectors.toList());

            renderTooltip(matrixStack, textComponents, Optional.empty(), mouseX, mouseY);
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
