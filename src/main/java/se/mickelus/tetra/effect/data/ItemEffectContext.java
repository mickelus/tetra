package se.mickelus.tetra.effect.data;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ItemEffectContext {
    private LivingEntity usingEntity;
    private ItemStack usedItemStack;
    private Level level;
    private @Nullable LivingEntity targetEntity;
    private @Nullable BlockPos targetPos;
    private @Nullable BlockState targetState;
    private Map<String, Float> data;

    public ItemEffectContext(
            LivingEntity usingEntity,
            ItemStack usedItemStack,
            Level level,
            @Nullable LivingEntity targetEntity,
            @Nullable BlockPos targetPos,
            @Nullable BlockState targetState) {
        this.usingEntity = usingEntity;
        this.usedItemStack = usedItemStack;
        this.level = level;
        this.targetEntity = targetEntity;
        this.targetPos = targetPos;
        this.targetState = targetState;

        this.data = Collections.emptyMap();
    }

    public ItemEffectContext(LivingEntity usingEntity, ItemStack usedItemStack, Level level) {
        this(usingEntity, usedItemStack, level, null, null, null);
    }

    public ItemEffectContext(LivingEntity usingEntity, ItemStack usedItemStack, Level level, LivingEntity targetEntity) {
        this(usingEntity, usedItemStack, level, targetEntity, null, null);
    }

    public ItemEffectContext(LivingEntity usingEntity, ItemStack usedItemStack, Level level, BlockPos targetPos, BlockState targetState) {
        this(usingEntity, usedItemStack, level, null, targetPos, targetState);
    }

    public ItemEffectContext copy() {
        return new ItemEffectContext(usingEntity, usedItemStack, level, targetEntity, targetPos, targetState);
    }

    public ItemEffectContext withData(Map<String, Float> data) {
        ItemEffectContext copy = copy();
        copy.data = data;
        return copy;
    }

    public ItemEffectContext withMergedData(Map<String, Float> data) {
        ItemEffectContext copy = copy();
        copy.data = Stream.of(copy.data, data)
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b));
        return copy;
    }

    public LivingEntity getUsingEntity() {
        return usingEntity;
    }

    public ItemStack getUsedItemStack() {
        return usedItemStack;
    }

    public Level getLevel() {
        return level;
    }

    @Nullable
    public LivingEntity getTargetEntity() {
        return targetEntity;
    }

    @Nullable
    public BlockPos getTargetPos() {
        return targetPos;
    }

    @Nullable
    public BlockState getTargetState() {
        return targetState;
    }

    public Map<String, Float> getData() {
        return data;
    }
}
