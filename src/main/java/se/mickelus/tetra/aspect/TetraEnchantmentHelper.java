package se.mickelus.tetra.aspect;

import com.google.common.collect.HashBiMap;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.common.CommonHooks;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;
import org.apache.commons.lang3.tuple.Pair;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static se.mickelus.tetra.util.ItemStackTagHelper.*;

public class TetraEnchantmentHelper {
    private static final Map<ItemAspect, EnchantmentRules> aspectMap = HashBiMap.create();
    private static final ThreadLocal<Set<ItemStack>> mappingItems =
            ThreadLocal.withInitial(() -> Collections.newSetFromMap(new IdentityHashMap<>()));

    public static void init() {
        aspectMap.put(ItemAspect.armor, new EnchantmentRules(items(Items.LEATHER_BOOTS, Items.IRON_LEGGINGS, Items.IRON_CHESTPLATE, Items.IRON_HELMET), "additions/armor", "exclusions/armor"));
        aspectMap.put(ItemAspect.armorFeet, new EnchantmentRules(items(Items.LEATHER_BOOTS), "additions/armor_feet", "exclusions/armor_feet"));
        aspectMap.put(ItemAspect.armorLegs, new EnchantmentRules(items(Items.IRON_LEGGINGS), "additions/armor_legs", "exclusions/armor_legs"));
        aspectMap.put(ItemAspect.armorChest, new EnchantmentRules(items(Items.IRON_CHESTPLATE), "additions/armor_chest", "exclusions/armor_chest"));
        aspectMap.put(ItemAspect.armorHead, new EnchantmentRules(items(Items.IRON_HELMET), "additions/armor_head", "exclusions/armor_head"));
        aspectMap.put(ItemAspect.edgedWeapon, new EnchantmentRules(items(Items.DIAMOND_SWORD, Items.DIAMOND_AXE), "additions/edged_weapon", "exclusions/edged_weapon"));
        aspectMap.put(ItemAspect.bluntWeapon, new EnchantmentRules(items(Items.DIAMOND_AXE, Items.MACE), "additions/blunt_weapon", "exclusions/blunt_weapon"));
        aspectMap.put(ItemAspect.pointyWeapon, new EnchantmentRules(items(Items.TRIDENT), "additions/pointy_weapon", "exclusions/pointy_weapon"));
        aspectMap.put(ItemAspect.throwable, new EnchantmentRules(List.of(), "additions/throwable", "exclusions/throwable"));
        aspectMap.put(ItemAspect.blockBreaker, new EnchantmentRules(items(Items.DIAMOND_PICKAXE, Items.DIAMOND_AXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_HOE), "additions/block_breaker", "exclusions/block_breaker"));
        aspectMap.put(ItemAspect.fishingRod, new EnchantmentRules(items(Items.FISHING_ROD), "additions/fishing_rod", "exclusions/fishing_rod"));
        aspectMap.put(ItemAspect.breakable, new EnchantmentRules(items(Items.SHEARS), "additions/breakable", "exclusions/breakable"));
        aspectMap.put(ItemAspect.bow, new EnchantmentRules(items(Items.BOW), "additions/bow", "exclusions/bow"));
        aspectMap.put(ItemAspect.wearable, new EnchantmentRules(items(Items.ELYTRA, Items.IRON_HELMET), "additions/wearable", "exclusions/wearable"));
        aspectMap.put(ItemAspect.crossbow, new EnchantmentRules(items(Items.CROSSBOW), "additions/crossbow", "exclusions/crossbow"));
        aspectMap.put(ItemAspect.vanishable, new EnchantmentRules(items(Items.DIAMOND_SWORD), "additions/vanishable", "exclusions/vanishable"));
    }

    public static void registerMapping(ItemAspect aspect, EnchantmentRules rules) {
        aspectMap.put(aspect, rules);
    }

    public static boolean isApplicableForAspects(Enchantment enchantment, boolean fromTable, Map<ItemAspect, Integer> aspects) {
        int requiredLevel = fromTable ? 2 : 1;

        return aspects.entrySet().stream()
                .filter(entry -> entry.getValue() >= requiredLevel)
                .filter(entry -> aspectMap.containsKey(entry.getKey()))
                .anyMatch(entry -> aspectMap.get(entry.getKey()).isApplicable(enchantment));
    }

    public static void ensureMappings(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof IModularItem)) {
            return;
        }

        if (itemStack.getTagEnchantments().isEmpty()) {
            removeTagKey(itemStack, "EnchantmentMapping");
            return;
        }

        if (mappingItems.get().contains(itemStack)) {
            return;
        }

        CompoundTag mappings = getTagElement(itemStack, "EnchantmentMapping");
        if (mappings == null || mappings.getAllKeys().size() < itemStack.getTagEnchantments().size()) {
            mapEnchantments(itemStack);
        }
    }

    public static ItemStack removeAllEnchantments(ItemStack itemStack) {
        EnchantmentHelper.setEnchantments(itemStack, ItemEnchantments.EMPTY);
        removeTagKey(itemStack, "EnchantmentMapping");

        IModularItem.updateIdentifier(itemStack);

        return itemStack;
    }

    public static ItemStack transferReplacementEnchantments(ItemStack original, ItemStack replacementStack) {
        Optional.of(original.getTagEnchantments())
                .filter(enchantments -> !enchantments.isEmpty())
                .ifPresent(enchantments -> {
                    EnchantmentHelper.setEnchantments(replacementStack, enchantments);
                    mapEnchantments(replacementStack);
                });

        return replacementStack;
    }

    public static void applyEnchantment(ItemStack itemStack, String slot, Enchantment enchantment, int level) {
        applyEnchantment(itemStack, slot, getHolder(enchantment), level);
    }

    public static void applyEnchantment(ItemStack itemStack, String slot, Holder<Enchantment> enchantment, int level) {
        itemStack.enchant(enchantment, level);
        mapEnchantment(itemStack, slot, enchantment);
    }

    public static void mapEnchantment(ItemStack itemStack, String slot, Enchantment enchantment) {
        mapEnchantment(itemStack, slot, getHolder(enchantment));
    }

    public static void mapEnchantment(ItemStack itemStack, String slot, Holder<Enchantment> enchantment) {
        CompoundTag tag = getOrCreateTag(itemStack);
        CompoundTag map = tag.contains("EnchantmentMapping", Tag.TAG_COMPOUND)
                ? tag.getCompound("EnchantmentMapping")
                : new CompoundTag();
        map.putString(requireEnchantmentKey(enchantment).toString(), slot);
        tag.put("EnchantmentMapping", map);
    }

    public static void mapEnchantments(ItemStack itemStack) {
        if (itemStack.getTagEnchantments().isEmpty()) {
            removeTagKey(itemStack, "EnchantmentMapping");
            return;
        }

        Set<ItemStack> inProgress = mappingItems.get();
        if (!inProgress.add(itemStack)) {
            return;
        }

        try {
            CompoundTag tag = getOrCreateTag(itemStack);
            CompoundTag mappings = tag.contains("EnchantmentMapping", Tag.TAG_COMPOUND)
                    ? tag.getCompound("EnchantmentMapping")
                    : new CompoundTag();
            Map<String, String> mapped = Optional.of(mappings)
                    .map(CompoundTag::getAllKeys)
                    .stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toMap(Function.identity(), mappings::getString));

            ItemModuleMajor[] modules = ((IModularItem) itemStack.getItem()).getMajorModules(itemStack);
            Map<String, Integer> capacity = Arrays.stream(modules)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(
                            ItemModule::getSlot,
                            module -> module.getAvailableMagicCapacityForMapping(itemStack),
                            Integer::max,
                            LinkedHashMap::new));

            itemStack.getTagEnchantments().entrySet().forEach(entry ->
                    getEnchantmentKey(entry.getKey())
                            .map(ResourceLocation::toString)
                            .map(mapped::get)
                            .ifPresent(slot -> {
                                Enchantment enchantment = entry.getKey().value();
                                if (enchantment != null) {
                                    int cost = getEnchantmentCapacityCost(enchantment, entry.getIntValue());
                                    capacity.merge(slot, cost, Integer::sum);
                                }
                            }));

            itemStack.getTagEnchantments().entrySet().stream()
                    .filter(entry -> {
                        return getEnchantmentKey(entry.getKey())
                                .map(ResourceLocation::toString)
                                .map(key -> !mapped.containsKey(key))
                                .orElse(false);
                    })
                    .forEach(entry -> {
                        Holder<Enchantment> holder = entry.getKey();
                        Enchantment enchantment = entry.getKey().value();
                        if (enchantment != null) {
                            Arrays.stream(modules)
                                    .filter(Objects::nonNull)
                                    .filter(module -> module.acceptsEnchantment(itemStack, enchantment, false))
                                    .map(ItemModule::getSlot)
                                    .max(Comparator.comparing(slot -> capacity.getOrDefault(slot, 0)))
                                    .ifPresent(slot -> {
                                        mapEnchantment(itemStack, slot, holder);
                                        int cost = getEnchantmentCapacityCost(enchantment, entry.getIntValue());
                                        capacity.merge(slot, cost, Integer::sum);
                                    });
                        }
                    });
            if (mappings.getAllKeys().isEmpty()) {
                tag.remove("EnchantmentMapping");
            } else {
                tag.put("EnchantmentMapping", mappings);
            }
        } finally {
            inProgress.remove(itemStack);
            if (inProgress.isEmpty()) {
                mappingItems.remove();
            }
        }
    }

    @Nullable
    public static Pair<String, Integer> getEnchantmentPrimitive(CompoundTag nbt) {
        return Pair.of(nbt.getString("id"), nbt.getInt("lvl"));
    }

    @Nullable
    public static Pair<Enchantment, Integer> getEnchantment(CompoundTag nbt) {
        return Optional.ofNullable(ForgeRegistries.ENCHANTMENTS.getValue(ResourceLocation.parse(nbt.getString("id"))))
                .map(enchantment -> Pair.of(enchantment, nbt.getInt("lvl")))
                .orElse(null);
    }

    public static int getEnchantmentCapacityCost(Enchantment enchantment, int level) {
        return -(enchantment.getMaxCost(level) + enchantment.getMinCost(level));
    }

    public static void removeEnchantment(ItemStack itemStack, Enchantment enchantment) {
        getEnchantmentKey(enchantment)
                .ifPresent(enchantmentKey -> removeEnchantment(itemStack, enchantmentKey.toString()));
    }

    public static void removeEnchantment(ItemStack itemStack, String enchantment) {
        Optional.ofNullable(getTagElement(itemStack, "EnchantmentMapping"))
                .ifPresent(map -> map.remove(enchantment));
        EnchantmentHelper.updateEnchantments(itemStack, mutable -> mutable.removeIf(holder -> {
            return getEnchantmentKey(holder)
                    .map(ResourceLocation::toString)
                    .filter(enchantment::equals)
                    .isPresent();
        }));
    }

    public static void removeEnchantments(ItemStack itemStack, String slot) {
        CompoundTag map = getTagElement(itemStack, "EnchantmentMapping");
        if (map != null) {
            Set<String> matchingEnchantments = map.getAllKeys().stream()
                    .filter(ench -> slot.equals(map.getString(ench)))
                    .collect(Collectors.toSet());

            EnchantmentHelper.updateEnchantments(itemStack, mutable -> mutable.removeIf(holder -> {
                return getEnchantmentKey(holder)
                        .map(ResourceLocation::toString)
                        .filter(matchingEnchantments::contains)
                        .isPresent();
            }));
            matchingEnchantments.forEach(map::remove);
        }
    }

    public static String getEnchantmentTooltip(Enchantment enchantment, int level, boolean clearFormatting) {
        return getEnchantmentTooltip(getHolder(enchantment), level, clearFormatting);
    }

    public static String getEnchantmentTooltip(Holder<Enchantment> enchantment, int level, boolean clearFormatting) {
        if (clearFormatting) {
            return ChatFormatting.stripFormatting(getEnchantmentName(enchantment, level));
        }

        return getEnchantmentName(enchantment, level);
    }

    public static String getEnchantmentName(Enchantment enchantment, int level) {
        return Enchantment.getFullname(getHolder(enchantment), level).getString();
    }

    public static String getEnchantmentName(Holder<Enchantment> enchantment, int level) {
        return Enchantment.getFullname(enchantment, level).getString();
    }

    public static String getEnchantmentDescription(Enchantment enchantment) {
        return getEnchantmentKey(enchantment)
                .map(key -> Util.makeDescriptionId("enchantment", key) + ".desc")
                .filter(I18n::exists)
                .map(I18n::get)
                .orElse(null);
    }

    public static String getEnchantmentDescription(Holder<Enchantment> enchantment) {
        return getEnchantmentKey(enchantment)
                .map(key -> Util.makeDescriptionId("enchantment", key) + ".desc")
                .filter(I18n::exists)
                .map(I18n::get)
                .orElse(null);
    }

    public static class EnchantmentRules {
        List<ItemStack> supportedItems;
        TagKey<Enchantment> exclusions;
        TagKey<Enchantment> additions;

        public EnchantmentRules(List<ItemStack> supportedItems, String additions, String exclusions) {
            this.supportedItems = supportedItems;
            ITagManager<Enchantment> tags = ForgeRegistries.ENCHANTMENTS.tags();
            this.additions = tags.createTagKey(ResourceLocation.fromNamespaceAndPath(TetraMod.MOD_ID, additions));
            this.exclusions = tags.createTagKey(ResourceLocation.fromNamespaceAndPath(TetraMod.MOD_ID, exclusions));

        }

        public boolean isApplicable(Enchantment enchantment) {
            ITagManager<Enchantment> tags = ForgeRegistries.ENCHANTMENTS.tags();
            boolean supported = supportedItems.stream().anyMatch(enchantment::isSupportedItem);
            return (supported || tags.getTag(additions).contains(enchantment)) && !tags.getTag(exclusions).contains(enchantment);
        }
    }

    private static List<ItemStack> items(Item... items) {
        return Arrays.stream(items).map(ItemStack::new).toList();
    }

    public static Holder<Enchantment> getHolder(Enchantment enchantment) {
        ResourceLocation key = requireEnchantmentKey(enchantment);
        return getRegistryLookup().getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, key));
    }

    public static Stream<Holder.Reference<Enchantment>> getRegisteredEnchantments() {
        return getRegistryLookup().listElements();
    }

    public static Optional<ResourceLocation> getEnchantmentKey(Enchantment enchantment) {
        Optional<ResourceLocation> key = Optional.ofNullable(ForgeRegistries.ENCHANTMENTS.getKey(enchantment));
        return key.isPresent() ? key : findEnchantmentKey(enchantment);
    }

    public static Optional<ResourceLocation> getEnchantmentKey(Holder<Enchantment> enchantment) {
        Optional<ResourceLocation> key = enchantment.unwrapKey().map(ResourceKey::location);
        return key.isPresent() ? key : getEnchantmentKey(enchantment.value());
    }

    private static Optional<ResourceLocation> findEnchantmentKey(Enchantment enchantment) {
        HolderLookup.RegistryLookup<Enchantment> lookup = CommonHooks.resolveLookup(Registries.ENCHANTMENT);
        if (lookup == null) {
            return Optional.empty();
        }

        return lookup.listElements()
                .filter(holder -> holder.value() == enchantment || holder.value().equals(enchantment))
                .findFirst()
                .map(holder -> holder.key().location());
    }

    private static ResourceLocation requireEnchantmentKey(Enchantment enchantment) {
        return getEnchantmentKey(enchantment)
                .orElseThrow(() -> new IllegalStateException("Unregistered enchantment: " + enchantment));
    }

    private static ResourceLocation requireEnchantmentKey(Holder<Enchantment> enchantment) {
        return getEnchantmentKey(enchantment)
                .orElseThrow(() -> new IllegalStateException("Unregistered enchantment: " + enchantment));
    }

    private static HolderLookup.RegistryLookup<Enchantment> getRegistryLookup() {
        return Objects.requireNonNull(CommonHooks.resolveLookup(Registries.ENCHANTMENT), "Enchantment registry lookup unavailable");
    }
}
