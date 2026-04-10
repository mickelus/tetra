package se.mickelus.tetra.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import se.mickelus.tetra.compat.forge.registries.ForgeRegistries;
import se.mickelus.tetra.compat.forge.registries.RegistryObject;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.module.schematic.requirement.IntegerPredicate;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class ItemAbilityIngredient implements ICustomIngredient {
    public static RegistryObject<IngredientType<ItemAbilityIngredient>> type;
    private static final Codec<IntegerPredicate> TIER_CODEC = Codec.PASSTHROUGH.xmap(
            dynamic -> {
                JsonElement json = dynamic.convert(JsonOps.INSTANCE).getValue();
                return json == null || json.isJsonNull() ? null : IntegerPredicate.Deserializer.deserialize(json);
            },
            predicate -> new Dynamic<>(JsonOps.INSTANCE, predicate == null ? JsonNull.INSTANCE : predicate.serialize())
    );
    public static final MapCodec<ItemAbilityIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemAbility.CODEC.fieldOf("tool").forGetter(ingredient -> ingredient.toolAction),
            TIER_CODEC.optionalFieldOf("tier").forGetter(ingredient -> Optional.ofNullable(ingredient.tier))
    ).apply(instance, (toolAction, tier) -> new ItemAbilityIngredient(toolAction, tier.orElse(null))));
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemAbilityIngredient> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());

    private final ItemAbility toolAction;
    @Nullable
    private final IntegerPredicate tier;

    public ItemAbilityIngredient(ItemAbility toolAction, @Nullable IntegerPredicate tier) {
        this.toolAction = toolAction;
        this.tier = tier;
    }

    @Override
    public boolean test(ItemStack input) {
        return !input.isEmpty()
                && input.canPerformAction(toolAction)
                && (tier == null || input.getItem() instanceof ItemModularHandheld item && tier.test(item.getHarvestTier(input, toolAction)));
    }

    @Override
    public Stream<ItemStack> getItems() {
        return ForgeRegistries.ITEMS.getValues().stream()
                .map(Item::getDefaultInstance)
                .filter(this::test);
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

        if (!(other instanceof ItemAbilityIngredient ingredient)) {
            return false;
        }

        return Objects.equals(toolAction, ingredient.toolAction)
                && Objects.equals(tier == null ? null : tier.serialize().toString(), ingredient.tier == null ? null : ingredient.tier.serialize().toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(toolAction, tier == null ? null : tier.serialize().toString());
    }
}
