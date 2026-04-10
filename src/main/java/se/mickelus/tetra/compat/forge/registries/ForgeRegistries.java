package se.mickelus.tetra.compat.forge.registries;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.sounds.SoundEvent;
import se.mickelus.tetra.compat.forge.registries.tags.ITag;
import se.mickelus.tetra.compat.forge.registries.tags.ITagManager;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

public final class ForgeRegistries {
    public static final CompatRegistry<Block> BLOCKS = new CompatRegistry<>(BuiltInRegistries.BLOCK);
    public static final CompatRegistry<Item> ITEMS = new CompatRegistry<>(BuiltInRegistries.ITEM);
    public static final CompatRegistry<BlockEntityType<?>> BLOCK_ENTITY_TYPES = new CompatRegistry<>(BuiltInRegistries.BLOCK_ENTITY_TYPE);
    public static final CompatRegistry<MenuType<?>> MENU_TYPES = new CompatRegistry<>(BuiltInRegistries.MENU);
    public static final CompatRegistry<EntityType<?>> ENTITY_TYPES = new CompatRegistry<>(BuiltInRegistries.ENTITY_TYPE);
    public static final CompatRegistry<ParticleType<?>> PARTICLE_TYPES = new CompatRegistry<>(BuiltInRegistries.PARTICLE_TYPE);
    public static final CompatRegistry<MobEffect> MOB_EFFECTS = new CompatRegistry<>(BuiltInRegistries.MOB_EFFECT);
    public static final CompatRegistry<SoundEvent> SOUND_EVENTS = new CompatRegistry<>(BuiltInRegistries.SOUND_EVENT);
    public static final CompatRegistry<Attribute> ATTRIBUTES = new CompatRegistry<>(BuiltInRegistries.ATTRIBUTE);
    public static final CompatRegistry<Enchantment> ENCHANTMENTS = new CompatRegistry<>(Registries.ENCHANTMENT);

    public static final class Keys {
        public static final ResourceKey<Registry<com.mojang.serialization.MapCodec<? extends IGlobalLootModifier>>> GLOBAL_LOOT_MODIFIER_SERIALIZERS =
                NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS;

        private Keys() {}
    }

    private ForgeRegistries() {}

    public static final class CompatRegistry<T> {
        private final Registry<T> registry;
        private final ResourceKey<? extends Registry<T>> registryKey;
        private final ITagManager<T> tags;

        private CompatRegistry(Registry<T> registry) {
            this.registry = registry;
            this.registryKey = registry.key();
            this.tags = new CompatTagManager<>(this);
        }

        private CompatRegistry(ResourceKey<? extends Registry<T>> registryKey) {
            this.registry = null;
            this.registryKey = registryKey;
            this.tags = new CompatTagManager<>(this);
        }

        public T getValue(ResourceLocation location) {
            if (registry != null) {
                return registry.get(location);
            }
            return lookup().get(ResourceKey.create(registryKey, location)).map(Holder.Reference::value).orElse(null);
        }

        public ResourceLocation getKey(T value) {
            if (registry != null) {
                return registry.getKey(value);
            }
            return lookup().listElements()
                    .filter(holder -> holder.value() == value)
                    .map(holder -> holder.key().location())
                    .findFirst()
                    .orElse(null);
        }

        public boolean containsKey(ResourceLocation location) {
            return registry != null
                    ? registry.containsKey(location)
                    : lookup().get(ResourceKey.create(registryKey, location)).isPresent();
        }

        public Collection<T> getValues() {
            return registry != null
                    ? registry.stream().toList()
                    : lookup().listElements().map(Holder.Reference::value).toList();
        }

        public ITagManager<T> tags() {
            return tags;
        }

        private ResourceKey<? extends Registry<T>> registryKey() {
            return registryKey;
        }

        private HolderSet.Named<T> getTag(TagKey<T> key) {
            if (registry != null) {
                return registry.getTag(key).orElse(null);
            }
            return lookup().get(key).orElse(null);
        }

        private HolderLookup.RegistryLookup<T> lookup() {
            return Objects.requireNonNull(CommonHooks.resolveLookup(registryKey), "Registry lookup unavailable: " + registryKey.location());
        }
    }

    private static final class CompatTagManager<T> implements ITagManager<T> {
        private final CompatRegistry<T> registry;

        private CompatTagManager(CompatRegistry<T> registry) {
            this.registry = registry;
        }

        @Override
        public TagKey<T> createTagKey(ResourceLocation location) {
            return TagKey.create(registry.registryKey(), location);
        }

        @Override
        public ITag<T> getTag(TagKey<T> key) {
            return new CompatTag<>(registry.getTag(key));
        }
    }

    private static final class CompatTag<T> implements ITag<T> {
        private final HolderSet.Named<T> tag;

        private CompatTag(HolderSet.Named<T> tag) {
            this.tag = tag;
        }

        @Override
        public boolean contains(T value) {
            return tag != null && tag.stream().anyMatch(holder -> holder.value() == value);
        }

        @Override
        public boolean isEmpty() {
            return tag == null || tag.size() == 0;
        }

        @Override
        public java.util.stream.Stream<T> stream() {
            return tag == null ? java.util.stream.Stream.empty() : tag.stream().map(holder -> holder.value());
        }

        @Override
        public Iterator<T> iterator() {
            return stream().iterator();
        }
    }
}
