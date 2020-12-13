package se.mickelus.tetra.blocks.workbench.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.client.gui.GuiUtils;
import se.mickelus.mgui.gui.GuiAttachment;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiTexture;
import se.mickelus.mgui.gui.GuiTextureOffset;
import se.mickelus.tetra.blocks.salvage.InteractiveBlockOverlay;
import se.mickelus.tetra.blocks.workbench.WorkbenchContainer;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;
import se.mickelus.tetra.gui.HoneProgressGui;
import se.mickelus.tetra.properties.PropertyHelper;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;
import se.mickelus.tetra.util.CastOptional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class WorkbenchScreen extends ContainerScreen<WorkbenchContainer> {
    private PlayerEntity viewingPlayer;

    private final WorkbenchTile tileEntity;
    private final WorkbenchContainer container;

    private GuiElement defaultGui;

    private GuiModuleList moduleList;
    private WorkbenchStatsGui statGroup;
    private GuiIntegrityBar integrityBar;
    private HoneProgressGui honeBar;
    private GuiActionList actionList;

    private final GuiInventoryInfo inventoryInfo;
    private String selectedSlot;
    private int previewMaterialSlot = -1;

    private GuiSlotDetail slotDetail;

    private ItemStack currentTarget = ItemStack.EMPTY;
    private ItemStack currentPreview = ItemStack.EMPTY;
    private UpgradeSchematic currentSchematic = null;

    private ItemStack[] currentMaterials;

    private boolean hadItem = false;

    private boolean isDirty = false;

    public WorkbenchScreen(WorkbenchContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);

        this.xSize = 320;
        this.ySize = 240;

        this.tileEntity = container.getTileEntity();
        this.container = container;

        defaultGui = new GuiElement(0, 0, xSize, ySize);
        defaultGui.addChild(new GuiTextureOffset(134, 40, 51, 51, GuiTextures.workbench));
        defaultGui.addChild(new GuiTexture(72, 153, 179, 106, GuiTextures.playerInventory));

        moduleList = new GuiModuleList(164, 49, this::selectSlot, this::updateSlotHoverPreview);
        defaultGui.addChild(moduleList);

        statGroup = new WorkbenchStatsGui(60, 0);
        defaultGui.addChild(statGroup);

        integrityBar = new GuiIntegrityBar(0, 90);
        integrityBar.setAttachmentAnchor(GuiAttachment.topCenter);
        defaultGui.addChild(integrityBar);

        honeBar = new HoneProgressGui(0, 90);
        honeBar.setAttachmentAnchor(GuiAttachment.topCenter);
        honeBar.setVisible(false);
        defaultGui.addChild(honeBar);

        inventoryInfo = new GuiInventoryInfo(84, 164, Minecraft.getInstance().player);
        defaultGui.addChild(inventoryInfo);

        actionList = new GuiActionList(0, 120);
        actionList.setAttachmentAnchor(GuiAttachment.topCenter);
        actionList.setAttachmentPoint(GuiAttachment.middleCenter);
        defaultGui.addChild(actionList);

        slotDetail = new GuiSlotDetail(46, 102,
                schematic -> tileEntity.setCurrentSchematic(schematic, selectedSlot),
                () -> selectSlot(null),
                this::craftUpgrade,
                this::previewTweaks,
                this::applyTweaks);
        defaultGui.addChild(slotDetail);

        /* There's some interaction between vanilla containers and forge item handlers that cause adding to a stack to call onContentsChanged
         * (and this by extent) twice before the content actually changes, which cause the UI to update incorrectly. Dirty marking fixes that issue
         * but might cause long delays if the client is laggy?
         */
        tileEntity.addChangeListener("gui.workbench", () -> isDirty = true);

        currentMaterials = new ItemStack[WorkbenchTile.inventorySlots];
        Arrays.fill(currentMaterials, ItemStack.EMPTY);
    }

    @Override
    public void init(Minecraft minecraft, int width, int height) {
        super.init(minecraft, width, height);

        viewingPlayer = minecraft.player;
    }

    @Override
    public void render(MatrixStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        this.renderBackground(matrixStack, 0);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        renderHoveredTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;

        defaultGui.draw(new MatrixStack(), x, y, width, height, mouseX, mouseY, 1);
    }

    // override this to stop titles from rendering
    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y) { }

    @Override
    protected void renderHoveredTooltip(MatrixStack matrixStack, int mouseX, int mouseY) {
        super.renderHoveredTooltip(matrixStack, mouseX, mouseY);

        List<String> tooltipLines = defaultGui.getTooltipLines();
        if (tooltipLines != null) {
            List<ITextComponent> textComponents = tooltipLines.stream()
                    .map(line -> line.replace("\\n", "\n"))
                    .flatMap(line -> Arrays.stream(line.split("\n")))
                    .map(StringTextComponent::new)
                    .collect(Collectors.toList());

            GuiUtils.drawHoveringText(matrixStack, textComponents, mouseX, mouseY, width, height, -1, minecraft.fontRenderer);
        }

        updateMaterialHoverPreview();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);

        return defaultGui.onMouseClick((int) mouseX, (int) mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);

        defaultGui.onMouseRelease((int) mouseX, (int) mouseY, button);

        return true;
    }

    @Override
    public boolean charTyped(char typecChar, int keyCode) {
        slotDetail.keyTyped(typecChar);
        return false;
    }

    private void selectSlot(String slotKey) {
        selectedSlot = slotKey;
        tileEntity.clearSchematic();
        moduleList.setFocus(selectedSlot);

        if (selectedSlot != null) {
            slotDetail.onTileEntityChange(viewingPlayer, tileEntity, tileEntity.getTargetItemStack(), selectedSlot, tileEntity.getCurrentSchematic());
        }
        slotDetail.setVisible(selectedSlot != null);
    }

    private void deselectSchematic() {
        tileEntity.clearSchematic();
    }

    private void craftUpgrade() {
        tileEntity.initiateCrafting(viewingPlayer);
    }

    private void previewTweaks(Map<String, Integer> tweakMap) {
        ItemStack previewStack = currentTarget.copy();
        CastOptional.cast(previewStack.getItem(), ModularItem.class)
                .map(item -> item.getModuleFromSlot(previewStack, selectedSlot))
                .ifPresent(module -> tweakMap.forEach((tweakKey, step) -> {
                    if (module.hasTweak(previewStack, tweakKey)) {
                        module.setTweakStep(previewStack, tweakKey, step);
                    }
                    ModularItem.updateIdentifier(previewStack);
                }));

        statGroup.update(currentTarget, previewStack, null, null, viewingPlayer);
    }

    private void applyTweaks(Map<String, Integer> tweakMap) {
        tileEntity.applyTweaks(viewingPlayer, selectedSlot, tweakMap);
    }

    private void onTileEntityChange() {
        ItemStack newTarget = tileEntity.getTargetItemStack();
        ItemStack newPreview = ItemStack.EMPTY;
        UpgradeSchematic newSchematic = tileEntity.getCurrentSchematic();
        String currentSlot = tileEntity.getCurrentSlot();

        if (newTarget.getItem() instanceof ModularItem && currentSchematic != null) {
            newPreview = buildPreviewStack(currentSchematic, newTarget, selectedSlot, tileEntity.getMaterials());
        }

        boolean targetItemChanged = !ItemStack.areItemStacksEqual(currentTarget, newTarget);
        boolean previewChanged = !ItemStack.areItemStacksEqual(currentPreview, newPreview);
        boolean schematicChanged = !Objects.equals(currentSchematic, newSchematic);
        boolean materialsChanged = diffMaterials(tileEntity.getMaterials());

        currentPreview = newPreview;
        currentSchematic = newSchematic;

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

            // block overlay needs refresh as some actions have in world interactions
            InteractiveBlockOverlay.markDirty();
        }

        if (targetItemChanged || previewChanged || schematicChanged || slotChanged || materialsChanged) {
            updateItemDisplay(currentTarget, currentPreview);

            if (currentTarget.getItem() instanceof ModularItem) {
                slotDetail.onTileEntityChange(viewingPlayer, tileEntity, currentTarget, selectedSlot, currentSchematic);
            }
        }

        inventoryInfo.update(currentSchematic, currentSlot, currentTarget);

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
            if (currentSchematic == null && selectedSlot == null) {
                actionList.setVisible(true);
                slotDetail.setVisible(false);
            } else if (currentTarget.getItem() instanceof ModularItem) {
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

        inventoryInfo.update(tileEntity.getCurrentSchematic(), tileEntity.getCurrentSlot(), currentTarget);

        World world = tileEntity.getWorld();
        if (isDirty) {
            onTileEntityChange();
            isDirty = false;
        } else if (world != null && world.getGameTime() % 20 == 0) {
            BlockPos pos = tileEntity.getPos();
            Map<ToolType, Integer> availableTools = PropertyHelper.getCombinedToolLevels(viewingPlayer, world, pos, world.getBlockState(pos));

            if (tileEntity.getCurrentSchematic() != null && slotDetail.isVisible()) {
                slotDetail.update(viewingPlayer, tileEntity, availableTools);
            }

            if (actionList.isVisible()) {
                actionList.updateTools(availableTools);
            }
        }
    }

    private void updateItemDisplay(ItemStack itemStack, ItemStack previewStack) {
        moduleList.update(itemStack, previewStack, selectedSlot);
        statGroup.update(itemStack, previewStack, null, null, viewingPlayer);
        slotDetail.updatePreview(currentSchematic, selectedSlot, itemStack, previewStack);

        integrityBar.setItemStack(itemStack, previewStack);
        honeBar.update(itemStack, tileEntity.isTargetPlaceholder());
        honeBar.setX(Math.max(integrityBar.getWidth() / 2 + 8, 35));
    }

    private void itemShowAnimation() {
        moduleList.showAnimation();
        statGroup.showAnimation();
        integrityBar.showAnimation();
        honeBar.showAnimation();
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
        UpgradeSchematic currentSchematic = tileEntity.getCurrentSchematic();
        ItemStack targetStack = tileEntity.getTargetItemStack();
        if (currentSchematic != null && hoveredSlot != null && hoveredSlot.getHasStack()) {
            newPreviewMaterialSlot = hoveredSlot.getSlotIndex();
        }

        if (newPreviewMaterialSlot != previewMaterialSlot && targetStack.getItem() instanceof ModularItem) {
            ItemStack[] materials = tileEntity.getMaterials();
            if (newPreviewMaterialSlot != -1 && Arrays.stream(materials).allMatch(ItemStack::isEmpty)) {
                ItemStack previewStack = buildPreviewStack(currentSchematic, targetStack, selectedSlot, new ItemStack[]{hoveredSlot.getStack()});
                updateItemDisplay(targetStack, previewStack);
            } else {
                ItemStack previewStack = ItemStack.EMPTY;
                if (currentSchematic != null) {
                    previewStack = buildPreviewStack(currentSchematic, targetStack, selectedSlot, materials);
                }
                updateItemDisplay(targetStack, previewStack);
            }
            previewMaterialSlot = newPreviewMaterialSlot;
        }
    }

    private ItemStack buildPreviewStack(UpgradeSchematic schematic, ItemStack targetStack, String slot, ItemStack[] materials) {
        if (schematic.isMaterialsValid(targetStack, slot, materials)) {
            ItemStack result = schematic.applyUpgrade(targetStack, materials, false, tileEntity.getCurrentSlot(), null);

            boolean willReplace = currentSchematic.willReplace(targetStack, materials, slot);

            for (Map.Entry<ToolType, Integer> entry : currentSchematic.getRequiredToolLevels(targetStack, materials).entrySet()) {
                result = WorkbenchTile.consumeCraftingToolEffects(result, slot, willReplace, entry.getKey(), entry.getValue(), viewingPlayer,
                        tileEntity.getWorld(), tileEntity.getPos(), tileEntity.getBlockState(), false);
            }

            ModularItem.updateIdentifier(result);
            return result;
        }
        return ItemStack.EMPTY;
    }
}
