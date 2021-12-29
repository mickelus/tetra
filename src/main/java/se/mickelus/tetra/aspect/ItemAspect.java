package se.mickelus.tetra.aspect;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ItemAspect {
    private static final Map<String, ItemAspect> map = new ConcurrentHashMap<>();

    public static final ItemAspect armor = get("armor");
    public static final ItemAspect armorFeet = get("armorFeet");
    public static final ItemAspect armorLegs = get("armorLegs");
    public static final ItemAspect armorChest = get("armorChest");
    public static final ItemAspect armorHead = get("armorHead");
    public static final ItemAspect edgedWeapon = get("edgedWeapon");
    public static final ItemAspect bluntWeapon = get("bluntWeapon");
    public static final ItemAspect pointyWeapon = get("pointyWeapon");
    public static final ItemAspect throwable = get("throwable");
    public static final ItemAspect blockBreaker = get("blockBreaker");
    public static final ItemAspect fishingRod = get("fishingRod");
    public static final ItemAspect breakable = get("breakable");
    public static final ItemAspect bow = get("bow");
    public static final ItemAspect wearable = get("wearable");
    public static final ItemAspect crossbow = get("crossbow");
    public static final ItemAspect vanishable = get("vanishable");


    private final String key;

    private ItemAspect(String key) {
        this.key = key;
    }

    public static ItemAspect get(String key) {
        return map.computeIfAbsent(key, k -> new ItemAspect(key));
    }

    public String getKey() {
        return key;
    }
}
