package se.mickelus.tetra.items;

import java.util.HashMap;
import java.util.Map;

public class ItemColors {
    private static Map<String, Integer> colors = new HashMap<>();

    public static int oak               = define(0xdfa940, "oak");
    public static int oak_glyph         = define(0x9d804e, "oak_glyph");
    public static int spruce            = define(0x694f2f, "spruce");
    public static int spruce_glyph      = define(0x694f2f, "spruce_glyph");
    public static int birch             = define(0xf5dd8c, "birch");
    public static int birch_glyph       = define(0xc8b77d, "birch_glyph");
    public static int jungle            = define(0xb07b58, "jungle");
    public static int jungle_glyph      = define(0x996e4c, "jungle_glyph");
    public static int acacia            = define(0xac5d33, "acacia");
    public static int acacia_glyph      = define(0xac5d33, "acacia_glyph");
    public static int dark_oak          = define(0x3c2712, "dark_oak");
    public static int dark_oak_glyph    = define(0x3c2712, "dark_oak_glyph");
    public static int cobblestone       = define(0xffffff, "cobblestone");
    public static int cobblestone_glyph = define(0x9a9a9a, "cobblestone_glyph");
    public static int stone             = define(0xaaaaaa, "stone");
    public static int stone_glyph       = define(0x9a9a9a, "stone_glyph");
    public static int granite           = define(0xd38b70, "granite");
    public static int granite_glyph     = define(0x8a6658, "granite_glyph");
    public static int diorite           = define(0xffffff, "diorite");
    public static int diorite_glyph     = define(0xb1b1b3, "diorite_glyph");
    public static int andesite          = define(0xaaaaae, "andesite");
    public static int andesite_glyph    = define(0x79797a, "andesite_glyph");
    public static int flint             = define(0x676767, "flint");
    public static int flint_glyph       = define(0x343434, "flint_glyph");
    public static int iron              = define(0xffffff, "iron");
    public static int iron_glyph        = define(0xd8d8d8, "iron_glyph");
    public static int gold              = define(0xfffb46, "gold");
    public static int gold_glyph        = define(0xeaee57, "gold_glyph");
    public static int diamond           = define(0x2bffee, "diamond");
    public static int diamond_glyph     = define(0x33ebcb, "diamond_glyph");
    public static int obsidian          = define(0x44395e, "obsidian");
    public static int obsidian_glyph    = define(0x3c3056, "obsidian_glyph");
    public static int copper            = define(0xdf7646, "copper");
    public static int copper_glyph      = define(0xdf7646, "copper_glyph");
    public static int tin               = define(0xb3c9c1, "tin");
    public static int tin_glyph         = define(0xb3c9c1, "tin_glyph");
    public static int silver            = define(0xccc2ff, "silver");
    public static int silver_glyph      = define(0xd6f6ff, "silver_glyph");
    public static int nickel            = define(0xc2d3a6, "nickel");
    public static int nickel_glyph      = define(0xc2d3a6, "nickel_glyph");
    public static int lead              = define(0x9781b3, "lead");
    public static int lead_glyph        = define(0x897a9b, "lead_glyph");
    public static int bronze            = define(0xb36827, "bronze");
    public static int bronze_glyph      = define(0xb2712c, "bronze_glyph");
    public static int electrum          = define(0xe4da4c, "electrum");
    public static int electrum_glyph    = define(0xe4da4c, "electrum_glyph");
    public static int steel             = define(0x999999, "steel");
    public static int steel_glyph       = define(0x999999, "steel_glyph");
    public static int thaumium          = define(0x4d3984, "thaumium");
    public static int thaumium_glyph    = define(0x4d3984, "thaumium_glyph");
    public static int ironwood          = define(0x625c58, "ironwood");
    public static int ironwood_glyph    = define(0x5ea917, "ironwood_glyph");
    public static int steeleaf          = define(0x458430, "steeleaf");
    public static int steeleaf_glyph    = define(0x458430, "steeleaf_glyph");
    public static int fierymetal        = define(0xfa9f00, "fierymetal");
    public static int fierymetal_glyph  = define(0xfa9f00, "fierymetal_glyph");
    public static int knightmetal       = define(0xb8cf9c, "knightmetal");
    public static int knightmetal_glyph = define(0xb8cf9c, "knightmetal_glyph");
    public static int prismarine        = define(0x8cffdb, "prismarine");
    public static int prismarine_glyph  = define(0x73b5aa, "prismarine_glyph");

    public static int inherit           = define(0x000000, "inherit");

    public static int define(int value, String color) {
        colors.put(color, value);
        return value;
    }

    public static int get(String color) {
        return colors.get(color);
    }

    public static boolean exists(String color) {
        return colors.containsKey(color);
    }
}
