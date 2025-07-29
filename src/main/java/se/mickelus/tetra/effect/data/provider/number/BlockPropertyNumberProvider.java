package se.mickelus.tetra.effect.data.provider.number;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.condition.FixedItemEffectCondition;
import se.mickelus.tetra.effect.data.condition.ItemEffectCondition;
import se.mickelus.tetra.effect.data.provider.vector.VectorProvider;

public class BlockPropertyNumberProvider implements NumberProvider {
    VectorProvider position;
    BlockProperty property;
    ItemEffectCondition relative = new FixedItemEffectCondition(false);

    @Override
    public float getValue(ItemEffectContext context) {
        Level level = context.getLevel();
        BlockPos blockPos = position.getBlockPos(context);
        BlockState blockState = level.getBlockState(blockPos);
        return switch (property) {
            case hardness -> blockState.getDestroySpeed(level, blockPos);
            case lightValue -> blockState.getLightBlock(level, blockPos);
            case experienceDrop -> getExpDrop(context, level, blockPos, blockState);
            case comparatorValue -> blockState.getAnalogOutputSignal(level, blockPos);
            case flammability -> blockState.getFlammability(level, blockPos, Direction.UP);
            case friction -> blockState.getFriction(level, blockPos, null);
        };
    }

    private float getExpDrop(ItemEffectContext context, Level level, BlockPos blockPos, BlockState blockState) {
        if (relative.test(context)) {
            return blockState.getExpDrop(level, level.getRandom(), blockPos,
                    EnchantmentHelper.getTagEnchantmentLevel(Enchantments.BLOCK_FORTUNE, context.getUsedItemStack()),
                    EnchantmentHelper.getTagEnchantmentLevel(Enchantments.SILK_TOUCH, context.getUsedItemStack()));
        }

        return blockState.getExpDrop(level, level.getRandom(), blockPos, 0, 0);
    }

    enum BlockProperty {
        hardness,
        lightValue,
        experienceDrop,
        comparatorValue,
        flammability,
        friction
    }
}
