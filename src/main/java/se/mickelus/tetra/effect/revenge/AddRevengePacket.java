package se.mickelus.tetra.effect.revenge;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import se.mickelus.tetra.network.AbstractPacket;

public class AddRevengePacket extends AbstractPacket {
    private int entityId = -1;

    public AddRevengePacket(Entity attacker) {
        this.entityId = attacker.getId();
    }

    public AddRevengePacket() {}

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
        RevengeTracker.addEnemy(player, entityId);
    }
}
