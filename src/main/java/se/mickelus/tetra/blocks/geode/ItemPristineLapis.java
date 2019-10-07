package se.mickelus.tetra.blocks.geode;

import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.oredict.OreDictionary;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.TetraItemGroup;
import se.mickelus.tetra.items.TetraItem;
import se.mickelus.tetra.network.PacketHandler;

public class ItemPristineLapis extends TetraItem {
    private static final String unlocalizedName = "pristine_lapis";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ItemPristineLapis instance;

    public ItemPristineLapis() {
        setRegistryName(unlocalizedName);
        setUnlocalizedName(unlocalizedName);
        setCreativeTab(TetraItemGroup.getInstance());
    }

    @Override
    public void init(PacketHandler packetHandler) {
        OreDictionary.registerOre("gemLapis", this);
    }
}
