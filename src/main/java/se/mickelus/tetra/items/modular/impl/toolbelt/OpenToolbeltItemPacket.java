package se.mickelus.tetra.items.modular.impl.toolbelt;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkHooks;
import se.mickelus.tetra.items.modular.impl.toolbelt.inventory.ToolbeltSlotType;
import se.mickelus.tetra.network.AbstractPacket;

public class OpenToolbeltItemPacket extends AbstractPacket {

    public OpenToolbeltItemPacket() { }

    @Override
    public void toBytes(PacketBuffer buffer) {
    }

    @Override
    public void fromBytes(PacketBuffer buffer) {
    }

    @Override
    public void handle(PlayerEntity player) {
        ItemStack itemStack = ToolbeltHelper.findToolbelt(player);
        if (!itemStack.isEmpty()) {
            NetworkHooks.openGui((ServerPlayerEntity) player, (INamedContainerProvider) itemStack.getItem());
        }
    }
}
