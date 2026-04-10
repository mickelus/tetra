package se.mickelus.tetra.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ReplaceTableModifier extends LootModifier {
    public static final MapCodec<ReplaceTableModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> LootModifier.codecStart(instance)
            .and(ResourceLocation.CODEC.fieldOf("table").forGetter(modifier -> modifier.table))
            .apply(instance, ReplaceTableModifier::new));

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
                .reloadableRegistries()
                .getLootTable(ResourceKey.create(Registries.LOOT_TABLE, table))
                .getRandomItems(newParams);
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
