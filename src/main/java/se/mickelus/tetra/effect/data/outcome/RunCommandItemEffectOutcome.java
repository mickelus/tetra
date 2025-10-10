package se.mickelus.tetra.effect.data.outcome;

import net.minecraft.commands.CommandSigningContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.TaskChainer;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.provider.entity.EntityProvider;
import se.mickelus.tetra.effect.data.provider.vector.VectorProvider;

public class RunCommandItemEffectOutcome extends ItemEffectOutcome {
    String command;
    EntityProvider entity;
    VectorProvider position;

    @Override
    public boolean perform(ItemEffectContext context) {
        if (context.getLevel() instanceof ServerLevel serverLevel) {
            MinecraftServer server = serverLevel.getServer();

            CommandSourceStack commandSourceStack = new CommandSourceStack(
                    server,
                    Vec3.atLowerCornerOf(serverLevel.getSharedSpawnPos()),
                    Vec2.ZERO,
                    serverLevel,
                    2,
                    "Tetra effect",
                    Component.literal("Tetra effect"),
                    server,
                    entity.getEntity(context),
                    true,
                    (p_81361_, p_81362_, p_81363_) -> {
                    },
                    EntityAnchorArgument.Anchor.FEET,
                    CommandSigningContext.ANONYMOUS,
                    TaskChainer.immediate(server),
                    (p_280930_) -> {
                    }
            );

            int result = server.getCommands().performPrefixedCommand(commandSourceStack, this.command);
            return result > 0;
        }
        return false;
    }
}
