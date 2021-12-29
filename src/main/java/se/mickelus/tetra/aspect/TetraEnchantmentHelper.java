package se.mickelus.tetra.aspect;

import com.google.common.collect.HashBiMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.tuple.Pair;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TetraEnchantmentHelper {
    private static final Map<ItemAspect, EnchantmentRules> aspectMap = HashBiMap.create();
    static {
        aspectMap.put(ItemAspect.armor, new EnchantmentRules(EnchantmentType.ARMOR, "additions/armor", "exclusions/armor"));
        aspectMap.put(ItemAspect.armorFeet, new EnchantmentRules(EnchantmentType.ARMOR_FEET, "additions/armor_feet", "exclusions/armor_feet"));
        aspectMap.put(ItemAspect.armorLegs, new EnchantmentRules(EnchantmentType.ARMOR_LEGS, "additions/armor_legs", "exclusions/armor_legs"));
        aspectMap.put(ItemAspect.armorChest, new EnchantmentRules(EnchantmentType.ARMOR_CHEST, "additions/armor_chest", "exclusions/armor_chest"));
        aspectMap.put(ItemAspect.armorHead, new EnchantmentRules(EnchantmentType.ARMOR_HEAD, "additions/armor_head", "exclusions/armor_head"));
        aspectMap.put(ItemAspect.edgedWeapon, new EnchantmentRules(EnchantmentType.WEAPON, "additions/edged_weapon", "exclusions/edged_weapon"));
        aspectMap.put(ItemAspect.bluntWeapon, new EnchantmentRules(EnchantmentType.WEAPON, "additions/blunt_weapon", "exclusions/blunt_weapon"));
        aspectMap.put(ItemAspect.pointyWeapon, new EnchantmentRules(EnchantmentType.TRIDENT, "additions/pointy_weapon", "exclusions/pointy_weapon"));
        aspectMap.put(ItemAspect.throwable, new EnchantmentRules(null, "additions/throwable", "exclusions/throwable"));
        aspectMap.put(ItemAspect.blockBreaker, new EnchantmentRules(EnchantmentType.DIGGER, "additions/block_breaker", "exclusions/block_breaker"));
        aspectMap.put(ItemAspect.fishingRod, new EnchantmentRules(EnchantmentType.FISHING_ROD, "additions/fishing_rod", "exclusions/fishing_rod"));
        aspectMap.put(ItemAspect.breakable, new EnchantmentRules(EnchantmentType.BREAKABLE, "additions/breakable", "exclusions/breakable"));
        aspectMap.put(ItemAspect.bow, new EnchantmentRules(EnchantmentType.BOW, "additions/bow", "exclusions/bow"));
        aspectMap.put(ItemAspect.wearable, new EnchantmentRules(EnchantmentType.WEARABLE, "additions/wearable", "exclusions/wearable"));
        aspectMap.put(ItemAspect.crossbow, new EnchantmentRules(EnchantmentType.CROSSBOW, "additions/crossbow", "exclusions/crossbow"));
        aspectMap.put(ItemAspect.vanishable, new EnchantmentRules(EnchantmentType.VANISHABLE, "additions/vanishable", "exclusions/vanishable"));
    }

    public static boolean isApplicableForAspects(Enchantment enchantment, boolean fromTable, Map<ItemAspect, Integer> aspects) {
        int requiredLevel = fromTable ? 2 : 1;

        return aspects.entrySet().stream()
                .filter(entry -> entry.getValue() >= requiredLevel)
                .filter(entry -> aspectMap.containsKey(entry.getKey()))
                .anyMatch(entry -> aspectMap.get(entry.getKey()).isApplicable(enchantment));
    }

    @Nullable
    public static EnchantmentType getEnchantmentType(ItemAspect aspect) {
        return aspectMap.get(aspect).type;
    }

    public static ItemStack removeAllEnchantments(ItemStack itemStack) {
        itemStack.removeChildTag("Enchantments");
        itemStack.removeChildTag("StoredEnchantments");
        itemStack.removeChildTag("EnchantmentMapping");

        IModularItem.updateIdentifier(itemStack);

        return itemStack;
    }

    public static ItemStack transferReplacementEnchantments(ItemStack original, ItemStack replacementStack) {
        Optional.ofNullable(original.getTag())
                .map(tag -> tag.getList("Enchantments", Constants.NBT.TAG_COMPOUND))
                .filter(enchantments -> enchantments.size() > 0)
                .ifPresent(enchantments -> {
                    replacementStack.getOrCreateTag().put("Enchantments", enchantments.copy());
                    mapEnchantments(replacementStack);
                });

        return replacementStack;
    }

    public static void applyEnchantment(ItemStack itemStack, String slot, Enchantment enchantment, int level) {
        itemStack.addEnchantment(enchantment, level);
        mapEnchantment(itemStack, slot, enchantment);
    }

    public static void mapEnchantment(ItemStack itemStack, String slot, Enchantment enchantment) {
        CompoundNBT map = itemStack.getOrCreateChildTag("EnchantmentMapping");
        map.putString(Registry.ENCHANTMENT.getKey(enchantment).toString(), slot);
    }

    public static void mapEnchantments(ItemStack itemStack) {
        CompoundNBT mappings = itemStack.getOrCreateChildTag("EnchantmentMapping");
        Map<String, String> mapped = Optional.of(mappings)
                .map(CompoundNBT::keySet)
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .collect(Collectors.toMap(Function.identity(), mappings::getString));

        Map<String, Integer> capacity = Arrays.stream(((IModularItem) itemStack.getItem()).getMajorModules(itemStack))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(ItemModule::getSlot, module -> module.getMagicCapacity(itemStack)));


        List<Pair<String, Integer>> unmapped = Optional.ofNullable(itemStack.getTag())
                .map(tag -> tag.getList("Enchantments", Constants.NBT.TAG_COMPOUND))
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .map(nbt -> ((CompoundNBT) nbt))
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
    public static Pair<Enchantment, Integer> getEnchantment(CompoundNBT nbt) {
        return Registry.ENCHANTMENT.getOptional(new ResourceLocation(nbt.getString("id")))
                .map(enchantment -> Pair.of(enchantment, nbt.getInt("lvl")))
                .orElse(null);
    }

    public static int getEnchantmentCapacityCost(Enchantment enchantment, int level) {
        return -(enchantment.getMaxEnchantability(level) + enchantment.getMinEnchantability(level)) / 2;
    }

    public static void removeEnchantments(ItemStack itemStack, String slot) {
        CompoundNBT map = itemStack.getChildTag("EnchantmentMapping");
        ListNBT enchantments = Optional.ofNullable(itemStack.getTag())
                .map(tag -> tag.getList("Enchantments", Constants.NBT.TAG_COMPOUND))
                .orElse(null);

        if (map != null && enchantments != null) {
            Set<String> matchingEnchantments = map.keySet().stream()
                    .filter(ench -> slot.equals(map.getString(ench)))
                    .collect(Collectors.toSet());

            enchantments.removeIf(nbt -> matchingEnchantments.contains(((CompoundNBT) nbt).getString("id")));
            matchingEnchantments.forEach(map::remove);
        }
    }

    public static String getEnchantmentTooltip(Enchantment enchantment, int level, boolean clearFormatting) {
        if (clearFormatting) {
            return TextFormatting.getTextWithoutFormattingCodes(getEnchantmentName(enchantment, level));
        }

        return getEnchantmentName(enchantment, level);
    }

    public static String getEnchantmentName(Enchantment enchantment, int level) {
        String name = I18n.format(enchantment.getName());

        if (level != 1 || enchantment.getMaxLevel() != 1) {
            name += " " + I18n.format("enchantment.level." + level);
        }

        return name;
    }

    public static String getEnchantmentDescription(Enchantment enchantment) {
        return Optional.of(enchantment.getName() + ".desc")
                .filter(I18n::hasKey)
                .map(I18n::format)
                .orElse(null);
    }

    static class EnchantmentRules {
        EnchantmentType type;
        ResourceLocation exclusions;
        ResourceLocation additions;

        public EnchantmentRules(@Nullable EnchantmentType type, String exclusions, String additions) {
            this.type = type;
            this.exclusions = new ResourceLocation(TetraMod.MOD_ID, exclusions);
            this.additions = new ResourceLocation(TetraMod.MOD_ID, additions);
        }

        public boolean isApplicable(Enchantment enchantment) {
            Set<ResourceLocation> tags = enchantment.getTags();
            return ((type != null && type.equals(enchantment.type)) || tags.contains(additions)) && !tags.contains(exclusions);
        }
    }
}
