package se.mickelus.tetra.effect;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import se.mickelus.tetra.network.AbstractPacket;

public class RevengeTrackerPacket extends AbstractPacket {
    private int entityId = -1;

    public RevengeTrackerPacket(Entity attacker) {
        this.entityId = attacker.getEntityId();
    }

    public RevengeTrackerPacket() {}

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeVarInt(entityId);
    }

    @Override
    public void fromBytes(PacketBuffer buffer) {
        entityId = buffer.readVarInt();
    }

    @Override
    public void handle(PlayerEntity player) {
        RevengeTracker.addEnemy(entityId, player);
    }
}
