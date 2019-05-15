package se.mickelus.tetra.module.improvement;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import se.mickelus.tetra.network.AbstractPacket;

public class HonePacket extends AbstractPacket {

    ItemStack itemStack;

    public HonePacket() {}

    public HonePacket(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        ByteBufUtils.writeItemStack(buffer, itemStack);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        itemStack = ByteBufUtils.readItemStack(buffer);
    }

    @Override
    public void handle(EntityPlayer player) {
        ProgressionHelper.showHoneToastClient(itemStack);
    }
}
