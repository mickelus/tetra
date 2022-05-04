package se.mickelus.tetra.blocks;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import se.mickelus.mutil.network.PacketHandler;

public interface InitializableBlock {

    @OnlyIn(Dist.CLIENT)
    default void clientInit() {
    }

    default void commonInit(PacketHandler packetHandler) {
    }
}
