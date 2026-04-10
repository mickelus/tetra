package se.mickelus.tetra.items.modular.impl.holo.gui.craft.schematic;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import se.mickelus.tetra.compat.forge.registries.ForgeRegistries;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.GuiTexture;
import se.mickelus.tetra.ClientScheduler;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.impl.holo.ModularHolosphereItem;
import se.mickelus.tetra.items.modular.impl.holo.gui.HoloGui;
import se.mickelus.tetra.module.schematic.SchematicRarity;
import se.mickelus.tetra.module.schematic.SchematicType;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
public class HoloMaterialApplicable extends GuiElement {
    private final List<Component> emptyTooltip;
    private final GuiTexture icon;
    private List<Component> tooltip;
    private IModularItem item;
    private ItemStack itemStack;
    private String slot;
    private UpgradeSchematic schematic;

    public HoloMaterialApplicable(int x, int y) {
        super(x, y, 9, 9);

        icon = new GuiTexture(0, 0, 9, 9, 215, 0, GuiTextures.workbench);
        addChild(icon);

        emptyTooltip = Collections.singletonList(Component.translatable("tetra.holo.craft.empty_applicable_materials"));
    }

    @Override
    public List<Component> getTooltipLines() {
        if (hasFocus()) {
            return tooltip;
        }
        return null;
    }

    public void update(Level level, BlockPos pos, WorkbenchTile blockEntity, ItemStack itemStack, String slot, UpgradeSchematic schematic,
            Player playerEntity) {
        this.item = null;
        this.itemStack = null;
        this.slot = null;
        this.schematic = null;

        tooltip = new ArrayList<>();

        String[] materials = schematic.getApplicableMaterials();
        if (materials != null && materials.length > 0) {
            String materialsString = Arrays.stream(materials)
                    .map(mat -> {
                        if (mat.startsWith("#")) {
                            return I18n.get("tetra.variant_category." + mat.substring(1) + ".label");
                        } else if (mat.startsWith("!")) {
                            return I18n.get("tetra.material." + mat.substring(1));
                        }
                        return Optional.ofNullable(ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(mat)))
                                .map(Item::getDescription)
                                .map(Component::getString)
                                .orElse(mat);
                    })
                    .collect(Collectors.joining(", "));

            tooltip.add(Component.translatable("tetra.holo.craft.applicable_materials"));
            tooltip.add(Component.literal(materialsString).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal(""));

            ItemStack holosphereStack = ModularHolosphereItem.findHolosphere(playerEntity, level, pos);

            if ((schematic.getType() != SchematicType.major && schematic.getType() != SchematicType.minor)
                    || schematic.getRarity() != SchematicRarity.basic) {
                tooltip.add(Component.translatable("tetra.holo.craft.holosphere_shortcut_disabled"));
                tooltip.add(Component.translatable("tetra.holo.craft.holosphere_shortcut_unavailable").withStyle(ChatFormatting.DARK_GRAY));
            } else if (holosphereStack.isEmpty() || !(itemStack.getItem() instanceof IModularItem)) {
                tooltip.add(Component.translatable("tetra.holo.craft.holosphere_shortcut_disabled"));
                tooltip.add(Component.translatable("tetra.holo.craft.holosphere_shortcut_missing").withStyle(ChatFormatting.DARK_GRAY));
            } else {
                tooltip.add(Component.translatable("tetra.holo.craft.holosphere_shortcut"));
                this.item = (IModularItem) itemStack.getItem();
                this.itemStack = itemStack;
                this.slot = slot;
                this.schematic = schematic;
            }
        } else {
            tooltip = emptyTooltip;
        }
    }


    @Override
    public boolean onMouseClick(int x, int y, int button) {
        if (hasFocus() && item != null) {
            Screen currentScreen = Minecraft.getInstance().screen;
            HoloGui gui = HoloGui.getInstance();

            Minecraft.getInstance().setScreen(gui);
            gui.openSchematic(item, itemStack, slot, schematic, () -> ClientScheduler.schedule(0, () -> Minecraft.getInstance().setScreen(currentScreen)));
            return true;
        }

        return false;
    }
}
