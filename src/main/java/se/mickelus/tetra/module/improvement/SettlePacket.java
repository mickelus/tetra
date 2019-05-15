package se.mickelus.tetra.module.improvement;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import se.mickelus.tetra.network.AbstractPacket;

public class SettlePacket extends AbstractPacket {
    ItemStack itemStack;
    String slot;

    public SettlePacket() {}

    public SettlePacket(ItemStack itemStack, String slot) {
        this.itemStack = itemStack;
        this.slot = slot;
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        ByteBufUtils.writeItemStack(buffer, itemStack);
        ByteBufUtils.writeUTF8String(buffer, slot);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        itemStack = ByteBufUtils.readItemStack(buffer);
        slot = ByteBufUtils.readUTF8String(buffer);
    }

    @Override
    public void handle(EntityPlayer player) {
        ProgressionHelper.showSettleToastClient(itemStack, slot);
    }
}
