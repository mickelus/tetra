package se.mickelus.tetra.craftingeffect.condition;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
@ParametersAreNonnullByDefault
public class CraftTypeCondition implements CraftingEffectCondition {
    CraftType craft;

    @Override
    public boolean test(ResourceLocation[] unlocks, ItemStack upgradedStack, String slot, boolean isReplacing, Player player,
            ItemStack[] materials, Map<ToolAction, Integer> tools, Level world, BlockPos pos, BlockState blockState) {
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
