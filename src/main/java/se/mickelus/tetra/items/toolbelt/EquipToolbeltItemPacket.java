package se.mickelus.tetra.items.toolbelt;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import se.mickelus.tetra.items.toolbelt.inventory.ToolbeltSlotType;
import se.mickelus.tetra.network.AbstractPacket;

public class EquipToolbeltItemPacket extends AbstractPacket {

    private ToolbeltSlotType slotType;
    private int toolbeltItemIndex;

    public EquipToolbeltItemPacket() { }

    public EquipToolbeltItemPacket(ToolbeltSlotType inventoryType, int toolbeltSlot) {
        this.slotType = inventoryType;
        this.toolbeltItemIndex = toolbeltSlot;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
        buffer.writeInt(slotType.ordinal());
        buffer.writeInt(toolbeltItemIndex);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
        int ordinal = buffer.readInt();
        if (ordinal < ToolbeltSlotType.values().length) {
            slotType = ToolbeltSlotType.values()[ordinal];
        }
        toolbeltItemIndex = buffer.readInt();
    }

    @Override
    public void handleClientSide(EntityPlayer player) {
    }

    @Override
    public void handleServerSide(EntityPlayer player) {
        if (toolbeltItemIndex > -1) {
            UtilToolbelt.equipItemFromToolbelt(player, slotType, toolbeltItemIndex, EnumHand.OFF_HAND);
        } else {
            UtilToolbelt.storeItemInToolbelt(player);
        }
    }
}
