package se.mickelus.tetra.items.forged;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.TetraItem;
import se.mickelus.tetra.items.TetraItemGroup;

import javax.annotation.Nullable;
import java.util.List;

public class ItemQuickLatch extends TetraItem {
    private static final String unlocalizedName = "quick_latch";
    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ItemQuickLatch instance;

    public ItemQuickLatch() {
        super(new Properties().group(TetraItemGroup.instance));
        setRegistryName(unlocalizedName);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(new TranslationTextComponent("forged_description").setStyle(new Style().setColor(TextFormatting.DARK_GRAY )));
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        playClick(context.getWorld(), context.getPlayer());
        return ActionResultType.PASS;
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
