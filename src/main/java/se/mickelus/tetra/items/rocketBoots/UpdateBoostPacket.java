package se.mickelus.tetra.items.rocketBoots;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import se.mickelus.tetra.items.toolbelt.UtilToolbelt;
import se.mickelus.tetra.network.AbstractPacket;

public class UpdateBoostPacket extends AbstractPacket {

    private boolean active;
    private boolean charged;

    public UpdateBoostPacket() { }

    public UpdateBoostPacket(boolean active) {
        this(active, false);
    }

    public UpdateBoostPacket(boolean active, boolean charged) {
        this.active = active;
        this.charged = charged;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
        buffer.writeBoolean(active);
        buffer.writeBoolean(charged);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
        active = buffer.readBoolean();
        charged = buffer.readBoolean();
    }

    @Override
    public void handleClientSide(EntityPlayer player) {}

    @Override
    public void handleServerSide(EntityPlayer player) {
        System.out.println("POW; GOT SOME PKTS " +  charged);
        ItemStack stack;

        if (!UtilRocketBoots.hasBoots(player)) {
            return;
        }

        stack = player.getItemStackFromSlot(EntityEquipmentSlot.FEET);
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }

        ItemRocketBoots.setActive(stack.getTagCompound(), active, charged);
    }

}
