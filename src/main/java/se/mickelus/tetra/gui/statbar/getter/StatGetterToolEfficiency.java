package se.mickelus.tetra.gui.statbar.getter;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ToolType;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.util.CastOptional;

public class StatGetterToolEfficiency implements IStatGetter {

    private ToolType toolType;

    public StatGetterToolEfficiency(ToolType effect) {
        this.toolType = effect;
    }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack) {
        return CastOptional.cast(itemStack.getItem(), ModularItem.class)
                .map(item -> item.getToolEfficiency(itemStack, toolType))
                .orElse(0f);
    }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack, String slot) {
        return CastOptional.cast(itemStack.getItem(), ModularItem.class)
                .map(item -> item.getModuleFromSlot(itemStack, slot))
                .map(module -> module.getToolEfficiency(itemStack, toolType))
                .orElse(0f);
    }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack, String slot, String improvement) {
        return CastOptional.cast(itemStack.getItem(), ModularItem.class)
                .flatMap(item -> CastOptional.cast(item.getModuleFromSlot(itemStack, slot), ItemModuleMajor.class))
                .map(module -> module.getImprovement(itemStack, improvement))
                .map(improvementData -> improvementData.tools)
                .map(data -> data.getEfficiency(toolType))
                .orElse(0f);
    }
}
