package se.mickelus.tetra.items.modular.impl.toolbelt.suspend;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import se.mickelus.tetra.network.AbstractPacket;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
public class ToggleSuspendPacket extends AbstractPacket {

    boolean toggleOn;

    public ToggleSuspendPacket() { }

    public ToggleSuspendPacket(boolean toggleOn) {
        this.toggleOn = toggleOn;
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBoolean(toggleOn);
    }

    @Override
    public void fromBytes(FriendlyByteBuf buffer) {
        toggleOn = buffer.readBoolean();
    }

    @Override
    public void handle(Player player) {
        SuspendEffect.toggleSuspend(player, toggleOn);
    }
}
