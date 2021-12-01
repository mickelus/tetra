package se.mickelus.tetra.effect;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import se.mickelus.tetra.network.AbstractPacket;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
public class LungeEchoPacket extends AbstractPacket {
    boolean isVertical;

    public LungeEchoPacket() { }

    public LungeEchoPacket(boolean isVertical) {
        this.isVertical = isVertical;
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBoolean(isVertical);
    }

    @Override
    public void fromBytes(FriendlyByteBuf buffer) {
        isVertical = buffer.readBoolean();
    }

    @Override
    public void handle(Player player) {
        LungeEffect.receiveEchoPacket(player, isVertical);
    }
}
