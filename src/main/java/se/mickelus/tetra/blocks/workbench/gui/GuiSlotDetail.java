package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import se.mickelus.mgui.gui.impl.GuiTabVerticalGroup;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;
import se.mickelus.tetra.capabilities.CapabilityHelper;
import se.mickelus.mgui.gui.GuiButton;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiRect;
import se.mickelus.mgui.gui.GuiTexture;
import se.mickelus.mgui.gui.animation.AnimationChain;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.schema.UpgradeSchema;
import se.mickelus.tetra.util.CastOptional;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Consumer;

public class GuiSlotDetail extends GuiElement {

    private int tab = 0;

    private GuiTabVerticalGroup tabGroup;

    private GuiModuleDetails moduleDetails;

    private GuiElement schemaGroup;
    private GuiSchemaList schemaList;
    private GuiSchemaDetail schemaDetail;

    private GuiTweakControls tweakControls;

    private Consumer<UpgradeSchema> selectSchemaHandler;

    private final AnimationChain slotTransition;

    public GuiSlotDetail(int x, int y, Consumer<UpgradeSchema> selectSchemaHandler, Runnable closeHandler,
            Runnable craftHandler, Consumer<Map<String, Integer>> previewTweak, Consumer<Map<String, Integer>> applyTweak) {
        super(x, y, 224, 67);

        this.selectSchemaHandler = selectSchemaHandler;

        addChild(new GuiTexture(0, 0, width, height, 0, 68, GuiTextures.workbench));

        addChild(new GuiRect(1, 6, 2, 49, 0));

        tabGroup = new GuiTabVerticalGroup(1, 6, this::changeTab,
                I18n.format("workbench.slot_detail.details_tab"),
                I18n.format("workbench.slot_detail.craft_tab"),
                I18n.format("workbench.slot_detail.tweak_tab")
                );
        tabGroup.setHasContent(1, true);
        addChild(tabGroup);

        moduleDetails = new GuiModuleDetails(0, 0);
        addChild(moduleDetails);

        schemaGroup = new GuiElement(0, 0, width, height);
        addChild(schemaGroup);

        schemaList = new GuiSchemaList(0, 0, selectSchemaHandler);
        schemaList.setVisible(false);
        schemaGroup.addChild(schemaList);

        schemaDetail = new GuiSchemaDetail(0, 0, () -> selectSchemaHandler.accept(null), craftHandler);
        schemaDetail.setVisible(false);
        schemaGroup.addChild(schemaDetail);


        tweakControls = new GuiTweakControls(0, 0, previewTweak, applyTweak);
        addChild(tweakControls);

        GuiRect slotTransitionElement = new GuiRect(3, 3, 218, 56, 0);
        slotTransitionElement.setOpacity(0);
        addChild(slotTransitionElement);
        slotTransition = new AnimationChain(
                new KeyframeAnimation(60, slotTransitionElement).applyTo(new Applier.Opacity(0.3f)),
                new KeyframeAnimation(100, slotTransitionElement).applyTo(new Applier.Opacity(0)));

        GuiButton buttonClose = new GuiButton(215, -4, "x", closeHandler);
        addChild(buttonClose);

        setVisible(false);
    }

    private void changeTab(int index) {
        selectSchemaHandler.accept(null);
        tab = index;

        updateTabVisibility();

        slotTransition.stop();
        slotTransition.start();
    }

    private void updateTabVisibility() {
        moduleDetails.setVisible(tab == 0);
        schemaGroup.setVisible(tab == 1);
        tweakControls.setVisible(tab == 2);
    }

    public void onTileEntityChange(PlayerEntity player, WorkbenchTile tileEntity, ItemStack itemStack, String selectedSlot, UpgradeSchema currentSchema) {
        ItemModule module = CastOptional.cast(itemStack.getItem(), ItemModular.class)
                .map(item -> item.getModuleFromSlot(itemStack, selectedSlot))
                .orElse(null);

        if (currentSchema == null) {
            updateSchemaList(player, tileEntity, selectedSlot);
        } else {
            World world = tileEntity.getWorld();
            BlockPos pos = tileEntity.getPos();
            int[] availableCapabilities = CapabilityHelper.getCombinedCapabilityLevels(player, world, pos,
                    world.getBlockState(pos));
            ItemStack[] materials = tileEntity.getMaterials();


            schemaDetail.update(currentSchema, itemStack, materials, availableCapabilities, player.experienceLevel);
            schemaDetail.updateMagicCapacity(currentSchema, selectedSlot, itemStack,
                    currentSchema.applyUpgrade(itemStack.copy(), materials, false, selectedSlot, player));
            schemaDetail.toggleButton(currentSchema.canApplyUpgrade(player, itemStack, materials,
                    selectedSlot, availableCapabilities));

            tab = 1;
        }

        moduleDetails.update(module, itemStack);
        tabGroup.setHasContent(0, module != null);

        tweakControls.update(module, itemStack);
        tabGroup.setHasContent(2, module != null && module.isTweakable(itemStack));

        schemaDetail.setVisible(currentSchema != null);
        schemaList.setVisible(currentSchema == null);

        updateTabVisibility();
        tabGroup.setActive(tab);
    }

    public void update(PlayerEntity player, WorkbenchTile tileEntity, int[] availableCapabilities) {
        schemaDetail.updateAvailableCapabilities(availableCapabilities);

        schemaDetail.toggleButton(
                tileEntity.getCurrentSchema().canApplyUpgrade(
                        player,
                        tileEntity.getTargetItemStack(),
                        tileEntity.getMaterials(),
                        tileEntity.getCurrentSlot(),
                        availableCapabilities));
    }

    public void updatePreview(UpgradeSchema schema, String slot, ItemStack itemStack, ItemStack previewStack) {
        schemaDetail.updateMagicCapacity(schema, slot, itemStack, previewStack);
    }

    private void updateSchemaList(PlayerEntity player, WorkbenchTile tileEntity, String selectedSlot) {
        ItemStack targetStack = tileEntity.getTargetItemStack();
        UpgradeSchema[] schemas = ItemUpgradeRegistry.instance.getAvailableSchemas(player, targetStack);
        schemas = Arrays.stream(schemas)
                .filter(upgradeSchema -> upgradeSchema.isApplicableForSlot(selectedSlot, targetStack))
                .sorted(Comparator.comparing(UpgradeSchema::getRarity).thenComparing(UpgradeSchema::getType).thenComparing(UpgradeSchema::getKey))
                .toArray(UpgradeSchema[]::new);
        schemaList.setSchemas(schemas);
    }

    public void keyTyped(char typedChar) {
        tabGroup.keyTyped(typedChar);
    }
}
