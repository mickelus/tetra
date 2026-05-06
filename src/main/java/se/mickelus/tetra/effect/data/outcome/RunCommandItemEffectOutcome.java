package se.mickelus.tetra.effect.data.outcome;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
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

            CommandSourceStack commandSourceStack = server.createCommandSourceStack()
                    .withLevel(serverLevel)
                    .withPermission(2)
                    .withSuppressedOutput();

            if (position != null) {
                commandSourceStack = commandSourceStack.withPosition(position.getVector(context));
            }

            if (entity != null && entity.getEntity(context) != null) {
                commandSourceStack = commandSourceStack.withEntity(entity.getEntity(context));
            }

            server.getCommands().performPrefixedCommand(commandSourceStack, this.command);
            return true;
        }
        return false;
    }
}
