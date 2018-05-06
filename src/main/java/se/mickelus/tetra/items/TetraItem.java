package se.mickelus.tetra.items;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import se.mickelus.tetra.network.PacketHandler;

public class TetraItem extends Item implements ITetraItem {

    @Override
    public void clientPreInit() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(this.getRegistryName(), "inventory"));
    }

    @Override
    public void init(PacketHandler packetHandler) {

    }
}
