package se.mickelus.tetra.module.improvement;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import se.mickelus.tetra.network.AbstractPacket;

public class HonePacket extends AbstractPacket {

    ItemStack itemStack;

    public HonePacket() {}

    public HonePacket(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        ByteBufUtils.writeItemStack(buffer, itemStack);
    }

    @Override
    public void fromBytes(PacketBuffer buffer) {
        itemStack = ByteBufUtils.readItemStack(buffer);
    }

    @Override
    public void handle(PlayerEntity player) {
        ProgressionHelper.showHoneToastClient(itemStack);
    }
}
