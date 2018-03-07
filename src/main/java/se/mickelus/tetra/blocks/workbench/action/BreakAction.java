package se.mickelus.tetra.blocks.workbench.action;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import se.mickelus.tetra.blocks.geode.ItemGeode;
import se.mickelus.tetra.blocks.workbench.TileEntityWorkbench;
import se.mickelus.tetra.capabilities.Capability;

public class BreakAction implements WorkbenchAction {

    public static final String key = "breakGeode";


    @Override
    public String getKey() {
        return key;
    }

    @Override
    public boolean canPerformOn(ItemStack itemStack) {
        return !itemStack.isEmpty() && itemStack.getItem() instanceof ItemGeode;
    }

    @Override
    public Capability[] getRequiredCapabilitiesFor(ItemStack itemStack) {
        return new Capability[] {Capability.hammer};
    }

    @Override
    public int getCapabilityLevel(ItemStack itemStack, Capability capability) {
        if (Capability.hammer.equals(capability)) {
            return 2;
        }
        return 0;
    }

    @Override
    public void perform(EntityPlayer player, ItemStack itemStack, TileEntityWorkbench workbench) {
        ItemStack geodeContent = ItemGeode.instance.getRandomContent();
        if (!player.inventory.addItemStackToInventory(geodeContent)) {
            player.dropItem(geodeContent, false);
        }

        BlockPos pos = workbench.getPos();
        player.world.playSound(null, pos, SoundEvents.BLOCK_STONE_BREAK, player.getSoundCategory(),
                1.0F, 1.5f + (float) Math.random() * 0.5f);

        if (player.world instanceof WorldServer) {
            ((WorldServer)player.world).spawnParticle(EnumParticleTypes.ITEM_CRACK,
                    pos.getX() + 0.5d, pos.getY() + 1.1d, pos.getZ() + 0.5d,
                    6,  0, 0 ,0, player.world.rand.nextGaussian() * 0.2, Item.getIdFromItem(itemStack.getItem()), itemStack.getMetadata());
        }

        // todo: add proper criteria
        CriteriaTriggers.CONSUME_ITEM.trigger((EntityPlayerMP) player, itemStack);

        itemStack.setCount(itemStack.getCount() - 1);
    }
}
