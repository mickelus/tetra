package se.mickelus.tetra.items.forged;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.*;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.Tooltips;
import se.mickelus.tetra.blocks.forged.ForgedBlockCommon;
import se.mickelus.tetra.items.TetraItem;
import se.mickelus.tetra.items.TetraItemGroup;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.item.Item.Properties;

public class ItemQuickLatch extends TetraItem {
    private static final String unlocalizedName = "quick_latch";
    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ItemQuickLatch instance;

    public ItemQuickLatch() {
        super(new Properties().tab(TetraItemGroup.instance));
        setRegistryName(unlocalizedName);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(Tooltips.reveal);
        tooltip.add(new StringTextComponent(" "));
        tooltip.add(ForgedBlockCommon.locationTooltip);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        playClick(context.getLevel(), context.getPlayer());
        return ActionResultType.PASS;
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        playClick(world, player);
        return super.use(world, player, hand);
    }

    private void playClick(World world, PlayerEntity player) {
        SoundEvent event = SoundEvents.TRIPWIRE_CLICK_OFF;
        if (Math.random() > 0.5f) {
            event = SoundEvents.TRIPWIRE_CLICK_ON;
        }
        world.playSound(player, player.blockPosition(), event, SoundCategory.PLAYERS, 0.3f,  1f + 0.5f * (float) Math.random());
    }
}
