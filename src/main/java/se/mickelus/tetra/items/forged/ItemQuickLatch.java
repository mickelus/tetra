package se.mickelus.tetra.items.forged;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.TetraCreativeTabs;
import se.mickelus.tetra.items.TetraItem;

import javax.annotation.Nullable;
import java.util.List;

public class ItemQuickLatch extends TetraItem {
    private static final String unlocalizedName = "quick_latch";
    @GameRegistry.ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ItemQuickLatch instance;

    public ItemQuickLatch() {
        setRegistryName(unlocalizedName);
        setUnlocalizedName(unlocalizedName);
        setCreativeTab(TetraCreativeTabs.getInstance());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
        tooltip.add(ChatFormatting.DARK_GRAY + I18n.format("item." + unlocalizedName + ".description"));
        tooltip.add(ChatFormatting.DARK_GRAY + I18n.format("forged_description"));
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        playClick(world, player);
        return EnumActionResult.PASS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        playClick(world, player);
        return super.onItemRightClick(world, player, hand);
    }

    private void playClick(World world, EntityPlayer player) {
        SoundEvent event = SoundEvents.BLOCK_TRIPWIRE_CLICK_OFF;
        if (Math.random() > 0.5f) {
            event = SoundEvents.BLOCK_TRIPWIRE_CLICK_ON;
        }
        world.playSound(player, player.getPosition(), event, SoundCategory.PLAYERS, 0.3f,  1f + 0.5f * (float) Math.random());
    }
}
