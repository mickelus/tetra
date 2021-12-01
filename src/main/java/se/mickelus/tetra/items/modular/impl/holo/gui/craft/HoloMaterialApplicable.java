package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import com.google.common.collect.ImmutableList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiTexture;
import se.mickelus.tetra.ClientScheduler;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.impl.holo.ModularHolosphereItem;
import se.mickelus.tetra.items.modular.impl.holo.gui.HoloGui;
import se.mickelus.tetra.module.schematic.SchematicRarity;
import se.mickelus.tetra.module.schematic.SchematicType;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class HoloMaterialApplicable extends GuiElement {
    private final List<String> emptyTooltip = Collections.singletonList(I18n.get("tetra.holo.craft.empty_applicable_materials"));
    private List<String> tooltip;

    private GuiTexture icon;


    private IModularItem item;
    private String slot;
    private UpgradeSchematic schematic;

    public HoloMaterialApplicable(int x, int y) {
        super(x, y, 9, 9);

        icon = new GuiTexture(0, 0, 9, 9, 215, 0, GuiTextures.workbench);
        addChild(icon);
    }

    @Override
    public List<String> getTooltipLines() {
        if (hasFocus()) {
            return tooltip;
        }
        return null;
    }

    public void update(ItemStack itemStack, String slot, UpgradeSchematic schematic, Player playerEntity) {
        this.item = null;
        this.slot = null;
        this.schematic = null;

        ImmutableList.Builder<String> tooltipBuilder = new ImmutableList.Builder<>();

        String[] materials = schematic.getApplicableMaterials();
        if (materials != null && materials.length > 0) {
            String materialsString = Arrays.stream(materials)
                    .map(mat -> {
                        if (mat.startsWith("#")) {
                            return I18n.get("tetra.variant_category." + mat.substring(1) + ".label");
                        } else if (mat.startsWith("!")) {
                            return I18n.get("tetra.material." + mat.substring(1));
                        }
                        return Optional.ofNullable(ForgeRegistries.ITEMS.getValue(new ResourceLocation(mat)))
                                .map(Item::getDescription)
                                .map(Component::getString)
                                .orElse(mat);
                    })
                    .collect(Collectors.joining(", "));

            tooltipBuilder.add(I18n.get("tetra.holo.craft.applicable_materials"), ChatFormatting.GRAY + materialsString);

            tooltipBuilder.add(" ");

            if ((schematic.getType() != SchematicType.major && schematic.getType() != SchematicType.minor)
                    || schematic.getRarity() != SchematicRarity.basic) {
                tooltipBuilder.add(I18n.get("tetra.holo.craft.holosphere_shortcut_disabled"));
                tooltipBuilder.add(ChatFormatting.DARK_GRAY + I18n.get("tetra.holo.craft.holosphere_shortcut_unavailable"));
            } else if (ModularHolosphereItem.findHolosphere(playerEntity).isEmpty() || !(itemStack.getItem() instanceof IModularItem)) {
                tooltipBuilder.add(I18n.get("tetra.holo.craft.holosphere_shortcut_disabled"));
                tooltipBuilder.add(ChatFormatting.DARK_GRAY + I18n.get("tetra.holo.craft.holosphere_shortcut_missing"));
            } else {
                tooltipBuilder.add(I18n.get("tetra.holo.craft.holosphere_shortcut"));
                this.item = (IModularItem) itemStack.getItem();
                this.slot = slot;
                this.schematic = schematic;
            }

            tooltip = tooltipBuilder.build();
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
            gui.openSchematic(item, slot, schematic, () -> ClientScheduler.schedule(0, () -> Minecraft.getInstance().setScreen(currentScreen)));
            return true;
        }

        return false;
    }
}
