package se.mickelus.tetra.items;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import se.mickelus.mutil.network.PacketHandler;

public interface ITetraItem {
    @OnlyIn(Dist.CLIENT)
    default void clientInit() {
    }

    void init(PacketHandler packetHandler);
}
