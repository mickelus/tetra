package se.mickelus.tetra.items.modular.impl.toolbelt.suspend;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import se.mickelus.tetra.network.AbstractPacket;

public class ToggleSuspendPacket extends AbstractPacket {

    boolean toggleOn;

    public ToggleSuspendPacket() { }

    public ToggleSuspendPacket(boolean toggleOn) {
        this.toggleOn = toggleOn;
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeBoolean(toggleOn);
    }

    @Override
    public void fromBytes(PacketBuffer buffer) {
        toggleOn = buffer.readBoolean();
    }

    @Override
    public void handle(PlayerEntity player) {
        SuspendEffect.toggleSuspend(player, toggleOn);
    }
}
