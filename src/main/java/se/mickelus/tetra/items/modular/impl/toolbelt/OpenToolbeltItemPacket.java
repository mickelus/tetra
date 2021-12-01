package se.mickelus.tetra.items.modular.impl.toolbelt;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkHooks;
import se.mickelus.tetra.network.AbstractPacket;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
public class OpenToolbeltItemPacket extends AbstractPacket {

    public OpenToolbeltItemPacket() { }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
    }

    @Override
    public void fromBytes(FriendlyByteBuf buffer) {
    }

    @Override
    public void handle(Player player) {
        ItemStack itemStack = ToolbeltHelper.findToolbelt(player);
        if (!itemStack.isEmpty()) {
            NetworkHooks.openGui((ServerPlayer) player, (MenuProvider) itemStack.getItem());
        }
    }
}
