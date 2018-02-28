package se.mickelus.tetra.blocks.workbench.action;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.blocks.workbench.TileEntityWorkbench;
import se.mickelus.tetra.capabilities.Capability;

public interface WorkbenchAction {

    public String getKey();
    public boolean canPerformOn(ItemStack itemStack);
    public Capability[] getRequiredCapabilitiesFor(ItemStack itemStack);
    public int getCapabilityLevel(ItemStack itemStack, Capability capability);
    public void perform(EntityPlayer player, ItemStack itemStack, TileEntityWorkbench workbench);
}
