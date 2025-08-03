package se.mickelus.tetra.util;

import net.minecraft.client.resources.language.I18n;

import java.util.function.Supplier;

public class StringHelper {
    private static final Supplier<String>[] units = new Supplier[]{
        () -> "",
        () -> I18n.get("enchantment.level.1"),
        () -> I18n.get("enchantment.level.2"),
        () -> I18n.get("enchantment.level.3"),
        () -> I18n.get("enchantment.level.4"),
        () -> I18n.get("enchantment.level.5"),
        () -> I18n.get("enchantment.level.6"),
        () -> I18n.get("enchantment.level.7"),
        () -> I18n.get("enchantment.level.8"),
        () -> I18n.get("enchantment.level.9")
    };
    private static final Supplier<String>[] tens = new Supplier[]{
        () -> "",
        () -> I18n.get("enchantment.level.10"),
        () -> I18n.get("enchantment.level.20"),
        () -> I18n.get("enchantment.level.30"),
        () -> I18n.get("enchantment.level.40"),
        () -> I18n.get("enchantment.level.50"),
        () -> I18n.get("enchantment.level.60"),
        () -> I18n.get("enchantment.level.70"),
        () -> I18n.get("enchantment.level.80"),
        () -> I18n.get("enchantment.level.90")
    };
    private static final Supplier<String>[] hundreds = new Supplier[]{
        () -> "",
        () -> I18n.get("enchantment.level.100"),
        () -> I18n.get("enchantment.level.200"),
        () -> I18n.get("enchantment.level.300"),
        () -> I18n.get("enchantment.level.400"),
        () -> I18n.get("enchantment.level.500"),
        () -> I18n.get("enchantment.level.600"),
        () -> I18n.get("enchantment.level.700"),
        () -> I18n.get("enchantment.level.800"),
        () -> I18n.get("enchantment.level.900")
    };

    public static String toRoman(int value) {
        if (value < 1 || value > 3999) {
            throw new IllegalArgumentException("Value must be between 1 and 3999");
        }

        StringBuilder result = new StringBuilder();
        if (value >= 1000) {
            String string = I18n.get("enchantment.level.1000");
            while (value >= 1000) {
                result.append(string);
                value -= 1000;
            }
        }

        result.append(hundreds[(value % 1000) / 100].get());
        result.append(tens[(value % 100) / 10].get());
        result.append(units[value % 10].get());

        return result.toString();
    }
}
