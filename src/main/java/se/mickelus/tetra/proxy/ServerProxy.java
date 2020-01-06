package se.mickelus.tetra.proxy;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import se.mickelus.tetra.blocks.ITetraBlock;
import se.mickelus.tetra.items.ITetraItem;

public class ServerProxy implements IProxy {
    @Override
    public void preInit(ITetraItem[] items, ITetraBlock[] blocks) {

    }

    @Override
    public void init(FMLCommonSetupEvent event, ITetraItem[] items, ITetraBlock[] blocks) {

    }

    @Override
    public void postInit() {

    }

    @Override
    public PlayerEntity getClientPlayer() {
        return null;
    }

}
