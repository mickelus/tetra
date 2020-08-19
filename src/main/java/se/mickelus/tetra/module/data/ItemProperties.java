package se.mickelus.tetra.module.data;

public class ItemProperties {
    /**
     * The durability that the module provides, this is what vanilla calls item damage. A higher value makes the item
     * last longer before it needs repairs. Swords lose one point of durability when hitting entities and two points
     * when destroying blocks, for tools it's the opposite.
     */
    public int durability = 0;

    /**
     * Works differently for modules and improvements.
     * For modules: Multiplies the durability of the entire item and multiplies the amount of durability gained from each repair.
     * For improvements: Multiplies the durability of the module.
     */
    public float durabilityMultiplier = 1;

    /**
     * Integrity is tetras way of balancing items. Some modules provide integrity while some have an integrity cost, if
     * an upgrade would cause the cost to exceed the available integrity the upgrade would not be possible. A negative
     * value represents an integrity cost while positive values cause the module to provide integrity.
     */
    public int integrity = 0;

    public int integrityUsage = 0;

    /**
     * Multiplies the flat integrity of the item. Works the same way as the multipliers for damage and attack speed. As
     * integrity is an integer value the multiplier may not always have effect.
     */
    public float integrityMultiplier = 1;

    public static ItemProperties merge(ItemProperties a, ItemProperties b) {
        if (a == null) {
            return b;
        } else if (b == null) {
            return a;
        }

        ItemProperties result = new ItemProperties();
        result.durability = a.durability + b.durability;
        result.durabilityMultiplier = a.durabilityMultiplier * b.durabilityMultiplier;

        // todo: this is a bit hacky and shouldn't be in the merge function
        if (a.integrity < 0) {
            result.integrityUsage -= a.integrity;
        } else {
            result.integrity += a.integrity;
        }

        if (b.integrity < 0) {
            result.integrityUsage -= b.integrity;
        } else {
            result.integrity += b.integrity;
        }
        result.integrityUsage += a.integrityUsage + b.integrityUsage;

        return result;
    }

    public ItemProperties multiply(float factor) {
        ItemProperties result = new ItemProperties();
        result.durability = Math.round(durability * factor);
        if (durabilityMultiplier != 1) {
            result.durabilityMultiplier = durabilityMultiplier * factor;
        }

        result.integrity = Math.round(integrity * factor);
        result.integrityUsage = Math.round(integrityUsage * factor);

        if (integrityMultiplier != 1) {
            result.integrityMultiplier = integrityMultiplier * factor;
        }

        return result;
    }
}
