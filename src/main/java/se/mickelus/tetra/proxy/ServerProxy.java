package se.mickelus.tetra.proxy;

import net.minecraft.core.Registry;
import net.minecraft.tags.SerializationTags;
import net.minecraft.tags.TagCollection;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import se.mickelus.tetra.blocks.ITetraBlock;
import se.mickelus.tetra.items.ITetraItem;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
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
    public Player getClientPlayer() {
        return null;
    }

    @Override
    public TagCollection<Item> getItemTags() {
        return SerializationTags.getInstance().getOrEmpty(Registry.ITEM_REGISTRY);
    }
}
