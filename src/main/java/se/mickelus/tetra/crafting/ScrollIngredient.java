package se.mickelus.tetra.crafting;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.minecraftforge.registries.RegistryObject;
import se.mickelus.tetra.blocks.scroll.ScrollData;
import se.mickelus.tetra.blocks.scroll.ScrollItem;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
public class ScrollIngredient implements ICustomIngredient {
    public static RegistryObject<IngredientType<ScrollIngredient>> type;
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

        return Objects.equals(data.key, ingredient.data.key)
                && Objects.equals(data.details, ingredient.data.details)
                && data.isIntricate == ingredient.data.isIntricate
                && data.material == ingredient.data.material
                && data.ribbon == ingredient.data.ribbon
                && Objects.equals(data.glyphs, ingredient.data.glyphs)
                && Objects.equals(data.schematics, ingredient.data.schematics)
                && Objects.equals(data.craftingEffects, ingredient.data.craftingEffects);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data.key, data.details, data.isIntricate, data.material, data.ribbon, data.glyphs, data.schematics, data.craftingEffects);
    }
}
