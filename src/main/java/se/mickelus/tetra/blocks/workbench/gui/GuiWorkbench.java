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
import se.mickelus.tetra.blocks.workbench.ContainerWorkbench;
import se.mickelus.tetra.blocks.workbench.TileEntityWorkbench;
import se.mickelus.tetra.blocks.workbench.action.BreakAction;
import se.mickelus.tetra.blocks.workbench.action.RepairAction;
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

    private GuiModuleList moduleList;
    private GuiStatGroup statGroup;
    private GuiIntegrityBar integrityBar;
    private GuiSchemaList schemaList;

    private GuiSchemaDetail schemaDetail;
    private String selectedSlot;

    private GuiActionButton geodeActionButton;
    private final GuiActionButton repairActionButton;


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

        moduleList = new GuiModuleList(164, 49, this::selectSlot);
        defaultGui.addChild(moduleList);

        statGroup = new GuiStatGroup(60, 0);
        defaultGui.addChild(statGroup);

        integrityBar = new GuiIntegrityBar(160, 90);
        defaultGui.addChild(integrityBar);

        schemaList = new GuiSchemaList(46, 102,
                schema -> tileEntity.setCurrentSchema(schema, selectedSlot),
                () -> selectSlot(null));
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

        repairActionButton = new GuiActionButton(140, 114, I18n.format("workbench.repair"), Capability.hammer, () -> {
            tileEntity.performAction(viewingPlayer, RepairAction.key);
        });
        repairActionButton.setVisible(false);
        defaultGui.addChild(repairActionButton);

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

    private void selectSlot(String slotKey) {
        selectedSlot = slotKey;
        tileEntity.clearSchema();
        moduleList.setFocus(selectedSlot);
        updateSchemaList();
    }

    private void deselectSchema() {
        tileEntity.clearSchema();
    }

    private void craftUpgrade() {
        tileEntity.initiateCrafting(viewingPlayer);
    }

    private void onTileEntityChange() {
        ItemStack itemStack = tileEntity.getTargetItemStack();
        ItemStack previewStack = ItemStack.EMPTY;
        UpgradeSchema currentSchema = tileEntity.getCurrentSchema();
        String currentSlot = tileEntity.getCurrentSlot();

        if (!ItemStack.areItemStackTagsEqual(targetStack, itemStack)) {
            targetStack = itemStack;
            selectedSlot = null;
        }

        if (!itemStack.isEmpty() && currentSlot != null) {
            selectedSlot = currentSlot;
        }

        container.updateSlots();

        geodeActionButton.setVisible(false);
        repairActionButton.setVisible(false);

        if (currentSchema == null) {
            updateSchemaList();
            schemaDetail.setVisible(false);
        } else if (!itemStack.isEmpty() && itemStack.getItem() instanceof ItemModular) {
            previewStack = buildPreviewStack(currentSchema, itemStack, tileEntity.getMaterials());

            schemaDetail.update(viewingPlayer, currentSchema, itemStack, tileEntity.getMaterials());
            schemaDetail.toggleButton(currentSchema.canApplyUpgrade(viewingPlayer, itemStack, tileEntity.getMaterials()));

            schemaList.setVisible(false);
            schemaDetail.setVisible(true);
        }

        moduleList.update(itemStack, previewStack, currentSlot != null ? currentSlot : selectedSlot);
        statGroup.setItemStack(itemStack, previewStack, viewingPlayer);
        integrityBar.setItemStack(itemStack, previewStack);
    }

    private void updateSchemaList() {
        if (selectedSlot != null) {
            UpgradeSchema[] schemas = ItemUpgradeRegistry.instance.getAvailableSchemas(viewingPlayer, tileEntity.getTargetItemStack());
            schemas = Arrays.stream(schemas)
                    .filter(upgradeSchema -> upgradeSchema.isApplicableForSlot(selectedSlot))
                    .sorted(Comparator.comparing(UpgradeSchema::getType).thenComparing(UpgradeSchema::getKey))
                    .toArray(UpgradeSchema[]::new);
            schemaList.setSchemas(schemas);
            schemaList.setVisible(true);
        } else {
            schemaList.setVisible(false);

            if (tileEntity.canPerformAction(viewingPlayer, BreakAction.key)) {
                geodeActionButton.update(viewingPlayer, 2);
                geodeActionButton.setVisible(true);
            }

            if (tileEntity.canPerformAction(viewingPlayer, RepairAction.key)) {
                repairActionButton.update(viewingPlayer, 0);
                repairActionButton.setVisible(true);
            }
        }

    }

    private ItemStack buildPreviewStack(UpgradeSchema schema, ItemStack targetStack, ItemStack[] materials) {
        if (schema.isMaterialsValid(targetStack, materials)) {
            return schema.applyUpgrade(targetStack, materials, false, null);
        }
        return ItemStack.EMPTY;
    }
}
