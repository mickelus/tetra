package se.mickelus.tetra.effect.data.outcome;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.provider.entity.EntityProvider;
import se.mickelus.tetra.effect.data.provider.vector.VectorProvider;

public class RunCommandItemEffectOutcome extends ItemEffectOutcome {
    String command;
    EntityProvider entity;
    VectorProvider position;

    @Override
    public boolean perform(ItemEffectContext context) {
        MinecraftServer server = context.getLevel().getServer();

        CommandSourceStack commandSourceStack = server.createCommandSourceStack()
                .withPermission(2)
                .withLevel(context.getLevel())
                .withEntity(entity.getEntity(context))
                .withPosition(position.getVector(context));

        int result = server.getCommands().performPrefixedCommand(commandSourceStack, this.command);
        return result > 0;
    }
}
