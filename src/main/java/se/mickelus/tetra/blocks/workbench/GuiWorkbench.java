package se.mickelus.tetra.blocks.workbench;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import se.mickelus.tetra.gui.*;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.UpgradeSchema;

import java.io.IOException;

@SideOnly(Side.CLIENT)
public class GuiWorkbench extends GuiContainer {

    private static GuiWorkbench instance;

    private static final String WORKBENCH_TEXTURE = "textures/gui/workbench.png";
    private static final String INVENTORY_TEXTURE = "textures/gui/player-inventory.png";

    private EntityPlayer viewingPlayer;

    private final TileEntityWorkbench tileEntity;

    private GuiElement defaultGui;

    private GuiModuleList componentList;
    private GuiSchemaList schemaList;
    private GuiSchemaDetail schemaDetail;

    private ItemStack targetStack = ItemStack.EMPTY;

    public GuiWorkbench(ContainerWorkbench container, TileEntityWorkbench tileEntity, EntityPlayer viewingPlayer) {
        super(container);
        this.allowUserInput = false;
        this.xSize = 320;
        this.ySize = 240;

        this.tileEntity = tileEntity;

        this.viewingPlayer = viewingPlayer;

        defaultGui = new GuiElement(0, 0, xSize, ySize);
        defaultGui.addChild(new GuiTextureOffset(134, 40, 51, 51, WORKBENCH_TEXTURE));
        defaultGui.addChild(new GuiTexture(72, 153, 179, 106, INVENTORY_TEXTURE));


        componentList = new GuiModuleList(164, 49);
        defaultGui.addChild(componentList);

        schemaList = new GuiSchemaList(46, 100);
        schemaList.registerSelectHandler(tileEntity::setCurrentSchema);
        defaultGui.addChild(schemaList);

        schemaDetail = new GuiSchemaDetail(46, 100, this::deselectSchema, this::craftUpgrade);
        defaultGui.addChild(schemaDetail);

        tileEntity.addChangeListener("gui.workbench", this::onTileEntityChange);

        instance = this;
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

//        drawRect(x, y, x + xSize, y + ySize, 0x44ffffff);
        defaultGui.draw(x, y, width, height, mouseX, mouseY);
//        componentList.draw(width / 2, y, width, height, mouseX, mouseY);

//        fontRendererObj.drawString("" + tileEntity.getCurrentState(), x, y, 0xffffffff, false);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        defaultGui.onClick(mouseX, mouseY);

//        tileEntity.setCurrentState(tileEntity.getCurrentState() + 1);
    }

    private void deselectSchema() {
        tileEntity.setCurrentSchema(null);
    }

    private void craftUpgrade() {
        tileEntity.initiateCrafting();
    }

    private void onTileEntityChange() {
        ItemStack stack = tileEntity.getTargetItemStack();
        UpgradeSchema schema = tileEntity.getCurrentSchema();

        if (!ItemStack.areItemStackTagsEqual(targetStack, stack)) {
            componentList.update(stack);
            targetStack = stack;
        }

        if (schema == null) {
            schemaList.setSchemas(ItemUpgradeRegistry.instance.getAvailableSchemas(viewingPlayer, tileEntity.getTargetItemStack()));

            schemaList.setVisible(true);
            schemaDetail.setVisible(false);
        } else {

            schemaDetail.setSchema(schema);
            schemaDetail.toggleButton(schema.canApplyUpgrade(stack, tileEntity.getMaterials()));

            schemaList.setVisible(false);
            schemaDetail.setVisible(true);
        }
    }
}
