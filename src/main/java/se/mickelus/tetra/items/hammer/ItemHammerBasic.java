package se.mickelus.tetra.items.hammer;

import net.minecraft.item.ItemStack;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.capabilities.ICapabilityProvider;
import se.mickelus.tetra.items.TetraCreativeTabs;
import se.mickelus.tetra.items.TetraItem;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class ItemHammerBasic extends TetraItem implements ICapabilityProvider {
    private static final String unlocalizedName = "hammer_basic";

    public ItemHammerBasic() {

        setUnlocalizedName(unlocalizedName);
        setRegistryName(unlocalizedName);
        setMaxDamage(200);
        setMaxStackSize(1);
        setCreativeTab(TetraCreativeTabs.getInstance());
    }

    @Override
    public int getCapabilityLevel(ItemStack itemStack, Capability capability) {
        if (capability == Capability.hammer) {
            return 1;
        }

        return 0;
    }

    @Override
    public Collection<Capability> getCapabilities(ItemStack itemStack) {
        return Collections.singletonList(Capability.hammer);
    }
}
