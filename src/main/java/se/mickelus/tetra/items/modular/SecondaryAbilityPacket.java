package se.mickelus.tetra.items.modular;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import se.mickelus.tetra.network.AbstractPacket;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
public class SecondaryAbilityPacket extends AbstractPacket {
    private int targetId = -1;
    private InteractionHand hand;

    public SecondaryAbilityPacket() {
    }

    public SecondaryAbilityPacket(@Nullable LivingEntity target, InteractionHand hand) {
        targetId = Optional.ofNullable(target)
                .map(Entity::getId)
                .orElse(-1);

        this.hand = hand;
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(targetId);
        buffer.writeInt(hand.ordinal());
    }

    @Override
    public void fromBytes(FriendlyByteBuf buffer) {
        targetId = buffer.readInt();
        hand = InteractionHand.values()[buffer.readInt()];
    }

    @Override
    public void handle(Player player) {
        LivingEntity target = Optional.of(targetId)
                .filter(id -> id != -1)
                .map(id -> player.level.getEntity(id))
                .filter(entity -> entity instanceof LivingEntity)
                .map(entity -> (LivingEntity) entity)
                .orElse(null);

        ItemModularHandheld.handleSecondaryAbility(player, hand, target);
    }
}
