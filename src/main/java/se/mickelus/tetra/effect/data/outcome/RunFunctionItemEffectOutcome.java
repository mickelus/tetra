package se.mickelus.tetra.effect.data.outcome;

import com.google.gson.JsonObject;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.provider.EntityPositionProvider;
import se.mickelus.tetra.effect.data.provider.EntityProvider;
import se.mickelus.tetra.effect.data.provider.PositionProvider;
import se.mickelus.tetra.effect.data.provider.StandardEntityProvider;

public class RunFunctionItemEffectOutcome extends ItemEffectOutcome {
    ResourceLocation function;
    EntityProvider entity = new StandardEntityProvider(StandardEntityProvider.Target.source);
    PositionProvider position = new EntityPositionProvider(entity, EntityPositionProvider.Origin.feet);

    @Override
    public boolean perform(ItemEffectContext context) {
        MinecraftServer server = ((ServerLevel) context.getLevel()).getServer();
        CommandFunction function = server.getFunctions().get(this.function).orElse(null);
        if (function != null) {
            CommandSourceStack commandSourceStack = server.createCommandSourceStack()
                    .withPermission(2)
                    .withLevel((ServerLevel) context.getLevel())
                    .withEntity(entity.getEntity(context))
                    .withPosition(position.getPosition(context));

            int result = server.getFunctions().execute(function, commandSourceStack);
            return result > 0;
        }

        return false;
    }

    public static ItemEffectOutcome deserialize(JsonObject jsonObject) {
        return DataManager.gson.fromJson(jsonObject, RunFunctionItemEffectOutcome.class);
    }
}
