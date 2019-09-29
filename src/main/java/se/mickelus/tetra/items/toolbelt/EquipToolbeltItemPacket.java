package se.mickelus.tetra.items.toolbelt;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
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
    public void toBytes(PacketBuffer buffer) {
        buffer.writeInt(slotType.ordinal());
        buffer.writeInt(hand.ordinal());
        buffer.writeInt(toolbeltItemIndex);
    }

    @Override
    public void fromBytes(PacketBuffer buffer) {
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
    public void handle(PlayerEntity player) {
        if (toolbeltItemIndex > -1) {
            UtilToolbelt.equipItemFromToolbelt(player, slotType, toolbeltItemIndex, hand);
        } else {
            UtilToolbelt.storeItemInToolbelt(player);
        }
    }
}
