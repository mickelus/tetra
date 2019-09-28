package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
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
import se.mickelus.tetra.module.schema.UpgradeSchema;
import se.mickelus.tetra.util.CastOptional;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@SideOnly(Side.CLIENT)
public class GuiWorkbench extends GuiContainer {

    private static GuiWorkbench instance;

    private static final String WORKBENCH_TEXTURE = "textures/gui/workbench.png";
    private static final String INVENTORY_TEXTURE = "textures/gui/player-inventory.png";

    private EntityPlayer viewingPlayer;

    private final TileEntityWorkbench tileEntity;
    private final ContainerWorkbench container;

    private GuiElement defaultGui;

    private GuiModuleList moduleList;
    private GuiStatGroup statGroup;
    private GuiIntegrityBar integrityBar;
    private GuiActionList actionList;

    private final GuiInventoryInfo inventoryInfo;
    private String selectedSlot;
    private int previewMaterialSlot = -1;

    private GuiSlotDetail slotDetail;

    private ItemStack currentTarget = ItemStack.EMPTY;
    private ItemStack currentPreview = ItemStack.EMPTY;
    private UpgradeSchema currentSchema = null;

    private ItemStack[] currentMaterials;

    private boolean hadItem = false;

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

        moduleList = new GuiModuleList(164, 49, this::selectSlot, this::updateSlotHoverPreview);
        defaultGui.addChild(moduleList);

        statGroup = new GuiStatGroup(60, 0);
        defaultGui.addChild(statGroup);

        integrityBar = new GuiIntegrityBar(160, 90);
        defaultGui.addChild(integrityBar);

        inventoryInfo = new GuiInventoryInfo(84, 164, viewingPlayer);
        defaultGui.addChild(inventoryInfo);

        actionList = new GuiActionList(0, 120);
        actionList.setAttachmentAnchor(GuiAttachment.topCenter);
        actionList.setAttachmentPoint(GuiAttachment.middleCenter);
        defaultGui.addChild(actionList);

        slotDetail = new GuiSlotDetail(46, 102,
                schema -> tileEntity.setCurrentSchema(schema, selectedSlot),
                () -> selectSlot(null),
                this::craftUpgrade,
                this::previewTweaks,
                this::applyTweaks);
        defaultGui.addChild(slotDetail);

        tileEntity.addChangeListener("gui.workbench", this::onTileEntityChange);

        currentMaterials = new ItemStack[TileEntityWorkbench.MATERIAL_SLOT_COUNT];
        Arrays.fill(currentMaterials, ItemStack.EMPTY);

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
            tooltipLines = tooltipLines.stream()
                    .map(line -> line.replace("\\n", "\n"))
                    .collect(Collectors.toList());

            GuiUtils.drawHoveringText(tooltipLines, mouseX, mouseY, width, height, -1, fontRenderer);
        }

        updateMaterialHoverPreview();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        defaultGui.onClick(mouseX, mouseY);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);

        defaultGui.mouseReleased(mouseX, mouseY);
    }

    private void selectSlot(String slotKey) {
        selectedSlot = slotKey;
        tileEntity.clearSchema();
        moduleList.setFocus(selectedSlot);

        if (selectedSlot != null) {
            slotDetail.onTileEntityChange(viewingPlayer, tileEntity, tileEntity.getTargetItemStack(), selectedSlot, tileEntity.getCurrentSchema());
        }
        slotDetail.setVisible(selectedSlot != null);
    }

    private void deselectSchema() {
        tileEntity.clearSchema();
    }

    private void craftUpgrade() {
        tileEntity.initiateCrafting(viewingPlayer);
    }

    private void previewTweaks(Map<String, Integer> tweakMap) {
        ItemStack previewStack = currentTarget.copy();
        CastOptional.cast(previewStack.getItem(), ItemModular.class)
                .map(item -> item.getModuleFromSlot(previewStack, selectedSlot))
                .ifPresent(module -> tweakMap.forEach((tweakKey, step) -> {
                    if (module.hasTweak(previewStack, tweakKey)) {
                        module.setTweakStep(previewStack, tweakKey, step);
                    }
                }));

        statGroup.update(currentTarget, previewStack, null, null, viewingPlayer);
    }

    private void applyTweaks(Map<String, Integer> tweakMap) {
        tileEntity.applyTweaks(viewingPlayer, selectedSlot, tweakMap);
    }

    private void onTileEntityChange() {
        ItemStack newTarget = tileEntity.getTargetItemStack();
        ItemStack newPreview = ItemStack.EMPTY;
        UpgradeSchema newSchema = tileEntity.getCurrentSchema();
        String currentSlot = tileEntity.getCurrentSlot();

        if (newTarget.getItem() instanceof ItemModular && currentSchema != null) {
            newPreview = buildPreviewStack(currentSchema, newTarget, tileEntity.getMaterials());
        }

        boolean targetItemChanged = !ItemStack.areItemStacksEqual(currentTarget, newTarget);
        boolean previewChanged = !ItemStack.areItemStacksEqual(currentPreview, newPreview);
        boolean schemaChanged = !Objects.equals(currentSchema, newSchema);
        boolean materialsChanged = diffMaterials(tileEntity.getMaterials());

        currentPreview = newPreview;
        currentSchema = newSchema;

        if (targetItemChanged) {
            ItemStack.areItemStacksEqual(currentTarget, newTarget);
            currentTarget = newTarget.copy();
            selectedSlot = null;
        }

        boolean slotChanged = !Objects.equals(selectedSlot, currentSlot);

        if (!currentTarget.isEmpty() && currentSlot != null) {
            selectedSlot = currentSlot;
        }

        container.updateSlots();

        if (slotChanged || targetItemChanged) {
            actionList.updateActions(currentTarget, tileEntity.getAvailableActions(viewingPlayer), viewingPlayer,
                    action -> tileEntity.performAction(viewingPlayer, action.getKey()));
        }

        if (targetItemChanged || previewChanged || schemaChanged || slotChanged || materialsChanged) {
            updateItemDisplay(currentTarget, currentPreview);

            if (currentTarget.getItem() instanceof ItemModular) {
                slotDetail.onTileEntityChange(viewingPlayer, tileEntity, currentTarget, selectedSlot, currentSchema);
            }
        }

        inventoryInfo.update(currentSchema, currentTarget);

        if (!currentTarget.isEmpty()) {
            if (!hadItem) {
                hadItem = true;
                if (targetItemChanged && currentSlot == null) {
                    itemShowAnimation();
                }
            }
        } else {
            hadItem = false;
        }

        if (!currentTarget.isEmpty()) {
            if (currentSchema == null && selectedSlot == null) {
                actionList.setVisible(true);
                slotDetail.setVisible(false);
            } else if (currentTarget.getItem() instanceof ItemModular) {
                actionList.setVisible(false);
                slotDetail.setVisible(selectedSlot != null);
            }
        } else {
            actionList.setVisible(false);
            slotDetail.setVisible(false);
        }
    }

    private boolean diffMaterials(ItemStack[] newMaterials) {
        boolean isDiff = false;
        for (int i = 0; i < newMaterials.length; i++) {
            if (!ItemStack.areItemStacksEqual(newMaterials[i], currentMaterials[i])) {
                isDiff = true;
                break;
            }
        }

        for (int i = 0; i < newMaterials.length; i++) {
            currentMaterials[i] = newMaterials[i].copy();
        }

        return isDiff;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        World world = tileEntity.getWorld();
        BlockPos pos = tileEntity.getPos();
        int[] availableCapabilities = CapabilityHelper.getCombinedCapabilityLevels(viewingPlayer, world, pos,
                world.getBlockState(pos));

        inventoryInfo.update(tileEntity.getCurrentSchema(), currentTarget);

        if (tileEntity.getCurrentSchema() != null && slotDetail.isVisible()) {
            slotDetail.update(viewingPlayer, tileEntity, availableCapabilities);
        }

        if (actionList.isVisible()) {
            actionList.updateCapabilities(availableCapabilities);
        }
    }

    private void updateItemDisplay(ItemStack itemStack, ItemStack previewStack) {
        moduleList.update(itemStack, previewStack, selectedSlot);
        statGroup.update(itemStack, previewStack, null, null, viewingPlayer);
        integrityBar.setItemStack(itemStack, previewStack);
        slotDetail.updatePreview(currentSchema, selectedSlot, itemStack, previewStack);
    }

    private void itemShowAnimation() {
        moduleList.showAnimation();
        statGroup.showAnimation();
        integrityBar.showAnimation();
        actionList.showAnimation();
    }

    private void updateSlotHoverPreview(String slot, String improvement) {
        if (tileEntity.getCurrentSlot() == null) {
            ItemStack itemStack = tileEntity.getTargetItemStack();
            statGroup.update(itemStack, ItemStack.EMPTY, slot, improvement, viewingPlayer);
        }
    }

    private void updateMaterialHoverPreview() {
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

    private ItemStack buildPreviewStack(UpgradeSchema schema, ItemStack targetStack, ItemStack[] materials) {
        if (schema.isMaterialsValid(targetStack, materials)) {
            return schema.applyUpgrade(targetStack, materials, false, tileEntity.getCurrentSlot(), null);
        }
        return ItemStack.EMPTY;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        slotDetail.keyTyped(typedChar);
        super.keyTyped(typedChar, keyCode);
    }
}
