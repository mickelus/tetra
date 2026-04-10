package se.mickelus.tetra.compat.forge.common;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;

public final class ForgeHooks {
    private ForgeHooks() {}

    public static int onBlockBreakEvent(Level level, GameType gameType, ServerPlayer player, BlockPos pos) {
        var event = CommonHooks.fireBlockBreak(level, gameType, player, pos, level.getBlockState(pos));
        return event.isCanceled() ? -1 : 0;
    }

    public static CriticalHitEvent getCriticalHit(Player player, Entity target, boolean vanillaCritical, float damageModifier) {
        return CommonHooks.fireCriticalHit(player, target, vanillaCritical, damageModifier);
    }
}
