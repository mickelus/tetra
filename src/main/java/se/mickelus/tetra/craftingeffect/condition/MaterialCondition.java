package se.mickelus.tetra.craftingeffect.condition;

import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolType;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
@ParametersAreNonnullByDefault
public class MaterialCondition implements CraftingEffectCondition {
    ItemPredicate material;

    @Override
    public boolean test(ResourceLocation[] unlocks, ItemStack upgradedStack, String slot, boolean isReplacing, Player player,
            ItemStack[] materials, Map<ToolType, Integer> tools, Level world, BlockPos pos, BlockState blockState) {
        for (ItemStack material: materials) {
            if (this.material.matches(material)) {
                return true;
            }
        }
        return false;
    }
}
