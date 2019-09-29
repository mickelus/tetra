package se.mickelus.tetra.module.improvement;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
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
    public void toBytes(PacketBuffer buffer) {
        buffer.writeItemStack(itemStack);
        buffer.writeString(slot);
    }

    @Override
    public void fromBytes(PacketBuffer buffer) {
        itemStack = buffer.readItemStack();
        slot = buffer.readString();
    }

    @Override
    public void handle(PlayerEntity player) {
        ProgressionHelper.showSettleToastClient(itemStack, slot);
    }
}
