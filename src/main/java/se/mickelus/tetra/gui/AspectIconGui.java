package se.mickelus.tetra.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.GuiTexture;
import se.mickelus.mutil.util.CastOptional;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.data.AspectData;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
public class AspectIconGui extends GuiElement {
    protected List<Component> tooltip;

    public AspectIconGui(int x, int y) {
        super(x, y, 10, 10);

        GuiTexture indicator = new GuiTexture(0, 0, width, height, 176, 0, GuiTextures.workbench);
        addChild(indicator);
    }

    public void update(ItemStack itemStack, ItemModule module) {
        AspectData aspects = module.getAspects(itemStack);
        tooltip = new ArrayList<>();

        if (!aspects.getValues().isEmpty()) {
            tooltip.add(Component.translatable("item.tetra.modular.aspects.header").withStyle(ChatFormatting.GRAY));
            aspects.getLevelMap().forEach((aspect, level) -> {
                String key = "tetra.aspect." + aspect.getKey();
                String levelString = I18n.get("enchantment.level." + level);
                if (I18n.exists(key)) {
                    tooltip.add(Component.literal(I18n.get(key) + " " + levelString));
                } else {
                    tooltip.add(Component.literal(StringUtils.capitalize(aspect.getKey()) + " " + levelString));
                }
            });
        } else {
            tooltip.add(Component.translatable("item.tetra.modular.aspects.empty").withStyle(ChatFormatting.GRAY));
        }
    }

    public void update(ItemStack itemStack, String slot) {
        CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .map(item -> item.getModuleFromSlot(itemStack, slot))
                .ifPresent(module -> update(itemStack, module));
    }

    @Override
    public List<Component> getTooltipLines() {
        if (hasFocus()) {
            return tooltip;
        }
        return super.getTooltipLines();
    }
}
