package se.mickelus.tetra.effect;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import se.mickelus.tetra.network.AbstractPacket;

public class TruesweepPacket extends AbstractPacket {
    public TruesweepPacket() { }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {}

    @Override
    public void fromBytes(FriendlyByteBuf buffer) {}

    @Override
    public void handle(Player player) {
        ItemStack itemStack = player.getMainHandItem();
        if (player.getAttackStrengthScale(0.5f) > 0.9f && EffectHelper.getEffectLevel(itemStack, ItemEffect.truesweep) > 0
                && player.isOnGround() && !player.isSprinting()) {
            SweepingEffect.truesweep(itemStack, player);
        }
    }
}
