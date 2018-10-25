package se.mickelus.tetra.blocks;

import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;
import se.mickelus.tetra.network.PacketHandler;

public interface ITetraBlock {
    public void clientPreInit();
    public void init(PacketHandler packetHandler);
    public boolean hasItem();
    public void registerItem(IForgeRegistry<Item> registry);
}
