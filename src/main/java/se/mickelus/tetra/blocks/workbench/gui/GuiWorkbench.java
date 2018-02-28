package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import se.mickelus.tetra.blocks.geode.ItemGeode;
import se.mickelus.tetra.blocks.workbench.ContainerWorkbench;
import se.mickelus.tetra.blocks.workbench.TileEntityWorkbench;
import se.mickelus.tetra.blocks.workbench.action.BreakAction;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.gui.*;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.schema.UpgradeSchema;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiWorkbench extends GuiContainer {

    private static GuiWorkbench instance;

    private static final String WORKBENCH_TEXTURE = "textures/gui/workbench.png";
    private static final String INVENTORY_TEXTURE = "textures/gui/player-inventory.png";

    private EntityPlayer viewingPlayer;

    private final TileEntityWorkbench tileEntity;
    private final ContainerWorkbench container;

    private GuiElement defaultGui;
    private FontRenderer fontRenderer;

    private GuiModuleList componentList;
    private GuiStatGroup statGroup;
    private GuiIntegrityBar integrityBar;
    private GuiSchemaList schemaList;
    private GuiSchemaDetail schemaDetail;

    private GuiActionButton geodeActionButton;

    private ItemStack targetStack = ItemStack.EMPTY;

    public GuiWorkbench(ContainerWorkbench container, TileEntityWorkbench tileEntity, EntityPlayer viewingPlayer) {
        super(container);
        this.allowUserInput = false;
        this.xSize = 320;
        this.ySize = 240;

        this.tileEntity = tileEntity;
        this.container = container;

        this.viewingPlayer = viewingPlayer;

        fontRenderer = Minecraft.getMinecraft().fontRenderer;
        defaultGui = new GuiElement(0, 0, xSize, ySize);
        defaultGui.addChild(new GuiTextureOffset(134, 40, 51, 51, WORKBENCH_TEXTURE));
        defaultGui.addChild(new GuiTexture(72, 153, 179, 106, INVENTORY_TEXTURE));

        componentList = new GuiModuleList(164, 49);
        defaultGui.addChild(componentList);

        statGroup = new GuiStatGroup(60, 0);
        defaultGui.addChild(statGroup);

        integrityBar = new GuiIntegrityBar(160, 90);
        defaultGui.addChild(integrityBar);

        schemaList = new GuiSchemaList(46, 102);
        schemaList.registerSelectHandler(tileEntity::setCurrentSchema);
        schemaList.setVisible(false);
        defaultGui.addChild(schemaList);

        schemaDetail = new GuiSchemaDetail(46, 102, this::deselectSchema, this::craftUpgrade);
        schemaDetail.setVisible(false);
        defaultGui.addChild(schemaDetail);

        geodeActionButton = new GuiActionButton(140, 114, I18n.format("item.geode.label"), Capability.hammer, () -> {
            tileEntity.performAction(viewingPlayer, BreakAction.key);
        });
        geodeActionButton.setVisible(false);
        defaultGui.addChild(geodeActionButton);

        tileEntity.addChangeListener("gui.workbench", this::onTileEntityChange);

        instance = this;
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);

        drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;

        defaultGui.draw(x, y, width, height, mouseX, mouseY, 1);
    }

    @Override
    protected void renderHoveredToolTip(int mouseX, int mouseY) {
        super.renderHoveredToolTip(mouseX, mouseY);
        List<String> tooltipLines = defaultGui.getTooltipLines();
        if (tooltipLines !=null) {
            GuiUtils.drawHoveringText(tooltipLines, mouseX, mouseY, width, height, -1, fontRenderer);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        defaultGui.onClick(mouseX, mouseY);
    }

    private void deselectSchema() {
        tileEntity.setCurrentSchema(null);
    }

    private void craftUpgrade() {
        tileEntity.initiateCrafting(viewingPlayer);
    }

    private void onTileEntityChange() {
        ItemStack stack = tileEntity.getTargetItemStack();
        ItemStack previewStack = ItemStack.EMPTY;
        UpgradeSchema schema = tileEntity.getCurrentSchema();

        container.updateSlots();

        geodeActionButton.setVisible(false);
        if (schema == null) {
            UpgradeSchema[] schemas = ItemUpgradeRegistry.instance.getAvailableSchemas(viewingPlayer, stack);
            if (schemas.length > 0) {
                schemas = Arrays.stream(schemas)
                        .sorted(Comparator.comparing(UpgradeSchema::getType).thenComparing(UpgradeSchema::getKey))
                        .toArray(UpgradeSchema[]::new);
                schemaList.setSchemas(schemas);
                schemaList.setVisible(true);
            } else {
                schemaList.setVisible(false);

                if (tileEntity.canPerformAction(BreakAction.key)) {
                    geodeActionButton.update(viewingPlayer, 2);
                    geodeActionButton.setVisible(true);
                }
            }
            schemaDetail.setVisible(false);
        } else if (!stack.isEmpty() && stack.getItem() instanceof ItemModular) {
            previewStack = buildPreviewStack(schema, stack, tileEntity.getMaterials());

            schemaDetail.update(viewingPlayer, schema, stack, tileEntity.getMaterials());
            schemaDetail.toggleButton(schema.canApplyUpgrade(viewingPlayer, stack, tileEntity.getMaterials()));

            schemaList.setVisible(false);
            schemaDetail.setVisible(true);
        }

        componentList.update(stack, previewStack);
        statGroup.setItemStack(stack, previewStack, viewingPlayer);
        integrityBar.setItemStack(stack, previewStack);


        if (!ItemStack.areItemStackTagsEqual(targetStack, stack)) {
            targetStack = stack;
        }
    }

    private ItemStack buildPreviewStack(UpgradeSchema schema, ItemStack targetStack, ItemStack[] materials) {
        if (schema.isMaterialsValid(targetStack, materials)) {
            return schema.applyUpgrade(targetStack, materials, false, null);
        }
        return ItemStack.EMPTY;
    }
}
