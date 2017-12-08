package se.mickelus.tetra.items.hammer;

import se.mickelus.tetra.items.TetraCreativeTabs;
import se.mickelus.tetra.items.TetraItem;

public class ItemHammerBasic extends TetraItem {
    private static final String unlocalizedName = "hammer_basic";

    public ItemHammerBasic() {

        setUnlocalizedName(unlocalizedName);
        setRegistryName(unlocalizedName);
        setMaxDamage(200);
        setMaxStackSize(1);
        setCreativeTab(TetraCreativeTabs.getInstance());
    }
}
