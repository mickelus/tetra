package se.mickelus.tetra.items.toolbelt.booster;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.NBTHelper;
import se.mickelus.tetra.items.toolbelt.UtilToolbelt;
import se.mickelus.tetra.network.AbstractPacket;

public class UpdateBoosterPacket extends AbstractPacket {

    private boolean active;
    private boolean charged;

    public UpdateBoosterPacket() { }

    public UpdateBoosterPacket(boolean active) {
        this(active, false);
    }

    public UpdateBoosterPacket(boolean active, boolean charged) {
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
        ItemStack itemStack = UtilToolbelt.findToolbelt(player);

        if (!itemStack.isEmpty() && UtilBooster.canBoost(itemStack)) {
            UtilBooster.setActive(NBTHelper.getTag(itemStack), active, charged);
        }

    }

}
