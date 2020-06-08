package se.mickelus.tetra.items.modular;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import se.mickelus.tetra.network.AbstractPacket;
import se.mickelus.tetra.util.CastOptional;

import java.util.Optional;

public class SecondaryAbilityPacket extends AbstractPacket {

    private int targetId = -1;
    private Hand hand;

    public SecondaryAbilityPacket() {}

    public SecondaryAbilityPacket(LivingEntity target, Hand hand) {
        targetId = Optional.ofNullable(target)
                .map(Entity::getEntityId)
                .orElse(-1);

        this.hand = hand;
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeInt(targetId);
        buffer.writeInt(hand.ordinal());
    }

    @Override
    public void fromBytes(PacketBuffer buffer) {
        targetId = buffer.readInt();
        hand = Hand.values()[buffer.readInt()];
    }

    @Override
    public void handle(PlayerEntity player) {
        LivingEntity target = Optional.of(targetId)
                .filter(id -> id != -1)
                .map(id -> player.world.getEntityByID(id))
                .filter(entity -> entity instanceof LivingEntity)
                .map(entity -> (LivingEntity) entity)
                .orElse(null);

        ItemModularHandheld.handleSecondaryAbility(player, hand, target);
    }
}
