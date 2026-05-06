package se.mickelus.tetra.crafting;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import java.util.function.Supplier;
import se.mickelus.tetra.blocks.scroll.ScrollData;
import se.mickelus.tetra.blocks.scroll.ScrollItem;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
public class ScrollIngredient implements ICustomIngredient {
    public static Supplier<IngredientType<ScrollIngredient>> type;
    public static final MapCodec<ScrollIngredient> CODEC = ScrollData.MAP_CODEC.xmap(ScrollIngredient::new, ingredient -> ingredient.data);
    public static final StreamCodec<RegistryFriendlyByteBuf, ScrollIngredient> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());

    private final ScrollData data;

    public ScrollIngredient(ScrollData data) {
        this.data = data;
    }

    @Override
    public boolean test(ItemStack input) {
        return !input.isEmpty()
                && input.getItem() == ScrollItem.instance
                && data.key.equals(ScrollData.read(input).key);
    }

    @Override
    public Stream<ItemStack> getItems() {
        ItemStack itemStack = new ItemStack(ScrollItem.instance);
        data.write(itemStack);
        return Stream.of(itemStack);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return type.get();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof ScrollIngredient ingredient)) {
            return false;
        }

        return Objects.equals(data, ingredient.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }
}
