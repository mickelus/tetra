package se.mickelus.tetra.items.toolbelt;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandlerToolbelt implements IGuiHandler {

    public static final int GUI_TOOLBELT_ID = 0;

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int hand, int y, int z) {
        if (GUI_TOOLBELT_ID == ID) {
            return new ContainerToolbelt(player.inventory, getItem(player, hand), player);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int hand, int y, int z) {
        if (GUI_TOOLBELT_ID == ID) {
            return new GuiToolbelt(new ContainerToolbelt(player.inventory, getItem(player, hand), player));
        }
        return null;
    }

    private ItemStack getItem(EntityPlayer player, int handOrdinal) {
        EnumHand hand = EnumHand.values()[handOrdinal];
        ItemStack itemStack = player.getHeldItemOffhand();

        if (hand == EnumHand.MAIN_HAND) {
            itemStack = player.getHeldItemMainhand();
        }

        return itemStack;
    }
}
