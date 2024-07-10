package se.mickelus.tetra.effect.data.outcome;

import com.google.gson.JsonObject;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.provider.EntityPositionProvider;
import se.mickelus.tetra.effect.data.provider.EntityProvider;
import se.mickelus.tetra.effect.data.provider.PositionProvider;
import se.mickelus.tetra.effect.data.provider.StandardEntityProvider;

public class RunCommandItemEffectOutcome extends ItemEffectOutcome {
    String command;
    EntityProvider entity = new StandardEntityProvider(StandardEntityProvider.Target.source);
    PositionProvider position = new EntityPositionProvider(entity, EntityPositionProvider.Origin.feet);

    @Override
    public boolean perform(ItemEffectContext context) {
        MinecraftServer server = ((ServerLevel) context.getLevel()).getServer();

        CommandSourceStack commandSourceStack = server.createCommandSourceStack()
                .withPermission(2)
                .withLevel((ServerLevel) context.getLevel())
                .withEntity(entity.getEntity(context))
                .withPosition(position.getPosition(context));

        int result = server.getCommands().performPrefixedCommand(commandSourceStack, this.command);
        return result > 0;
    }

    public static ItemEffectOutcome deserialize(JsonObject jsonObject) {
        return DataManager.gson.fromJson(jsonObject, RunCommandItemEffectOutcome.class);
    }
}
