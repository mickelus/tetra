package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import se.mickelus.mgui.gui.GuiButton;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiRect;
import se.mickelus.mgui.gui.GuiTexture;
import se.mickelus.mgui.gui.animation.AnimationChain;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.blocks.forged.container.ForgedContainerTile;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;
import se.mickelus.tetra.gui.VerticalTabGroupGui;
import se.mickelus.tetra.properties.PropertyHelper;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.SchematicRegistry;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;
import se.mickelus.tetra.util.CastOptional;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class GuiSlotDetail extends GuiElement {
    private static final char[] keybindings = new char[] { 'a', 's', 'd'};
    private static final String[] labels = new String[] {
            "tetra.workbench.slot_detail.details_tab",
            "tetra.workbench.slot_detail.craft_tab",
            "tetra.workbench.slot_detail.tweak_tab"
    };

    private int tab = 1;

    private VerticalTabGroupGui tabGroup;

    private GuiModuleDetails moduleDetails;

    private GuiElement schematicGroup;
    private GuiSchematicList schematicList;
    private GuiSchematicDetail schematicDetail;

    private GuiTweakControls tweakControls;

    private Consumer<UpgradeSchematic> selectSchematicHandler;

    private final AnimationChain slotTransition;

    public GuiSlotDetail(int x, int y, Consumer<UpgradeSchematic> selectSchematicHandler, Runnable closeHandler,
            Runnable craftHandler, Consumer<Map<String, Integer>> previewTweak, Consumer<Map<String, Integer>> applyTweak) {
        super(x, y, 224, 67);

        this.selectSchematicHandler = selectSchematicHandler;

        addChild(new GuiTexture(0, 0, width, height, 0, 68, GuiTextures.workbench));

        addChild(new GuiRect(1, 6, 2, 49, 0));

        tabGroup = new VerticalTabGroupGui(1, 6, this::changeTab, GuiTextures.workbench, 128, 32,
                IntStream.range(0, 3)
                        .mapToObj(i -> I18n.format(labels[i]))
                        .toArray(String[]::new));
        tabGroup.setHasContent(1, true);
        addChild(tabGroup);

        moduleDetails = new GuiModuleDetails(0, 0);
        addChild(moduleDetails);

        schematicGroup = new GuiElement(0, 0, width, height);
        addChild(schematicGroup);

        schematicList = new GuiSchematicList(0, 0, selectSchematicHandler);
        schematicList.setVisible(false);
        schematicGroup.addChild(schematicList);

        schematicDetail = new GuiSchematicDetail(0, 0, () -> selectSchematicHandler.accept(null), craftHandler);
        schematicDetail.setVisible(false);
        schematicGroup.addChild(schematicDetail);


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
        selectSchematicHandler.accept(null);
        tab = index;

        updateTabVisibility();

        slotTransition.stop();
        slotTransition.start();
    }

    private void updateTabVisibility() {
        moduleDetails.setVisible(tab == 0);
        schematicGroup.setVisible(tab == 1);
        tweakControls.setVisible(tab == 2);
    }

    public void onTileEntityChange(PlayerEntity player, WorkbenchTile tileEntity, ItemStack itemStack, String selectedSlot, UpgradeSchematic currentSchematic) {
        ItemModule module = CastOptional.cast(itemStack.getItem(), ModularItem.class)
                .map(item -> item.getModuleFromSlot(itemStack, selectedSlot))
                .orElse(null);

        if (currentSchematic == null) {
            updateSchematicList(player, tileEntity, selectedSlot);
        } else {
            World world = tileEntity.getWorld();
            BlockPos pos = tileEntity.getPos();
            Map<ToolType, Integer> availableTools = PropertyHelper.getCombinedToolLevels(player, world, pos, world.getBlockState(pos));
            ItemStack[] materials = tileEntity.getMaterials();


            schematicDetail.update(currentSchematic, itemStack, selectedSlot, materials, availableTools,
                    player.isCreative() ? Integer.MAX_VALUE : player.experienceLevel);
            schematicDetail.updateMagicCapacity(currentSchematic, selectedSlot, itemStack,
                    currentSchematic.applyUpgrade(itemStack.copy(), materials, false, selectedSlot, player));
            schematicDetail.toggleButton(currentSchematic.canApplyUpgrade(player, itemStack, materials, selectedSlot, availableTools));

            tab = 1;
        }

        moduleDetails.update(module, itemStack);
        tabGroup.setHasContent(0, module != null);

        tweakControls.update(module, itemStack);
        tabGroup.setHasContent(2, module != null && module.isTweakable(itemStack));

        schematicDetail.setVisible(currentSchematic != null);
        schematicList.setVisible(currentSchematic == null);

        updateTabVisibility();
        tabGroup.setActive(tab);
    }

    public void update(PlayerEntity player, WorkbenchTile tileEntity, Map<ToolType, Integer> availableTools) {
        schematicDetail.updateAvailableTools(availableTools);

        schematicDetail.toggleButton(
                tileEntity.getCurrentSchematic().canApplyUpgrade(
                        player,
                        tileEntity.getTargetItemStack(),
                        tileEntity.getMaterials(),
                        tileEntity.getCurrentSlot(),
                        availableTools));
    }

    public void updatePreview(UpgradeSchematic schematic, String slot, ItemStack itemStack, ItemStack previewStack) {
        schematicDetail.updateMagicCapacity(schematic, slot, itemStack, previewStack);
    }

    private void updateSchematicList(PlayerEntity player, WorkbenchTile tileEntity, String selectedSlot) {
        ItemStack targetStack = tileEntity.getTargetItemStack();
        UpgradeSchematic[] schematics = SchematicRegistry.getAvailableSchematics(player, targetStack);
        schematics = Arrays.stream(schematics)
                .filter(upgradeSchematic -> upgradeSchematic.isApplicableForSlot(selectedSlot, targetStack))
                .sorted(Comparator.comparing(UpgradeSchematic::getRarity).thenComparing(UpgradeSchematic::getType).thenComparing(UpgradeSchematic::getKey))
                .toArray(UpgradeSchematic[]::new);
        schematicList.setSchematics(schematics);
    }

    public void keyTyped(char typedChar) {
        tabGroup.keyTyped(typedChar);
    }
}
