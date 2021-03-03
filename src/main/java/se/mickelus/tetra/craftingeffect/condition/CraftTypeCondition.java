package se.mickelus.tetra.craftingeffect.condition;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

import java.util.Map;

public class CraftTypeCondition implements CraftingEffectCondition {
    CraftType craft;

    @Override
    public boolean test(ResourceLocation[] unlocks, ItemStack upgradedStack, String slot, boolean isReplacing, PlayerEntity player,
            ItemStack[] materials, Map<ToolType, Integer> tools, World world, BlockPos pos, BlockState blockState) {
        switch (craft) {
            case module:
                return isReplacing;
            case improvement:
                return !isReplacing && !isEnchantment(materials);
            case enchantment:
                return !isReplacing && isEnchantment(materials);
            case repair:
        }
        return false;
    }

    private boolean isEnchantment(ItemStack[] materials) {
        return materials.length == 1 && !materials[0].isEmpty() && Items.ENCHANTED_BOOK.equals(materials[0].getItem());
    }

    static enum CraftType {
        module,
        improvement,
        enchantment,
        repair;
    }
}
