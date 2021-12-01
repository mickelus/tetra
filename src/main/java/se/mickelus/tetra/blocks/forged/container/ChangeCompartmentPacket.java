package se.mickelus.tetra.blocks.forged.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import se.mickelus.tetra.network.AbstractPacket;
import se.mickelus.tetra.util.CastOptional;

public class ChangeCompartmentPacket extends AbstractPacket {

    private int compartmentIndex;

    public ChangeCompartmentPacket() {}

    public ChangeCompartmentPacket(int compartmentIndex) {
        this.compartmentIndex = compartmentIndex;
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeInt(compartmentIndex);
    }

    @Override
    public void fromBytes(PacketBuffer buffer) {
        compartmentIndex = buffer.readInt();
    }

    @Override
    public void handle(PlayerEntity player) {
        CastOptional.cast(player.containerMenu, ForgedContainerContainer.class)
                .ifPresent(container -> container.changeCompartment(compartmentIndex));
    }
}
