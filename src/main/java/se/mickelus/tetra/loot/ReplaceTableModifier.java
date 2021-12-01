package se.mickelus.tetra.loot;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSet;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;

import javax.annotation.Nonnull;
import java.util.List;

public class ReplaceTableModifier extends LootModifier {
    ResourceLocation table;

    protected ReplaceTableModifier(ILootCondition[] conditions, ResourceLocation table) {
        super(conditions);

        this.table = table;
    }

    @Nonnull
    @Override
    protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
        LootContext newContext = new LootContext.Builder(context.getLevel())
                .withRandom(context.getRandom())
                .withLuck(context.getLuck())
                .create(LootParameterSets.EMPTY);
        newContext.setQueriedLootTableId(table);

        return context.getLevel()
                .getServer()
                .getLootTables()
                .get(table)
                .getRandomItems(newContext);
    }

    public static class Serializer extends GlobalLootModifierSerializer<ReplaceTableModifier> {
        public ReplaceTableModifier read(ResourceLocation location, JsonObject object, ILootCondition[] conditions) {
            return new ReplaceTableModifier(conditions, new ResourceLocation(object.get("table").getAsString()));
        }

        public JsonObject write(ReplaceTableModifier instance) {
            JsonObject result = makeConditions(instance.conditions);
            result.addProperty("table", instance.table.toString());

            return result;
        }
    }
}
