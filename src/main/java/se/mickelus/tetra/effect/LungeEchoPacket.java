package se.mickelus.tetra.effect;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import se.mickelus.tetra.network.AbstractPacket;

public class LungeEchoPacket extends AbstractPacket {
    boolean isVertical;

    public LungeEchoPacket() { }

    public LungeEchoPacket(boolean isVertical) {
        this.isVertical = isVertical;
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeBoolean(isVertical);
    }

    @Override
    public void fromBytes(PacketBuffer buffer) {
        isVertical = buffer.readBoolean();
    }

    @Override
    public void handle(PlayerEntity player) {
        LungeEffect.receiveEchoPacket(player, isVertical);
    }
}
