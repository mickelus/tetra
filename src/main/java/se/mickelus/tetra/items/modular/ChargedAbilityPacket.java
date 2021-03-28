package se.mickelus.tetra.items.modular;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import se.mickelus.tetra.network.AbstractPacket;
import se.mickelus.tetra.network.BlockPosPacket;

import java.util.Optional;

public class ChargedAbilityPacket extends BlockPosPacket {

    private int targetId = -1;
    private Hand hand;
    private int ticksUsed;

    public ChargedAbilityPacket() {}

    public ChargedAbilityPacket(LivingEntity target, BlockPos pos, Hand hand, int ticksUsed) {
        super(pos == null ? BlockPos.ZERO : pos);
        targetId = Optional.ofNullable(target)
                .map(Entity::getEntityId)
                .orElse(-1);

        this.hand = hand;
        this.ticksUsed = ticksUsed;
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeInt(targetId);
        buffer.writeInt(hand.ordinal());
        buffer.writeInt(ticksUsed);
    }

    @Override
    public void fromBytes(PacketBuffer buffer) {
        targetId = buffer.readInt();
        hand = Hand.values()[buffer.readInt()];
        ticksUsed = buffer.readInt();
    }

    @Override
    public void handle(PlayerEntity player) {
        LivingEntity target = Optional.of(targetId)
                .filter(id -> id != -1)
                .map(id -> player.world.getEntityByID(id))
                .filter(entity -> entity instanceof LivingEntity)
                .map(entity -> (LivingEntity) entity)
                .orElse(null);

        ItemModularHandheld.handleChargedAbility(player, hand, target, BlockPos.ZERO.equals(pos) ? null : pos, ticksUsed);
    }
}
