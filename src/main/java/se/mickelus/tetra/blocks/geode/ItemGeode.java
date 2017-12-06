package se.mickelus.tetra.blocks.geode;

import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.TetraCreativeTabs;
import se.mickelus.tetra.items.TetraItem;

public class ItemGeode extends TetraItem {

    public static ItemGeode instance;
    private final String unlocalizedName = "geode";

    public ItemGeode() {
        setRegistryName(unlocalizedName);
        setUnlocalizedName(unlocalizedName);
        setCreativeTab(TetraCreativeTabs.getInstance());

        instance = this;
    }

    @SubscribeEvent
    public void registerItem(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(this);
        System.out.println("GEODE " + getRegistryName());
    }

}
