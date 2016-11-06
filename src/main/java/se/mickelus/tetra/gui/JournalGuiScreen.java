package se.mickelus.tetra.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import se.mickelus.tetra.TetraMod;

@SideOnly(Side.CLIENT)
public class JournalGuiScreen extends GuiScreen {

    private static JournalGuiScreen instance;
    private static final ResourceLocation vanillaBookTexture = new ResourceLocation("textures/gui/book.png");

    public static final String textureLocation = "textures/gui/book.png";
    private static final ResourceLocation texture = new ResourceLocation(TetraMod.MOD_ID, textureLocation);


    /** Update ticks since the gui was opened */
    private int updateCount;


    public static final int bookImageWidth = 192;
    public static final int bookImageHeight = 192;

    private EntityPlayer player;



    public JournalGuiScreen() {
        super();

        instance = this;
    }

    public void SetPlayer(EntityPlayer player) {
        this.player = player;
    }
    
    private void showView(int state) {

    }

    /**
     * Draws the screen and all the components in it.
     */
    @Override
    public void drawScreen(int par1, int par2, float par3) {
        GlStateManager.resetColor();
        mc.getTextureManager().bindTexture(vanillaBookTexture);

        // draw background
        drawTexturedModalRect(
                (width - bookImageWidth) / 2, // left
                (height - bookImageHeight) / 2, // top
                0, 0, JournalGuiScreen.bookImageWidth, JournalGuiScreen.bookImageHeight);

        super.drawScreen(par1, par2, par3);
    }

    public static JournalGuiScreen getInstance() {
        return instance;
    }

    /**
     * Called from the main game loop to update the screen.
     */
    @Override
    public void updateScreen() {
        super.updateScreen();
        ++this.updateCount;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

}
