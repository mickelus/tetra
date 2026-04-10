package se.mickelus.tetra.items.forged;

import net.minecraft.network.chat.Component;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.Tooltips;
import se.mickelus.tetra.blocks.forged.ForgedBlockCommon;
import se.mickelus.tetra.items.TetraItem;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class QuickLatchItem extends TetraItem {
    public static final String identifier = "quick_latch";
    @ObjectHolder(registryName = "item", value = TetraMod.MOD_ID + ":" + identifier)
    public static QuickLatchItem instance;

    public QuickLatchItem() {
        super(new Properties());
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack itemStack, net.minecraft.world.item.Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Tooltips.reveal);
        tooltip.add(Component.literal(" "));
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
        world.playSound(player, player.blockPosition(), event, SoundSource.PLAYERS, 0.3f, 1f + 0.5f * (float) Math.random());
    }
}
