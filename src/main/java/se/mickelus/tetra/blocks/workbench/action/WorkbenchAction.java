package se.mickelus.tetra.blocks.workbench.action;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.ItemAbility;
import org.jetbrains.annotations.Nullable;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;

import java.util.Collection;
import java.util.Map;

public interface WorkbenchAction {

    String getKey();

    boolean canPerformOn(@Nullable Player player, WorkbenchTile tile, ItemStack itemStack);

    Collection<ItemAbility> getRequiredItemAbilities(ItemStack itemStack);

    int getRequiredToolLevel(ItemStack itemStack, ItemAbility toolAction);

    Map<ItemAbility, Integer> getRequiredTools(ItemStack itemStack);

    void perform(Player player, ItemStack itemStack, WorkbenchTile workbench);

    default boolean allowInWorldInteraction() {
        return false;
    }
}
