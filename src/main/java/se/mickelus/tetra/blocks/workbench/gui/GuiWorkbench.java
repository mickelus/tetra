package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import se.mickelus.tetra.blocks.workbench.ContainerWorkbench;
import se.mickelus.tetra.blocks.workbench.TileEntityWorkbench;
import se.mickelus.tetra.capabilities.CapabilityHelper;
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
    private GuiActionList actionList;

    private GuiSchemaDetail schemaDetail;
    private final GuiInventoryInfo inventoryInfo;
    private String selectedSlot;
    private int previewMaterialSlot = -1;


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

        inventoryInfo = new GuiInventoryInfo(84, 164, viewingPlayer);
        defaultGui.addChild(inventoryInfo);

        actionList = new GuiActionList(0, 120);
        actionList.setAttachmentAnchor(GuiAttachment.topCenter);
        actionList.setAttachmentPoint(GuiAttachment.middleCenter);
        defaultGui.addChild(actionList);

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
        if (tooltipLines != null) {
            GuiUtils.drawHoveringText(tooltipLines, mouseX, mouseY, width, height, -1, fontRenderer);
        }

        updateHoverPreview();
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

        if (!ItemStack.areItemStacksEqual(targetStack, itemStack)) {
            targetStack = itemStack;
            selectedSlot = null;
        }

        if (!itemStack.isEmpty() && currentSlot != null) {
            selectedSlot = currentSlot;
        }

        container.updateSlots();

        if (!itemStack.isEmpty() && tileEntity.getCurrentSchema() == null && selectedSlot == null) {
            actionList.updateActions(targetStack, tileEntity.getAvailableActions(viewingPlayer), viewingPlayer,
                    action -> tileEntity.performAction(viewingPlayer, action.getKey()));
            actionList.setVisible(true);
        } else {
            actionList.setVisible(false);
        }

        if (currentSchema == null) {
            updateSchemaList();
            schemaDetail.setVisible(false);
        } else if (!itemStack.isEmpty() && itemStack.getItem() instanceof ItemModular) {
            previewStack = buildPreviewStack(currentSchema, itemStack, tileEntity.getMaterials());

            World world = tileEntity.getWorld();
            BlockPos pos = tileEntity.getPos();
            int[] availableCapabilities = CapabilityHelper.getCombinedCapabilityLevels(viewingPlayer, world, pos,
                    world.getBlockState(pos));

            schemaDetail.update(currentSchema, itemStack, tileEntity.getMaterials(), availableCapabilities);
            schemaDetail.toggleButton(currentSchema.canApplyUpgrade(viewingPlayer, itemStack, tileEntity.getMaterials(),
                    currentSlot, availableCapabilities));

            schemaList.setVisible(false);
            schemaDetail.setVisible(true);
        }

        inventoryInfo.update(currentSchema, targetStack);
        updateItemDisplay(itemStack, previewStack);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        World world = tileEntity.getWorld();
        BlockPos pos = tileEntity.getPos();
        int[] availableCapabilities = CapabilityHelper.getCombinedCapabilityLevels(viewingPlayer, world, pos,
                world.getBlockState(pos));

        inventoryInfo.update(tileEntity.getCurrentSchema(), targetStack);

        if (tileEntity.getCurrentSchema() != null && schemaDetail.isVisible()) {
            schemaDetail.updateAvailableCapabilities(availableCapabilities);

            schemaDetail.toggleButton(
                    tileEntity.getCurrentSchema().canApplyUpgrade(
                            viewingPlayer,
                            tileEntity.getTargetItemStack(),
                            tileEntity.getMaterials(),
                            tileEntity.getCurrentSlot(),
                            availableCapabilities));
        }

        if (actionList.isVisible()) {
            actionList.updateCapabilities(availableCapabilities);
        }
    }

    private void updateItemDisplay(ItemStack itemStack, ItemStack previewStack) {
        moduleList.update(itemStack, previewStack, selectedSlot);
        statGroup.setItemStack(itemStack, previewStack, viewingPlayer);
        integrityBar.setItemStack(itemStack, previewStack);
    }

    private void updateHoverPreview() {
        int newPreviewMaterialSlot = -1;
        Slot hoveredSlot = getSlotUnderMouse();
        UpgradeSchema currentSchema = tileEntity.getCurrentSchema();
        if (currentSchema != null && hoveredSlot != null && hoveredSlot.getHasStack()) {
            newPreviewMaterialSlot = hoveredSlot.getSlotIndex();
        }

        if (newPreviewMaterialSlot != previewMaterialSlot) {
            ItemStack[] materials = tileEntity.getMaterials();
            if (newPreviewMaterialSlot != -1 && Arrays.stream(materials).allMatch(ItemStack::isEmpty)) {
                ItemStack previewStack = buildPreviewStack(currentSchema, tileEntity.getTargetItemStack(), new ItemStack[]{hoveredSlot.getStack()});
                updateItemDisplay(tileEntity.getTargetItemStack(), previewStack);
            } else {
                ItemStack previewStack = ItemStack.EMPTY;
                if (currentSchema != null) {
                    previewStack = buildPreviewStack(currentSchema, tileEntity.getTargetItemStack(), materials);
                }
                updateItemDisplay(tileEntity.getTargetItemStack(), previewStack);
            }
            previewMaterialSlot = newPreviewMaterialSlot;
        }
    }

    private void updateSchemaList() {
        if (selectedSlot != null) {
            ItemStack targetStack = tileEntity.getTargetItemStack();
            UpgradeSchema[] schemas = ItemUpgradeRegistry.instance.getAvailableSchemas(viewingPlayer, targetStack);
            schemas = Arrays.stream(schemas)
                    .filter(upgradeSchema -> upgradeSchema.isApplicableForSlot(selectedSlot, targetStack))
                    .sorted(Comparator.comparing(UpgradeSchema::getType).thenComparing(UpgradeSchema::getRarity).thenComparing(UpgradeSchema::getKey))
                    .toArray(UpgradeSchema[]::new);
            schemaList.setSchemas(schemas);
            schemaList.setVisible(true);
        } else {
            schemaList.setVisible(false);
        }

    }

    private ItemStack buildPreviewStack(UpgradeSchema schema, ItemStack targetStack, ItemStack[] materials) {
        if (schema.isMaterialsValid(targetStack, materials)) {
            return schema.applyUpgrade(targetStack, materials, false, tileEntity.getCurrentSlot(), null);
        }
        return ItemStack.EMPTY;
    }
}
