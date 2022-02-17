package se.mickelus.tetra.aspect;

import com.google.common.collect.HashBiMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.apache.commons.lang3.tuple.Pair;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TetraEnchantmentHelper {
    private static final Map<ItemAspect, EnchantmentRules> aspectMap = HashBiMap.create();

    static {
        aspectMap.put(ItemAspect.armor, new EnchantmentRules(EnchantmentCategory.ARMOR, "additions/armor", "exclusions/armor"));
        aspectMap.put(ItemAspect.armorFeet, new EnchantmentRules(EnchantmentCategory.ARMOR_FEET, "additions/armor_feet", "exclusions/armor_feet"));
        aspectMap.put(ItemAspect.armorLegs, new EnchantmentRules(EnchantmentCategory.ARMOR_LEGS, "additions/armor_legs", "exclusions/armor_legs"));
        aspectMap.put(ItemAspect.armorChest, new EnchantmentRules(EnchantmentCategory.ARMOR_CHEST, "additions/armor_chest", "exclusions/armor_chest"));
        aspectMap.put(ItemAspect.armorHead, new EnchantmentRules(EnchantmentCategory.ARMOR_HEAD, "additions/armor_head", "exclusions/armor_head"));
        aspectMap.put(ItemAspect.edgedWeapon, new EnchantmentRules(EnchantmentCategory.WEAPON, "additions/edged_weapon", "exclusions/edged_weapon"));
        aspectMap.put(ItemAspect.bluntWeapon, new EnchantmentRules(EnchantmentCategory.WEAPON, "additions/blunt_weapon", "exclusions/blunt_weapon"));
        aspectMap.put(ItemAspect.pointyWeapon, new EnchantmentRules(EnchantmentCategory.TRIDENT, "additions/pointy_weapon", "exclusions/pointy_weapon"));
        aspectMap.put(ItemAspect.throwable, new EnchantmentRules(null, "additions/throwable", "exclusions/throwable"));
        aspectMap.put(ItemAspect.blockBreaker, new EnchantmentRules(EnchantmentCategory.DIGGER, "additions/block_breaker", "exclusions/block_breaker"));
        aspectMap.put(ItemAspect.fishingRod, new EnchantmentRules(EnchantmentCategory.FISHING_ROD, "additions/fishing_rod", "exclusions/fishing_rod"));
        aspectMap.put(ItemAspect.breakable, new EnchantmentRules(EnchantmentCategory.BREAKABLE, "additions/breakable", "exclusions/breakable"));
        aspectMap.put(ItemAspect.bow, new EnchantmentRules(EnchantmentCategory.BOW, "additions/bow", "exclusions/bow"));
        aspectMap.put(ItemAspect.wearable, new EnchantmentRules(EnchantmentCategory.WEARABLE, "additions/wearable", "exclusions/wearable"));
        aspectMap.put(ItemAspect.crossbow, new EnchantmentRules(EnchantmentCategory.CROSSBOW, "additions/crossbow", "exclusions/crossbow"));
        aspectMap.put(ItemAspect.vanishable, new EnchantmentRules(EnchantmentCategory.VANISHABLE, "additions/vanishable", "exclusions/vanishable"));
    }

    public static void registerMapping(ItemAspect aspect, @Nullable EnchantmentCategory category, String additions, String exclusions) {
        registerMapping(aspect, new EnchantmentRules(category, additions, exclusions));
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

    @Nullable
    public static EnchantmentCategory getEnchantmentCategory(ItemAspect aspect) {
        return aspectMap.get(aspect).category;
    }

    public static ItemStack removeAllEnchantments(ItemStack itemStack) {
        itemStack.removeTagKey("Enchantments");
        itemStack.removeTagKey("StoredEnchantments");
        itemStack.removeTagKey("EnchantmentMapping");

        IModularItem.updateIdentifier(itemStack);

        return itemStack;
    }

    public static ItemStack transferReplacementEnchantments(ItemStack original, ItemStack replacementStack) {
        Optional.ofNullable(original.getTag())
                .map(tag -> tag.getList("Enchantments", Tag.TAG_COMPOUND))
                .filter(enchantments -> enchantments.size() > 0)
                .ifPresent(enchantments -> {
                    replacementStack.getOrCreateTag().put("Enchantments", enchantments.copy());
                    mapEnchantments(replacementStack);
                });

        return replacementStack;
    }

    public static void applyEnchantment(ItemStack itemStack, String slot, Enchantment enchantment, int level) {
        itemStack.enchant(enchantment, level);
        mapEnchantment(itemStack, slot, enchantment);
    }

    public static void mapEnchantment(ItemStack itemStack, String slot, Enchantment enchantment) {
        CompoundTag map = itemStack.getOrCreateTagElement("EnchantmentMapping");
        map.putString(Registry.ENCHANTMENT.getKey(enchantment).toString(), slot);
    }

    public static void mapEnchantments(ItemStack itemStack) {
        CompoundTag mappings = itemStack.getOrCreateTagElement("EnchantmentMapping");
        Map<String, String> mapped = Optional.of(mappings)
                .map(CompoundTag::getAllKeys)
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Function.identity(), mappings::getString));

        Map<String, Integer> capacity = Arrays.stream(((IModularItem) itemStack.getItem()).getMajorModules(itemStack))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(ItemModule::getSlot, module -> module.getMagicCapacity(itemStack)));

        List<Pair<String, Integer>> unmapped = Optional.of(itemStack.getEnchantmentTags())
                .stream()
                .flatMap(Collection::stream)
                .map(nbt -> ((CompoundTag) nbt))
                .map(nbt -> Pair.of(nbt.getString("id"), nbt.getInt("lvl")))
                .filter(pair -> !mapped.containsKey(pair.getKey()))
                .collect(Collectors.toList());

        ItemModuleMajor[] modules = ((IModularItem) itemStack.getItem()).getMajorModules(itemStack);
        unmapped.forEach(pair -> {
            Enchantment enchantment = Registry.ENCHANTMENT.getOptional(new ResourceLocation(pair.getKey())).orElse(null);
            if (enchantment != null) {
                Arrays.stream(modules)
                        .filter(Objects::nonNull)
                        .filter(module -> module.acceptsEnchantment(itemStack, enchantment, false))
                        .map(ItemModule::getSlot)
                        .max(Comparator.comparing(slot -> capacity.getOrDefault(slot, 0)))
                        .ifPresent(slot -> {
                            mapEnchantment(itemStack, slot, enchantment);
                            int cost = getEnchantmentCapacityCost(enchantment, pair.getRight());
                            capacity.merge(slot, cost, Integer::sum);
                        });
            }
        });
    }

    @Nullable
    public static Pair<Enchantment, Integer> getEnchantment(CompoundTag nbt) {
        return Registry.ENCHANTMENT.getOptional(new ResourceLocation(nbt.getString("id")))
                .map(enchantment -> Pair.of(enchantment, nbt.getInt("lvl")))
                .orElse(null);
    }

    public static int getEnchantmentCapacityCost(Enchantment enchantment, int level) {
        return -(enchantment.getMaxCost(level) + enchantment.getMinCost(level)) / 2;
    }

    public static void removeEnchantment(ItemStack itemStack, Enchantment enchantment) {
        Optional.ofNullable(Registry.ENCHANTMENT.getKey(enchantment))
                .ifPresent(enchantmentKey -> removeEnchantment(itemStack, enchantmentKey.toString()));
    }

    public static void removeEnchantment(ItemStack itemStack, String enchantment) {
        Optional.ofNullable(itemStack.getTagElement("EnchantmentMapping"))
                .ifPresent(map -> map.remove(enchantment));
        Optional.ofNullable(itemStack.getTag())
                .map(tag -> tag.getList("Enchantments", Tag.TAG_COMPOUND))
                .ifPresent(enchantments -> enchantments.removeIf(nbt -> enchantment.equals(((CompoundTag) nbt).getString("id"))));
    }

    public static void removeEnchantments(ItemStack itemStack, String slot) {
        CompoundTag map = itemStack.getTagElement("EnchantmentMapping");
        ListTag enchantments = Optional.ofNullable(itemStack.getTag())
                .map(tag -> tag.getList("Enchantments", Tag.TAG_COMPOUND))
                .orElse(null);

        if (map != null && enchantments != null) {
            Set<String> matchingEnchantments = map.getAllKeys().stream()
                    .filter(ench -> slot.equals(map.getString(ench)))
                    .collect(Collectors.toSet());

            enchantments.removeIf(nbt -> matchingEnchantments.contains(((CompoundTag) nbt).getString("id")));
            matchingEnchantments.forEach(map::remove);
        }
    }

    public static String getEnchantmentTooltip(Enchantment enchantment, int level, boolean clearFormatting) {
        if (clearFormatting) {
            return ChatFormatting.stripFormatting(getEnchantmentName(enchantment, level));
        }

        return getEnchantmentName(enchantment, level);
    }

    public static String getEnchantmentName(Enchantment enchantment, int level) {
        return enchantment.getFullname(level).getString();
    }

    public static String getEnchantmentDescription(Enchantment enchantment) {
        return Optional.of(enchantment.getDescriptionId() + ".desc")
                .filter(I18n::exists)
                .map(I18n::get)
                .orElse(null);
    }

    static class EnchantmentRules {
        EnchantmentCategory category;
        ResourceLocation exclusions;
        ResourceLocation additions;

        public EnchantmentRules(@Nullable EnchantmentCategory category, String exclusions, String additions) {
            this.category = category;
            this.exclusions = new ResourceLocation(TetraMod.MOD_ID, exclusions);
            this.additions = new ResourceLocation(TetraMod.MOD_ID, additions);
        }

        public boolean isApplicable(Enchantment enchantment) {
            Set<ResourceLocation> tags = enchantment.getTags();
            return ((category != null && category.equals(enchantment.category)) || tags.contains(additions)) && !tags.contains(exclusions);
        }
    }
}
