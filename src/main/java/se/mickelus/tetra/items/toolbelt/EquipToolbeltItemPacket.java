package se.mickelus.tetra.items.toolbelt;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import se.mickelus.tetra.network.AbstractPacket;

public class EquipToolbeltItemPacket extends AbstractPacket {

    private int toolbeltItemIndex;

    public EquipToolbeltItemPacket() { }

    public EquipToolbeltItemPacket(int toolbeltSlot) {
        this.toolbeltItemIndex = toolbeltSlot;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
        buffer.writeInt(toolbeltItemIndex);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
        toolbeltItemIndex = buffer.readInt();
    }

    @Override
    public void handleClientSide(EntityPlayer player) {
    }

    @Override
    public void handleServerSide(EntityPlayer player) {
        if (toolbeltItemIndex > -1) {
            UtilToolbelt.equipItemFromToolbelt(player, toolbeltItemIndex, EnumHand.OFF_HAND);
        } else {
            UtilToolbelt.storeItemInToolbelt(player);
        }
    }
}
