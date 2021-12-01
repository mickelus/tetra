package se.mickelus.tetra.items;

import net.minecraft.item.Item;
import se.mickelus.tetra.network.PacketHandler;

import net.minecraft.item.Item.Properties;

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
