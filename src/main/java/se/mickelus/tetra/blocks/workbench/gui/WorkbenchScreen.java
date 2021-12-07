package se.mickelus.tetra.blocks.workbench.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolAction;
import se.mickelus.mutil.gui.GuiAttachment;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.GuiTexture;
import se.mickelus.mutil.gui.GuiTextureOffset;
import se.mickelus.tetra.blocks.salvage.InteractiveBlockOverlay;
import se.mickelus.tetra.blocks.workbench.WorkbenchContainer;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.gui.HoneProgressGui;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;
import se.mickelus.tetra.properties.PropertyHelper;
import se.mickelus.mutil.util.CastOptional;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
@OnlyIn(Dist.CLIENT)
public class WorkbenchScreen extends AbstractContainerScreen<WorkbenchContainer> {
    private Player viewingPlayer;

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

    public WorkbenchScreen(WorkbenchContainer container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);

        this.imageWidth = 320;
        this.imageHeight = 240;

        this.tileEntity = container.getTileEntity();
        this.container = container;

        defaultGui = new GuiElement(0, 0, imageWidth, imageHeight);
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
    public void init() {
        super.init();

        viewingPlayer = minecraft.player;
    }

    @Override
    public void render(PoseStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        this.renderBackground(matrixStack, 0);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        renderTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        defaultGui.draw(matrixStack, this.leftPos, this.topPos, width, height, mouseX, mouseY, 1);
    }

    // override this to stop titles from rendering
    @Override
    protected void renderLabels(PoseStack matrixStack, int x, int y) { }

    @Override
    protected void renderTooltip(PoseStack matrixStack, int mouseX, int mouseY) {
        super.renderTooltip(matrixStack, mouseX, mouseY);

        List<String> tooltipLines = defaultGui.getTooltipLines();
        if (tooltipLines != null) {
            List<Component> textComponents = tooltipLines.stream()
                    .map(line -> line.replace("\\n", "\n"))
                    .flatMap(line -> Arrays.stream(line.split("\n")))
                    .map(TextComponent::new)
                    .collect(Collectors.toList());

            renderTooltip(matrixStack, textComponents, Optional.empty(), mouseX, mouseY);
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
        CastOptional.cast(previewStack.getItem(), IModularItem.class)
                .map(item -> item.getModuleFromSlot(previewStack, selectedSlot))
                .ifPresent(module -> tweakMap.forEach((tweakKey, step) -> {
                    if (module.hasTweak(previewStack, tweakKey)) {
                        module.setTweakStep(previewStack, tweakKey, step);
                    }
                    IModularItem.updateIdentifier(previewStack);
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

        if (newTarget.getItem() instanceof IModularItem && newSchematic != null) {
            newPreview = buildPreviewStack(newSchematic, newTarget, currentSlot, tileEntity.getMaterials());
        }

        boolean targetItemChanged = !ItemStack.matches(currentTarget, newTarget);
        boolean previewChanged = !ItemStack.matches(currentPreview, newPreview);
        boolean schematicChanged = !Objects.equals(currentSchematic, newSchematic);
        boolean materialsChanged = diffMaterials(tileEntity.getMaterials());

        currentPreview = newPreview;
        currentSchematic = newSchematic;

        if (targetItemChanged) {
            ItemStack.matches(currentTarget, newTarget);
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
                    action -> tileEntity.performAction(viewingPlayer, action.getKey()), tileEntity);

            // block overlay needs refresh as some actions have in world interactions
            InteractiveBlockOverlay.markDirty();
        }

        if (targetItemChanged || previewChanged || schematicChanged || slotChanged || materialsChanged) {
            updateItemDisplay(currentTarget, currentPreview);

            if (currentTarget.getItem() instanceof IModularItem) {
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
            } else if (currentTarget.getItem() instanceof IModularItem) {
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
            if (!ItemStack.matches(newMaterials[i], currentMaterials[i])) {
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
    protected void containerTick() {
        inventoryInfo.update(tileEntity.getCurrentSchematic(), tileEntity.getCurrentSlot(), currentTarget);

        Level world = tileEntity.getLevel();
        if (isDirty) {
            onTileEntityChange();
            isDirty = false;
        } else if (world != null && world.getGameTime() % 20 == 0) {
            BlockPos pos = tileEntity.getBlockPos();
            Map<ToolAction, Integer> availableTools = PropertyHelper.getCombinedToolLevels(viewingPlayer, world, pos, world.getBlockState(pos));

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
        if (currentSchematic != null && hoveredSlot != null && hoveredSlot.hasItem()) {
            newPreviewMaterialSlot = hoveredSlot.getSlotIndex();
        }

        if (newPreviewMaterialSlot != previewMaterialSlot && targetStack.getItem() instanceof IModularItem) {
            ItemStack[] materials = tileEntity.getMaterials();
            if (newPreviewMaterialSlot != -1 && Arrays.stream(materials).allMatch(ItemStack::isEmpty)) {
                ItemStack previewStack = buildPreviewStack(currentSchematic, targetStack, selectedSlot, new ItemStack[]{hoveredSlot.getItem()});
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
            ItemStack result = schematic.applyUpgrade(targetStack, materials, false, slot, null);

            boolean willReplace = schematic.willReplace(targetStack, materials, slot);

            Map<ToolAction, Integer> tools = schematic.getRequiredToolLevels(targetStack, materials);

            for (Map.Entry<ToolAction, Integer> entry: tools.entrySet()) {
                result = WorkbenchTile.consumeCraftingToolEffects(result, slot, willReplace, entry.getKey(), entry.getValue(), viewingPlayer,
                        tileEntity.getLevel(), tileEntity.getBlockPos(), tileEntity.getBlockState(), false);
            }

            result = WorkbenchTile.applyCraftingBonusEffects(result, slot, willReplace, viewingPlayer, materials, materials, tools,
                    tileEntity.getLevel(), tileEntity.getBlockPos(), tileEntity.getBlockState(), false);

            IModularItem.updateIdentifier(result);
            return result;
        }
        return ItemStack.EMPTY;
    }
}
