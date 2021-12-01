package se.mickelus.tetra.effect.revenge;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import se.mickelus.tetra.network.AbstractPacket;

public class RemoveRevengePacket extends AbstractPacket {
    private int entityId = -1;

    public RemoveRevengePacket(Entity attacker) {
        this.entityId = attacker.getId();
    }

    public RemoveRevengePacket() {}

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
        RevengeTracker.removeEnemy(player, entityId);
    }
}
