package se.mickelus.tetra.effect.data.outcome;

import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.provider.entity.EntityProvider;
import se.mickelus.tetra.effect.data.provider.vector.VectorProvider;

public class RunFunctionItemEffectOutcome extends ItemEffectOutcome {
    ResourceLocation function;
    EntityProvider entity;
    VectorProvider position;

    @Override
    public boolean perform(ItemEffectContext context) {
        if (context.getLevel() instanceof ServerLevel serverLevel) {
            MinecraftServer server = serverLevel.getServer();
            CommandFunction function = server.getFunctions().get(this.function).orElse(null);
            if (function != null) {
                CommandSourceStack commandSourceStack = server.createCommandSourceStack()
                        .withPermission(2)
                        .withLevel(serverLevel)
                        .withPosition(position.getVector(context));

                if (entity != null) {
                    commandSourceStack = commandSourceStack.withEntity(entity.getEntity(context));
                }

                int result = server.getFunctions().execute(function, commandSourceStack);
                return result > 0;
            }
        }

        return false;
    }
}
