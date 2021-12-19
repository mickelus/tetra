package se.mickelus.tetra.items;

import net.minecraft.world.item.Item;
import se.mickelus.mutil.network.PacketHandler;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TetraItem extends Item implements ITetraItem {

    public TetraItem(Properties properties) {
        super(properties);
    }

    @Override
    public void clientInit() {
    }

    @Override
    public void init(PacketHandler packetHandler) {

    }
}
