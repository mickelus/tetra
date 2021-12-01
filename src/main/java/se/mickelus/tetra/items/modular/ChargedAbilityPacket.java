package se.mickelus.tetra.items.modular;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import se.mickelus.tetra.network.AbstractPacket;
import se.mickelus.tetra.network.BlockPosPacket;

import java.util.Optional;

public class ChargedAbilityPacket extends BlockPosPacket {

    private int targetId = -1;
    private Hand hand;
    private int ticksUsed;

    private Vector3d hitVec;

    public ChargedAbilityPacket() {}

    public ChargedAbilityPacket(LivingEntity target, BlockPos pos, Vector3d hitVec, Hand hand, int ticksUsed) {
        super(pos == null ? BlockPos.ZERO : pos);
        targetId = Optional.ofNullable(target)
                .map(Entity::getId)
                .orElse(-1);

        this.hand = hand;
        this.ticksUsed = ticksUsed;

        this.hitVec = hitVec == null ? Vector3d.ZERO : hitVec;
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        super.toBytes(buffer);
        buffer.writeInt(targetId);
        buffer.writeInt(hand.ordinal());
        buffer.writeInt(ticksUsed);

        buffer.writeDouble(hitVec.x);
        buffer.writeDouble(hitVec.y);
        buffer.writeDouble(hitVec.z);
    }

    @Override
    public void fromBytes(PacketBuffer buffer) {
        super.fromBytes(buffer);
        targetId = buffer.readInt();
        hand = Hand.values()[buffer.readInt()];
        ticksUsed = buffer.readInt();

        double x = buffer.readDouble();
        double y = buffer.readDouble();
        double z = buffer.readDouble();
        hitVec = new Vector3d(x, y, z);
    }

    @Override
    public void handle(PlayerEntity player) {
        LivingEntity target = Optional.of(targetId)
                .filter(id -> id != -1)
                .map(id -> player.level.getEntity(id))
                .filter(entity -> entity instanceof LivingEntity)
                .map(entity -> (LivingEntity) entity)
                .orElse(null);

        ItemModularHandheld.handleChargedAbility(player, hand, target, BlockPos.ZERO.equals(pos) ? null : pos,
                Vector3d.ZERO.equals(hitVec) ? null : hitVec, ticksUsed);
    }
}
