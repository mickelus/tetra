package se.mickelus.tetra.items.toolbelt;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import se.mickelus.tetra.items.toolbelt.gui.GuiToolbelt;
import se.mickelus.tetra.network.TetraGuiHandler;

public class GuiHandlerToolbelt implements TetraGuiHandler {
    @Override
    public Object getServerGuiElement(PlayerEntity player, World world, int hand, int y, int z) {
        return new ContainerToolbelt(player.inventory, getItem(player, hand), player);
    }

    @Override
    public Object getClientGuiElement(PlayerEntity player, World world, int hand, int y, int z) {
        return new GuiToolbelt(new ContainerToolbelt(player.inventory, getItem(player, hand), player));
    }

    private ItemStack getItem(PlayerEntity player, int handOrdinal) {
        EnumHand hand = EnumHand.values()[handOrdinal];
        ItemStack itemStack = player.getHeldItemOffhand();

        if (hand == EnumHand.MAIN_HAND) {
            itemStack = player.getHeldItemMainhand();
        }

        return itemStack;
    }
}
