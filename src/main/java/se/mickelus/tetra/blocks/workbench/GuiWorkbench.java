package se.mickelus.tetra.blocks.workbench;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import se.mickelus.tetra.gui.*;

import java.io.IOException;

@SideOnly(Side.CLIENT)
public class GuiWorkbench extends GuiContainer implements Runnable {

    private static GuiWorkbench instance;

    private static final String WORKBENCH_TEXTURE = "textures/gui/workbench.png";
    private static final String INVENTORY_TEXTURE = "textures/gui/player-inventory.png";
    private final TileEntityWorkbench tileEntity;


    private GuiElement defaultGui;

    private GuiModuleList componentList;

    private ItemStack targetStack = ItemStack.EMPTY;

    public GuiWorkbench(ContainerWorkbench container, TileEntityWorkbench tileEntity) {
        super(container);
        this.allowUserInput = false;
        this.xSize = 179;
        this.ySize = 176;

        this.tileEntity = tileEntity;

        defaultGui = new GuiElement(0, 0, xSize, ySize);
        defaultGui.addChild(new GuiTextureOffset(61, 0, 51, 51, WORKBENCH_TEXTURE));
        defaultGui.addChild(new GuiTexture(0, 74, 179, 106, INVENTORY_TEXTURE));

        componentList = new GuiModuleList(0, 0);

        tileEntity.registerChangeListener(this);
    }

	@Override
	public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
		this.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.func_191948_b(mouseX, mouseY);
	}

	@Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;

        defaultGui.draw(x, y, width, height, mouseX, mouseY);
        componentList.draw(width / 2, y, width, height, mouseX, mouseY);

        fontRendererObj.drawString("" + tileEntity.getCurrentState(), x, y, 0xffffffff, false);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        tileEntity.setCurrentState(tileEntity.getCurrentState() + 1);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
//        inventorySlots.removeListener(this);
    }

    /**
     * Called when the tileentity changes
     */
    @Override
    public void run() {
        onStackChange();
    }

    private void onStackChange() {
        ItemStack stack = tileEntity.getTargetItemStack();

        if (!targetStack.isItemEqual(stack)) {
            componentList.update(stack);
            targetStack = stack;
        }
    }
}
