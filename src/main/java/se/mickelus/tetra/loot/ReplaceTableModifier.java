package se.mickelus.tetra.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
public class ReplaceTableModifier extends LootModifier {
    public static final Supplier<Codec<ReplaceTableModifier>> codec = Suppliers.memoize(() -> RecordCodecBuilder.create(instance -> instance.group(
            LOOT_CONDITIONS_CODEC.fieldOf("conditions").forGetter(lm -> lm.conditions),
            ResourceLocation.CODEC.fieldOf("table").forGetter(i -> i.table)
    ).apply(instance, ReplaceTableModifier::new)));

    public ResourceLocation table;

    protected ReplaceTableModifier(LootItemCondition[] conditions, ResourceLocation table) {
        super(conditions);

        this.table = table;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        LootParams newParams = new LootParams.Builder(context.getLevel())
                .withLuck(context.getLuck())
                .create(LootContextParamSets.EMPTY);
        context.setQueriedLootTableId(table);

        return context.getLevel()
                .getServer()
                .getLootData()
                .getLootTable(table)
                .getRandomItems(newParams);
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return codec.get();
    }
}
