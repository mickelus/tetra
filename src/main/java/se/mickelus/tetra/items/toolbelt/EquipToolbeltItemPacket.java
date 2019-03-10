package se.mickelus.tetra.items.toolbelt;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import se.mickelus.tetra.items.toolbelt.inventory.ToolbeltSlotType;
import se.mickelus.tetra.network.AbstractPacket;

public class EquipToolbeltItemPacket extends AbstractPacket {

    private ToolbeltSlotType slotType;
    private int toolbeltItemIndex;

    private EnumHand hand;

    public EquipToolbeltItemPacket() { }

    public EquipToolbeltItemPacket(ToolbeltSlotType inventoryType, int toolbeltSlot, EnumHand hand) {
        this.slotType = inventoryType;
        this.toolbeltItemIndex = toolbeltSlot;
        this.hand = hand;
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(slotType.ordinal());
        buffer.writeInt(hand.ordinal());
        buffer.writeInt(toolbeltItemIndex);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        int typeOrdinal = buffer.readInt();
        if (typeOrdinal < ToolbeltSlotType.values().length) {
            slotType = ToolbeltSlotType.values()[typeOrdinal];
        }

        int handOrdinal = buffer.readInt();
        if (handOrdinal < EnumHand.values().length) {
            hand = EnumHand.values()[handOrdinal];
        }

        toolbeltItemIndex = buffer.readInt();
    }

    @Override
    public void handle(EntityPlayer player) {
        if (toolbeltItemIndex > -1) {
            UtilToolbelt.equipItemFromToolbelt(player, slotType, toolbeltItemIndex, hand);
        } else {
            UtilToolbelt.storeItemInToolbelt(player);
        }
    }
}
