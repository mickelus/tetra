package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ToolAction;
import se.mickelus.mutil.gui.GuiButton;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.GuiRect;
import se.mickelus.mutil.gui.GuiTexture;
import se.mickelus.mutil.gui.animation.AnimationChain;
import se.mickelus.mutil.gui.animation.Applier;
import se.mickelus.mutil.gui.animation.KeyframeAnimation;
import se.mickelus.mutil.util.CastOptional;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.gui.VerticalTabGroupGui;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.SchematicRegistry;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;
import se.mickelus.tetra.properties.PropertyHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.IntStream;

@ParametersAreNonnullByDefault
public class GuiSlotDetail extends GuiElement {
    private static final char[] keybindings = new char[]{'a', 's', 'd'};
    private static final String[] labels = new String[]{
            "tetra.workbench.slot_detail.details_tab",
            "tetra.workbench.slot_detail.craft_tab",
            "tetra.workbench.slot_detail.tweak_tab"
    };
    private final AnimationChain slotTransition;
    private final VerticalTabGroupGui tabGroup;
    private final GuiModuleDetails moduleDetails;
    private final GuiElement schematicGroup;
    private final GuiSchematicList schematicList;
    private final GuiSchematicDetail schematicDetail;
    private final GuiTweakControls tweakControls;
    private final Consumer<UpgradeSchematic> selectSchematicHandler;
    private int tab = 1;

    public GuiSlotDetail(int x, int y, Consumer<UpgradeSchematic> selectSchematicHandler, Runnable closeHandler,
            Runnable craftHandler, Consumer<Map<String, Integer>> previewTweak, Consumer<Map<String, Integer>> applyTweak) {
        super(x, y, 224, 67);

        this.selectSchematicHandler = selectSchematicHandler;

        addChild(new GuiTexture(0, 0, width, height, 0, 68, GuiTextures.workbench));

        addChild(new GuiRect(1, 6, 2, 49, 0));

        tabGroup = new VerticalTabGroupGui(1, 6, this::changeTab, GuiTextures.workbench, 128, 32,
                IntStream.range(0, 3)
                        .mapToObj(i -> I18n.get(labels[i]))
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

    public void onTileEntityChange(Player player, WorkbenchTile tileEntity, ItemStack itemStack, String selectedSlot, UpgradeSchematic currentSchematic) {
        ItemModule module = CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .map(item -> item.getModuleFromSlot(itemStack, selectedSlot))
                .orElse(null);

        if (currentSchematic == null) {
            updateSchematicList(player, tileEntity, selectedSlot);
        } else {
            Level world = tileEntity.getLevel();
            BlockPos pos = tileEntity.getBlockPos();
            Map<ToolAction, Integer> availableTools = PropertyHelper.getCombinedToolLevels(player, world, pos, world.getBlockState(pos));
            ItemStack[] materials = tileEntity.getMaterials();

            ItemStack previewStack = currentSchematic.applyUpgrade(itemStack.copy(), materials, false, selectedSlot, player);

            schematicDetail.update(currentSchematic, itemStack, selectedSlot, materials, availableTools, player);
            schematicDetail.updateMagicCapacity(currentSchematic, selectedSlot, itemStack, previewStack);
            schematicDetail.updateButton(currentSchematic, player, itemStack, previewStack, materials, selectedSlot, availableTools);

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

    public void update(Player player, WorkbenchTile tileEntity, Map<ToolAction, Integer> availableTools) {
        schematicDetail.updateAvailableTools(availableTools);

        ItemStack currentStack = tileEntity.getTargetItemStack().copy();
        UpgradeSchematic currentSchematic = tileEntity.getCurrentSchematic();
        ItemStack previewStack = currentSchematic.applyUpgrade(currentStack, tileEntity.getMaterials(), false, tileEntity.getCurrentSlot(), player);
        schematicDetail.updateButton(tileEntity.getCurrentSchematic(), player, currentStack, previewStack, tileEntity.getMaterials(),
                tileEntity.getCurrentSlot(), availableTools);
    }

    public void updatePreview(UpgradeSchematic schematic, String slot, ItemStack itemStack, ItemStack previewStack) {
        schematicDetail.updateMagicCapacity(schematic, slot, itemStack, previewStack);
    }

    private void updateSchematicList(Player player, WorkbenchTile tileEntity, String selectedSlot) {
        ItemStack targetStack = tileEntity.getTargetItemStack();
        UpgradeSchematic[] schematics = SchematicRegistry.getAvailableSchematics(player, tileEntity, targetStack);
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
