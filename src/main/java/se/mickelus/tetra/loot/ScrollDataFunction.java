package se.mickelus.tetra.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import java.util.function.Supplier;
import se.mickelus.tetra.blocks.scroll.ScrollData;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class ScrollDataFunction extends LootItemConditionalFunction {
    public static final String identifier = "scroll";
    public static Supplier<LootItemFunctionType<ScrollDataFunction>> type;
    public static final MapCodec<ScrollDataFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> LootItemConditionalFunction.commonFields(instance)
            .and(ScrollData.MAP_CODEC.forGetter(function -> function.data))
            .apply(instance, ScrollDataFunction::new));

    private final ScrollData data;

    protected ScrollDataFunction(List<LootItemCondition> conditions, ScrollData data) {
        super(conditions);
        this.data = data;
    }

    @Override
    protected ItemStack run(ItemStack itemStack, LootContext context) {
        data.write(itemStack);
        return itemStack;
    }

    @Override
    public LootItemFunctionType<ScrollDataFunction> getType() {
        return type.get();
    }
}
