package se.mickelus.tetra.module.schematic;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import se.mickelus.mutil.util.CastOptional;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;

import javax.annotation.Nullable;

public class CraftingContext {
    @Nullable
    public final Level world;
    @Nullable
    public final BlockPos pos;
    @Nullable
    public final BlockState blockState;
    @Nullable
    public final Player player;

    public final ItemStack targetStack;

    public final String slot;
    public final ResourceLocation[] unlocks;

    @Nullable
    public final ItemModule targetModule;
    @Nullable
    public final ItemModuleMajor targetMajorModule;

    public CraftingContext(@Nullable Level world, @Nullable BlockPos pos, @Nullable BlockState blockState, @Nullable Player player, ItemStack targetStack, String slot, ResourceLocation[] unlocks) {
        this.world = world;
        this.pos = pos;
        this.blockState = blockState;
        this.player = player;
        this.targetStack = targetStack;
        this.slot = slot;
        this.unlocks = unlocks;

        targetModule = CastOptional.cast(targetStack.getItem(), IModularItem.class)
                .map(item -> item.getModuleFromSlot(targetStack, slot))
                .orElse(null);

        if (targetModule instanceof ItemModuleMajor targetMajorModule) {
            this.targetMajorModule = targetMajorModule;
        } else {
            this.targetMajorModule = null;
        }
    }
}
