package se.mickelus.tetra.craftingeffect.outcome;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbility;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import java.util.Map;

public class ExplosionOutcome implements CraftingEffectOutcome {
    float chance = 1;
    float radius = 4;
    int randomOriginDistance = 0;
    Level.ExplosionInteraction type = Level.ExplosionInteraction.NONE;

    @Override
    public boolean apply(final ResourceLocation[] unlockedEffects, final ItemStack upgradedStack, final String slot, final boolean isReplacing,
            final Player player, final ItemStack[] preMaterials, final Map<ItemAbility, Integer> tools, final Level world,
            final UpgradeSchematic schematic, final BlockPos pos, final BlockState blockState, final boolean consumeResources,
            final ItemStack[] postMaterials, float severity) {
        if (consumeResources && !world.isClientSide()) {
            BlockPos origin = pos.above();
            if (randomOriginDistance > 0) {
                origin = getRandomBlockPos(world.random, randomOriginDistance).offset(origin);
            }

            if (world.random.nextDouble() < chance) {
                world.explode(player, origin.getX(), origin.getY(), origin.getZ(), radius, type);
                return true;
            }
        }
        return false;
    }

    private static BlockPos getRandomBlockPos(RandomSource random, int radius) {
        return new BlockPos(random.nextIntBetweenInclusive(-radius, radius),
                random.nextIntBetweenInclusive(-radius, radius),
                random.nextIntBetweenInclusive(-radius, radius));
    }
}
