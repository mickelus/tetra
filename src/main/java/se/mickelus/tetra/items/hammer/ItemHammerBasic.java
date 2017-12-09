package se.mickelus.tetra.items.hammer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import se.mickelus.tetra.blocks.workbench.BlockWorkbench;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.capabilities.ICapabilityProvider;
import se.mickelus.tetra.items.TetraCreativeTabs;
import se.mickelus.tetra.items.TetraItem;

import java.util.Collection;
import java.util.Collections;

public class ItemHammerBasic extends TetraItem implements ICapabilityProvider {
    private static final String unlocalizedName = "hammer_basic";

    public static ItemHammerBasic instance;

    public ItemHammerBasic() {

        setUnlocalizedName(unlocalizedName);
        setRegistryName(unlocalizedName);
        setMaxDamage(200);
        setMaxStackSize(1);
        setCreativeTab(TetraCreativeTabs.getInstance());

        instance = this;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return BlockWorkbench.upgradeWorkbench(player, world, pos, hand, facing);
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
