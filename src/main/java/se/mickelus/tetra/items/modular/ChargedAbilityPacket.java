package se.mickelus.tetra.items.modular;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import se.mickelus.tetra.network.BlockPosPacket;

import java.util.Optional;

public class ChargedAbilityPacket extends BlockPosPacket {

    private int targetId = -1;
    private InteractionHand hand;
    private int ticksUsed;

    private Vec3 hitVec;

    public ChargedAbilityPacket() {}

    public ChargedAbilityPacket(LivingEntity target, BlockPos pos, Vec3 hitVec, InteractionHand hand, int ticksUsed) {
        super(pos == null ? BlockPos.ZERO : pos);
        targetId = Optional.ofNullable(target)
                .map(Entity::getId)
                .orElse(-1);

        this.hand = hand;
        this.ticksUsed = ticksUsed;

        this.hitVec = hitVec == null ? Vec3.ZERO : hitVec;
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        super.toBytes(buffer);
        buffer.writeInt(targetId);
        buffer.writeInt(hand.ordinal());
        buffer.writeInt(ticksUsed);

        buffer.writeDouble(hitVec.x);
        buffer.writeDouble(hitVec.y);
        buffer.writeDouble(hitVec.z);
    }

    @Override
    public void fromBytes(FriendlyByteBuf buffer) {
        super.fromBytes(buffer);
        targetId = buffer.readInt();
        hand = InteractionHand.values()[buffer.readInt()];
        ticksUsed = buffer.readInt();

        double x = buffer.readDouble();
        double y = buffer.readDouble();
        double z = buffer.readDouble();
        hitVec = new Vec3(x, y, z);
    }

    @Override
    public void handle(Player player) {
        LivingEntity target = Optional.of(targetId)
                .filter(id -> id != -1)
                .map(id -> player.level.getEntity(id))
                .filter(entity -> entity instanceof LivingEntity)
                .map(entity -> (LivingEntity) entity)
                .orElse(null);

        ItemModularHandheld.handleChargedAbility(player, hand, target, BlockPos.ZERO.equals(pos) ? null : pos,
                Vec3.ZERO.equals(hitVec) ? null : hitVec, ticksUsed);
    }
}
