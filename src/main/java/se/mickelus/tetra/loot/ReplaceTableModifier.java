package se.mickelus.tetra.loot;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;

import javax.annotation.Nonnull;
import java.util.List;

public class ReplaceTableModifier extends LootModifier {
    ResourceLocation table;

    protected ReplaceTableModifier(LootItemCondition[] conditions, ResourceLocation table) {
        super(conditions);

        this.table = table;
    }

    @Nonnull
    @Override
    protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
        LootContext newContext = new LootContext.Builder(context.getLevel())
                .withRandom(context.getRandom())
                .withLuck(context.getLuck())
                .create(LootContextParamSets.EMPTY);
        newContext.setQueriedLootTableId(table);

        return context.getLevel()
                .getServer()
                .getLootTables()
                .get(table)
                .getRandomItems(newContext);
    }

    public static class Serializer extends GlobalLootModifierSerializer<ReplaceTableModifier> {
        public ReplaceTableModifier read(ResourceLocation location, JsonObject object, LootItemCondition[] conditions) {
            return new ReplaceTableModifier(conditions, new ResourceLocation(object.get("table").getAsString()));
        }

        public JsonObject write(ReplaceTableModifier instance) {
            JsonObject result = makeConditions(instance.conditions);
            result.addProperty("table", instance.table.toString());

            return result;
        }
    }
}
