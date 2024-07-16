package se.mickelus.tetra.effect.data.outcome;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.provider.entity.EntityProvider;
import se.mickelus.tetra.effect.data.provider.entity.StandardEntityProvider;
import se.mickelus.tetra.effect.data.provider.vector.EntityVectorProvider;
import se.mickelus.tetra.effect.data.provider.vector.VectorProvider;

public class RunCommandItemEffectOutcome extends ItemEffectOutcome {
    String command;
    EntityProvider entity = new StandardEntityProvider(StandardEntityProvider.Target.source);
    VectorProvider position = new EntityVectorProvider(entity, EntityVectorProvider.Origin.feet);

    @Override
    public boolean perform(ItemEffectContext context) {
        MinecraftServer server = ((ServerLevel) context.getLevel()).getServer();

        CommandSourceStack commandSourceStack = server.createCommandSourceStack()
                .withPermission(2)
                .withLevel((ServerLevel) context.getLevel())
                .withEntity(entity.getEntity(context))
                .withPosition(position.getVector(context));

        int result = server.getCommands().performPrefixedCommand(commandSourceStack, this.command);
        return result > 0;
    }
}
