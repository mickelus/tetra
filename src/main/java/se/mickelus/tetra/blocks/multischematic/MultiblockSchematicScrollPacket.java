package se.mickelus.tetra.blocks.multischematic;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import se.mickelus.mutil.network.AbstractPacket;

public class MultiblockSchematicScrollPacket extends AbstractPacket {
    boolean isIncrease;

    public MultiblockSchematicScrollPacket() {
    }

    public MultiblockSchematicScrollPacket(boolean isIncrease) {
        this.isIncrease = isIncrease;
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBoolean(isIncrease);
    }

    @Override
    public void fromBytes(FriendlyByteBuf buffer) {
        isIncrease = buffer.readBoolean();
    }

    @Override
    public void handle(Player player) {
        MultiblockSchematicScrollHandler.shiftSchematic(player, isIncrease);
    }
}
