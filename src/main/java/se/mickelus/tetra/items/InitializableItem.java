package se.mickelus.tetra.items;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import se.mickelus.mutil.network.PacketHandler;

public interface InitializableItem {
    @OnlyIn(Dist.CLIENT)
    default void clientInit() {
    }

    default void commonInit(PacketHandler packetHandler) {
    }
}
