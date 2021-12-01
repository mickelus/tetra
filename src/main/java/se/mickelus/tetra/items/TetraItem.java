package se.mickelus.tetra.items;

import net.minecraft.world.item.Item;
import se.mickelus.tetra.network.PacketHandler;

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
