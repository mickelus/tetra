package se.mickelus.tetra.blocks.forged.container;

import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import se.mickelus.tetra.network.AbstractPacket;
import se.mickelus.tetra.util.CastOptional;

public class ChangeCompartmentPacket extends AbstractPacket {

    private int compartmentIndex;

    public ChangeCompartmentPacket() {}

    public ChangeCompartmentPacket(int compartmentIndex) {
        this.compartmentIndex = compartmentIndex;
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(compartmentIndex);
    }

    @Override
    public void fromBytes(FriendlyByteBuf buffer) {
        compartmentIndex = buffer.readInt();
    }

    @Override
    public void handle(Player player) {
        CastOptional.cast(player.containerMenu, ForgedContainerContainer.class)
                .ifPresent(container -> container.changeCompartment(compartmentIndex));
    }
}
