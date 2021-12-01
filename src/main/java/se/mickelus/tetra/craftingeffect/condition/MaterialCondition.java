package se.mickelus.tetra.craftingeffect.condition;

import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

import java.util.Arrays;
import java.util.Map;

public class MaterialCondition implements CraftingEffectCondition {
    ItemPredicate material;

    @Override
    public boolean test(ResourceLocation[] unlocks, ItemStack upgradedStack, String slot, boolean isReplacing, PlayerEntity player,
            ItemStack[] materials, Map<ToolType, Integer> tools, World world, BlockPos pos, BlockState blockState) {
        for (ItemStack material: materials) {
            if (this.material.matches(material)) {
                return true;
            }
        }
        return false;
    }
}
