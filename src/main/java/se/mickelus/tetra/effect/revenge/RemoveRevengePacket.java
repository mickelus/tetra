package se.mickelus.tetra.effect.revenge;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import se.mickelus.tetra.network.AbstractPacket;

public class RemoveRevengePacket extends AbstractPacket {
    private int entityId = -1;

    public RemoveRevengePacket(Entity attacker) {
        this.entityId = attacker.getId();
    }

    public RemoveRevengePacket() {}

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeVarInt(entityId);
    }

    @Override
    public void fromBytes(FriendlyByteBuf buffer) {
        entityId = buffer.readVarInt();
    }

    @Override
    public void handle(Player player) {
        RevengeTracker.removeEnemy(player, entityId);
    }
}
