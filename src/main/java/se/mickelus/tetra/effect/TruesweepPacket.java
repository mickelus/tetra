package se.mickelus.tetra.effect;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import se.mickelus.tetra.network.AbstractPacket;

public class TruesweepPacket extends AbstractPacket {
    public TruesweepPacket() { }

    @Override
    public void toBytes(PacketBuffer buffer) {}

    @Override
    public void fromBytes(PacketBuffer buffer) {}

    @Override
    public void handle(PlayerEntity player) {
        ItemStack itemStack = player.getMainHandItem();
        if (player.getAttackStrengthScale(0.5f) > 0.9f && EffectHelper.getEffectLevel(itemStack, ItemEffect.truesweep) > 0
                && player.isOnGround() && !player.isSprinting()) {
            SweepingEffect.truesweep(itemStack, player);
        }
    }
}
