package se.mickelus.tetra.craftingeffect.outcome;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import se.mickelus.tetra.effect.CombustingEffect;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import java.util.Map;

public class SpawnFireOutcome implements CraftingEffectOutcome {
    boolean soulfire = false;
    int radius = 5;
    int count = 8;
    float chance = 1;

    @Override
    public boolean apply(ResourceLocation[] unlockedEffects, ItemStack upgradedStack, String slot, boolean isReplacing, Player player,
            ItemStack[] preMaterials, Map<ToolAction, Integer> tools, Level world, UpgradeSchematic schematic, BlockPos pos, BlockState blockState,
            boolean consumeResources, ItemStack[] postMaterials) {
        if (consumeResources && !world.isClientSide() && world.getRandom().nextFloat() < chance) {
            CombustingEffect.igniteBlocksAround(world, pos, radius, count, false, soulfire);
        }
        return true;
    }
}
