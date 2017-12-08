package se.mickelus.tetra.items.toolbelt;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import se.mickelus.tetra.TetraMod;

import java.io.IOException;

@SideOnly(Side.CLIENT)
public class GuiToolbelt extends GuiContainer {

    private static GuiToolbelt instance;

    private static final ResourceLocation TOOLBELT_TEXTURE = new ResourceLocation(TetraMod.MOD_ID, "textures/gui/toolbelt-inventory.png");
    private static final ResourceLocation INVENTORY_TEXTURE = new ResourceLocation(TetraMod.MOD_ID, "textures/gui/player-inventory.png");


    public GuiToolbelt(ContainerToolbelt container) {
        super(container);
        this.allowUserInput = false;
        this.xSize = 179;
        this.ySize = 176;

        instance = this;
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
        drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;

        mc.getTextureManager().bindTexture(TOOLBELT_TEXTURE);
        GlStateManager.pushMatrix();
        GlStateManager.translate(-0.5F, 0.5F, 0);
        this.drawTexturedModalRect(x + 13, y + 3, 0, 0, 143, 75);
        GlStateManager.popMatrix();


        mc.getTextureManager().bindTexture(INVENTORY_TEXTURE);
        this.drawTexturedModalRect(x, y + 74, 0, 0, 179, 106);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        // todo: clear shadow slot if rightclick and "normal" slot is empty
    }
}
