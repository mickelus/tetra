package se.mickelus.tetra.blocks.forged.container;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import se.mickelus.tetra.network.AbstractPacket;
import se.mickelus.tetra.util.CastOptional;

public class ChangeCompartmentPacket extends AbstractPacket {

    private int compartmentIndex;

    public ChangeCompartmentPacket() {}

    public ChangeCompartmentPacket(int compartmentIndex) {
        this.compartmentIndex = compartmentIndex;
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(compartmentIndex);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        compartmentIndex = buffer.readInt();
    }

    @Override
    public void handle(EntityPlayer player) {
        CastOptional.cast(player.openContainer, ContainerForgedContainer.class)
                .ifPresent(container -> container.changeCompartment(compartmentIndex));
    }
}
