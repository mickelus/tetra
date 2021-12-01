package se.mickelus.tetra.effect.howling;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import se.mickelus.tetra.effect.EffectHelper;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.network.AbstractPacket;

public class HowlingPacket extends AbstractPacket {
    public HowlingPacket() { }

    @Override
    public void toBytes(PacketBuffer buffer) {}

    @Override
    public void fromBytes(PacketBuffer buffer) {}

    @Override
    public void handle(PlayerEntity player) {
        ItemStack itemStack = player.getMainHandItem();
        if (player.getAttackStrengthScale(0.5f) > 0.9f) {
            int effectLevel = EffectHelper.getEffectLevel(itemStack, ItemEffect.howling);
            if (effectLevel > 0) {
                HowlingEffect.trigger(itemStack, player, effectLevel);
            }
        }
    }
}
