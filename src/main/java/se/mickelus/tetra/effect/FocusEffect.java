package se.mickelus.tetra.effect;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import se.mickelus.mutil.util.CastOptional;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.items.modular.impl.bow.ModularBowItem;

import java.util.concurrent.TimeUnit;

public class FocusEffect {
    private static final Cache<Integer, Integer> cache = CacheBuilder.newBuilder()
            .maximumSize(20)
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build();

    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.player.level.getGameTime() % 2 == 0) {
            if (hasApplicableItem(event.player) && event.player.isCrouching()) {
                Player player = event.player;
                int id = getIdentifier(player);
                Integer duration = cache.getIfPresent(id);
                boolean isDrawing = isDrawing(event.player);
                int change = isDrawing ? 1 : 2;
                cache.put(id, duration != null ? duration + change : change);
                player.setAirSupply(player.getAirSupply() - (isDrawing ? 6 : 2));

                if (player.getAirSupply() <= -20) {
                    player.setAirSupply(0);

                    player.hurt(DamageSource.DROWN, 2.0F);
                }

            } else {
                cache.invalidate(getIdentifier(event.player));
            }
        }
    }

    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getAmount() > 0
                && event.getSource() != DamageSource.DROWN
                && event.getEntity() instanceof Player player) {
            cache.invalidate(getIdentifier(player));
        }
    }

    public static void onFireArrow(Player player, ItemStack itemStack) {
        boolean hasEcho = CastOptional.cast(itemStack.getItem(), ModularItem.class)
                .filter(item -> item.getEffectLevel(itemStack, ItemEffect.focusEcho) > 0)
                .isPresent();

        if (!hasEcho) {
            cache.invalidate(getIdentifier(player));
        }
    }

    public static boolean isDrawing(Player player) {
        ItemStack itemStack = player.getUseItem();
        return CastOptional.cast(itemStack.getItem(), ModularBowItem.class)
                .map(item -> item.getProgress(itemStack, player) < 1)
                .orElse(false);
    }

    public static float getSpreadReduction(Player player, ItemStack itemStack) {
        Integer duration = cache.getIfPresent(getIdentifier(player));
        if (duration != null) {
            return CastOptional.cast(itemStack.getItem(), ModularItem.class)
                    .map(item -> (float) item.getEffectEfficiency(itemStack, ItemEffect.focus) * duration / 20f)
                    .orElse(0f);
        }
        return 0;
    }

    public static boolean hasApplicableItem(LivingEntity player) {
        return isApplicableItem(player.getMainHandItem()) || isApplicableItem(player.getOffhandItem());
    }

    public static boolean isApplicableItem(ItemStack itemStack) {
        return CastOptional.cast(itemStack.getItem(), ModularItem.class)
                .filter(item -> item.getEffectEfficiency(itemStack, ItemEffect.focus) > 0)
                .isPresent();
    }

    private static int getIdentifier(Player entity) {
        return entity.level.isClientSide ? -entity.getId() : entity.getId();
    }
}
