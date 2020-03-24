package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.gui.GuiUtils;
import se.mickelus.mgui.gui.GuiAttachment;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiTexture;
import se.mickelus.mgui.gui.GuiTextureOffset;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.workbench.WorkbenchContainer;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;
import se.mickelus.tetra.capabilities.CapabilityHelper;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.items.modular.ItemModular;
import se.mickelus.tetra.module.schema.UpgradeSchema;
import se.mickelus.tetra.util.CastOptional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class WorkbenchScreen extends ContainerScreen<WorkbenchContainer> {
    private static final ResourceLocation inventoryTexture = new ResourceLocation(TetraMod.MOD_ID, "textures/gui/player-inventory.png");

    private PlayerEntity viewingPlayer;

    private final WorkbenchTile tileEntity;
    private final WorkbenchContainer container;

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

    public WorkbenchScreen(WorkbenchContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);

        this.xSize = 320;
        this.ySize = 240;

        this.tileEntity = container.getTileEntity();
        this.container = container;

        defaultGui = new GuiElement(0, 0, xSize, ySize);
        defaultGui.addChild(new GuiTextureOffset(134, 40, 51, 51, GuiTextures.workbench));
        defaultGui.addChild(new GuiTexture(72, 153, 179, 106, inventoryTexture));

        moduleList = new GuiModuleList(164, 49, this::selectSlot, this::updateSlotHoverPreview);
        defaultGui.addChild(moduleList);

        statGroup = new GuiStatGroup(60, 0);
        defaultGui.addChild(statGroup);

        integrityBar = new GuiIntegrityBar(160, 90);
        defaultGui.addChild(integrityBar);

        inventoryInfo = new GuiInventoryInfo(84, 164, Minecraft.getInstance().player);
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

        currentMaterials = new ItemStack[WorkbenchTile.inventorySlots];
        Arrays.fill(currentMaterials, ItemStack.EMPTY);
    }

    @Override
    public void init(Minecraft minecraft, int width, int height) {
        super.init(minecraft, width, height);

        viewingPlayer = minecraft.player;
    }

    @Override
    public void render(final int mouseX, final int mouseY, final float partialTicks) {
        this.renderBackground();
        super.render(mouseX, mouseY, partialTicks);
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
                    .flatMap(line -> Arrays.stream(line.split("\n")))
                    .collect(Collectors.toList());

            GuiUtils.drawHoveringText(tooltipLines, mouseX, mouseY, width, height, -1, minecraft.fontRenderer);
        }

        updateMaterialHoverPreview();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        return defaultGui.onClick((int) mouseX, (int) mouseY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);

        defaultGui.mouseReleased((int) mouseX, (int) mouseY);

        return true;
    }

    @Override
    public boolean charTyped(char typecChar, int keyCode) {
        slotDetail.keyTyped(typecChar);
        return false;
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
            newPreview = buildPreviewStack(currentSchema, newTarget, selectedSlot, tileEntity.getMaterials());
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

        inventoryInfo.update(currentSchema, currentSlot, currentTarget);

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
    public void tick() {
        super.tick();

        World world = tileEntity.getWorld();
        BlockPos pos = tileEntity.getPos();
        int[] availableCapabilities = CapabilityHelper.getCombinedCapabilityLevels(viewingPlayer, world, pos, world.getBlockState(pos));

        inventoryInfo.update(tileEntity.getCurrentSchema(), tileEntity.getCurrentSlot(), currentTarget);

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
        ItemStack targetStack = tileEntity.getTargetItemStack();
        if (currentSchema != null && hoveredSlot != null && hoveredSlot.getHasStack()) {
            newPreviewMaterialSlot = hoveredSlot.getSlotIndex();
        }

        if (newPreviewMaterialSlot != previewMaterialSlot && targetStack.getItem() instanceof ItemModular) {
            ItemStack[] materials = tileEntity.getMaterials();
            if (newPreviewMaterialSlot != -1 && Arrays.stream(materials).allMatch(ItemStack::isEmpty)) {
                ItemStack previewStack = buildPreviewStack(currentSchema, targetStack, selectedSlot, new ItemStack[]{hoveredSlot.getStack()});
                updateItemDisplay(targetStack, previewStack);
            } else {
                ItemStack previewStack = ItemStack.EMPTY;
                if (currentSchema != null) {
                    previewStack = buildPreviewStack(currentSchema, targetStack, selectedSlot, materials);
                }
                updateItemDisplay(targetStack, previewStack);
            }
            previewMaterialSlot = newPreviewMaterialSlot;
        }
    }

    private ItemStack buildPreviewStack(UpgradeSchema schema, ItemStack targetStack, String slot, ItemStack[] materials) {
        if (schema.isMaterialsValid(targetStack, slot, materials)) {
            return schema.applyUpgrade(targetStack, materials, false, tileEntity.getCurrentSlot(), null);
        }
        return ItemStack.EMPTY;
    }
}
