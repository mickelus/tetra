package se.mickelus.tetra.items.modular.impl.bow;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import se.mickelus.tetra.network.AbstractPacket;

import java.util.Optional;

public class ProjectileMotionPacket extends AbstractPacket {
    private int entityId = -1;
    private float motionX;
    private float motionY;
    private float motionZ;

    public ProjectileMotionPacket() {}

    public ProjectileMotionPacket(ProjectileEntity target) {
        entityId = target.getId();

        Vector3d motion = target.getDeltaMovement();
        motionX = (float) motion.x;
        motionY = (float) motion.y;
        motionZ = (float) motion.z;
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeVarInt(entityId);
        buffer.writeFloat(motionX);
        buffer.writeFloat(motionY);
        buffer.writeFloat(motionZ);
    }

    @Override
    public void fromBytes(PacketBuffer buffer) {
        entityId = buffer.readVarInt();
        motionX = buffer.readFloat();
        motionY = buffer.readFloat();
        motionZ = buffer.readFloat();
    }

    @Override
    public void handle(PlayerEntity player) {
        Optional.of(entityId)
                .filter(id -> id != -1)
                .map(id -> player.level.getEntity(id))
                .ifPresent(entity -> entity.setDeltaMovement(motionX, motionY, motionZ));
    }
}
