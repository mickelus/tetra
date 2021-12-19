package se.mickelus.tetra.blocks.forged.container;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import se.mickelus.mutil.network.AbstractPacket;
import se.mickelus.mutil.util.CastOptional;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ChangeCompartmentPacket extends AbstractPacket {

    private int compartmentIndex;

    public ChangeCompartmentPacket() {
    }

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
