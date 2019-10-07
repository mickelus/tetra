package se.mickelus.tetra.items.forged;

import net.minecraft.util.text.TextFormatting;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.TetraItemGroup;
import se.mickelus.tetra.items.TetraItem;

import javax.annotation.Nullable;
import java.util.List;

public class ItemQuickLatch extends TetraItem {
    private static final String unlocalizedName = "quick_latch";
    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ItemQuickLatch instance;

    public ItemQuickLatch() {
        setRegistryName(unlocalizedName);
        setUnlocalizedName(unlocalizedName);
        setCreativeTab(TetraItemGroup.getInstance());
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
        tooltip.add(TextFormatting.DARK_GRAY + I18n.format("item." + unlocalizedName + ".description"));
        tooltip.add(TextFormatting.DARK_GRAY + I18n.format("forged_description"));
    }

    @Override
    public EnumActionResult onItemUse(PlayerEntity player, World world, BlockPos pos, Hand hand, Direction facing, float hitX, float hitY, float hitZ) {
        playClick(world, player);
        return EnumActionResult.PASS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        playClick(world, player);
        return super.onItemRightClick(world, player, hand);
    }

    private void playClick(World world, PlayerEntity player) {
        SoundEvent event = SoundEvents.BLOCK_TRIPWIRE_CLICK_OFF;
        if (Math.random() > 0.5f) {
            event = SoundEvents.BLOCK_TRIPWIRE_CLICK_ON;
        }
        world.playSound(player, player.getPosition(), event, SoundCategory.PLAYERS, 0.3f,  1f + 0.5f * (float) Math.random());
    }
}
