package se.mickelus.tetra.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.GuiTexture;
import se.mickelus.mutil.util.CastOptional;
import se.mickelus.tetra.Tooltips;
import se.mickelus.tetra.aspect.ItemAspect;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.data.AspectData;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
public class AspectIconGui extends GuiElement {
    protected List<Component> tooltip;

    protected int detailOffset;
    protected List<Component> detailTooltip;
    protected List<ItemAspect> aspects;

    public AspectIconGui(int x, int y) {
        super(x, y, 10, 10);

        GuiTexture indicator = new GuiTexture(0, 0, width, height, 208, 32, GuiTextures.workbench);
        addChild(indicator);
    }

    public void update(ItemStack itemStack, ItemModule module) {
        AspectData aspects = module.getAspects(itemStack);
        tooltip = new ArrayList<>();

        detailOffset = 0;

        if (aspects != null && !aspects.getValues().isEmpty()) {
            tooltip.add(Component.translatable("tetra.modular.aspects.header").withStyle(ChatFormatting.GRAY));
            aspects.getLevelMap().forEach((aspect, level) -> {
                Component levelString = Component.translatable("enchantment.level." + level);
                tooltip.add(Component.literal("  ")
                        .append(getAspectLabel(aspect.getKey()))
                        .append(Component.literal(" "))
                        .append(levelString));
            });

            tooltip.add(Component.literal(" "));
            tooltip.add(Tooltips.expand);

            this.aspects = new ArrayList<>(aspects.getValues());
            updateDetailTooltip();
        } else {
            tooltip.add(Component.translatable("tetra.modular.aspects.empty").withStyle(ChatFormatting.GRAY));
            detailTooltip = null;
        }
    }

    public void update(ItemStack itemStack, String slot) {
        CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .map(item -> item.getModuleFromSlot(itemStack, slot))
                .ifPresent(module -> update(itemStack, module));
    }

    public void updateDetailTooltip() {
        ItemAspect aspect = aspects.get(detailOffset);

        detailTooltip = new ArrayList<>();
        detailTooltip.add(Component.translatable("tetra.modular.aspects.detail_header", detailOffset + 1, aspects.size()).withStyle(ChatFormatting.GRAY));
        detailTooltip.add(Component.literal(" "));
        detailTooltip.add(getAspectLabel(aspect.getKey()).withStyle(ChatFormatting.YELLOW));
        detailTooltip.add(getAspectDescription(aspect.getKey()));
        detailTooltip.add(Component.literal(" "));
        detailTooltip.add(Tooltips.expanded);
        if (aspects.size() > 1) {
            detailTooltip.add(Component.translatable("tetra.modular.aspects.scroll_tooltip"));
        }
    }

    private MutableComponent getAspectLabel(String key) {
        String localizationKey = "tetra.aspect." + key;
        if (I18n.exists(localizationKey)) {
            return Component.translatable(localizationKey);
        }
        return Component.literal(StringUtils.capitalize(key.replace("_", " ")));
    }

    private MutableComponent getAspectDescription(String key) {
        String localizationKey = "tetra.aspect." + key + ".description";
        if (I18n.exists(localizationKey)) {
            return Component.translatable(localizationKey);
        }
        return Component.translatable("tetra.modular.aspects.missing_description").withStyle(ChatFormatting.GRAY);
    }

    @Override
    public List<Component> getTooltipLines() {
        if (hasFocus()) {
            if (Screen.hasShiftDown() && !aspects.isEmpty()) {
                return detailTooltip;
            }
            return tooltip;
        }
        return super.getTooltipLines();
    }

    @Override
    public boolean onMouseScroll(double mouseX, double mouseY, double distance) {
        if (hasFocus()) {
            if (aspects.size() > 1) {
                detailOffset = Mth.clamp(detailOffset + (int) Math.signum(-distance), 0, aspects.size() - 1);
                updateDetailTooltip();
            }
            return true;
        }

        return false;
    }
}
