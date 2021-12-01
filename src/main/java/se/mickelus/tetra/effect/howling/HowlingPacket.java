package se.mickelus.tetra.effect.howling;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.effect.EffectHelper;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.network.AbstractPacket;

public class HowlingPacket extends AbstractPacket {
    public HowlingPacket() { }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {}

    @Override
    public void fromBytes(FriendlyByteBuf buffer) {}

    @Override
    public void handle(Player player) {
        ItemStack itemStack = player.getMainHandItem();
        if (player.getAttackStrengthScale(0.5f) > 0.9f) {
            int effectLevel = EffectHelper.getEffectLevel(itemStack, ItemEffect.howling);
            if (effectLevel > 0) {
                HowlingEffect.trigger(itemStack, player, effectLevel);
            }
        }
    }
}
