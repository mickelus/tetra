package se.mickelus.tetra.compat.neoforge.network;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;

public final class NetworkHooks {
    private NetworkHooks() {}

    public static void openScreen(ServerPlayer player, MenuProvider provider) {
        player.openMenu(provider);
    }

    public static void openScreen(ServerPlayer player, MenuProvider provider, BlockPos pos) {
        player.openMenu(provider, pos);
    }
}
