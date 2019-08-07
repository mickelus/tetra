package se.mickelus.tetra.blocks.geode;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.TetraCreativeTabs;
import se.mickelus.tetra.items.TetraItem;
import se.mickelus.tetra.network.PacketHandler;

public class ItemPristineDiamond extends TetraItem {
    private static final String unlocalizedName = "pristine_diamond";

    @GameRegistry.ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ItemPristineDiamond instance;

    public ItemPristineDiamond() {
        setRegistryName(unlocalizedName);
        setUnlocalizedName(unlocalizedName);
        setCreativeTab(TetraCreativeTabs.getInstance());
    }

    @Override
    public void init(PacketHandler packetHandler) {
        OreDictionary.registerOre("gemDiamond", this);
    }
}
