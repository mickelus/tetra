package se.mickelus.tetra.craftingeffect.outcome;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ToolAction;
import se.mickelus.tetra.effect.SculkTaintEffect;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@ParametersAreNonnullByDefault
public class SpawnSculkOutcome implements CraftingEffectOutcome {

    int severity = 5;
    double chance = 1;

    @Override
    public boolean apply(ResourceLocation[] unlockedEffects, ItemStack upgradedStack, String slot, boolean isReplacing, Player player,
            ItemStack[] preMaterials, Map<ToolAction, Integer> tools, Level world, UpgradeSchematic schematic, BlockPos origin, BlockState blockState,
            boolean consumeResources, ItemStack[] postMaterials) {
        if (consumeResources && !world.isClientSide() && world.random.nextDouble() < chance) {
            Optional<BlockPos> catalystOrigin = BlockPos.betweenClosedStream(new AABB(-2, 0, -2, 2, 5, 2))
                    .map(origin::offset)
                    .filter(pos -> world.getBlockState(pos).is(Blocks.SCULK_CATALYST))
                    .map(pos -> findVeinOrigin(world, pos))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findAny();
            if (catalystOrigin.isPresent()) {
                SculkTaintEffect.startSpread((ServerLevel) world, catalystOrigin.get(), severity);
                return true;
            }

            Optional<BlockPos> workbenchOrigin = findVeinOrigin(world, origin);
            if (workbenchOrigin.isPresent()) {
                SculkTaintEffect.startSpread((ServerLevel) world, workbenchOrigin.get(), severity);
                return true;
            }
        }
        return false;
    }

    private Optional<BlockPos> findVeinOrigin(Level world, BlockPos origin) {
        return Direction.allShuffled(world.getRandom()).stream()
                .map(origin::relative)
                .filter(world::isEmptyBlock)
                .filter(pos ->
                        Arrays.stream(Direction.values())
                                .anyMatch(direction ->
                                        MultifaceBlock.canAttachTo(world, direction, pos.relative(direction), world.getBlockState(pos.relative(direction))) && !world.getBlockState(pos.relative(direction)).is(Blocks.SCULK_CATALYST)))
                .findAny();
    }
}
