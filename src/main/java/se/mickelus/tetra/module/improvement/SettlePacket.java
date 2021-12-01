package se.mickelus.tetra.module.improvement;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import se.mickelus.tetra.network.AbstractPacket;

import java.io.IOException;

public class SettlePacket extends AbstractPacket {
    ItemStack itemStack;
    String slot;

    public SettlePacket() {}

    public SettlePacket(ItemStack itemStack, String slot) {
        this.itemStack = itemStack;
        this.slot = slot;
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeItem(itemStack);
        buffer.writeUtf(slot);
    }

    @Override
    public void fromBytes(FriendlyByteBuf buffer) {
        itemStack = buffer.readItem();
        slot = buffer.readUtf();
    }

    @Override
    public void handle(Player player) {
        ProgressionHelper.showSettleToastClient(itemStack, slot);
    }
}
