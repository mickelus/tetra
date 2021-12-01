package se.mickelus.tetra.items.forged;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
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
    public void appendHoverText(ItemStack itemStack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Tooltips.reveal);
        tooltip.add(new TextComponent(" "));
        tooltip.add(ForgedBlockCommon.locationTooltip);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        playClick(context.getLevel(), context.getPlayer());
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        playClick(world, player);
        return super.use(world, player, hand);
    }

    private void playClick(Level world, Player player) {
        SoundEvent event = SoundEvents.TRIPWIRE_CLICK_OFF;
        if (Math.random() > 0.5f) {
            event = SoundEvents.TRIPWIRE_CLICK_ON;
        }
        world.playSound(player, player.blockPosition(), event, SoundSource.PLAYERS, 0.3f,  1f + 0.5f * (float) Math.random());
    }
}
