package se.mickelus.tetra.module.data;

import com.google.gson.JsonObject;
import net.minecraft.enchantment.Enchantment;

public class EnchantmentMapping {
    /**
     * Use the resource location for the enchantment in json files
     */
    public Enchantment enchantment;

    /**
     * The id of the improvement that the enchantment should be converted into
     */
    public String improvement;

    /**
     * Denotes if the enchantment should be extracted from items as they are converted to tetra items, and from enchantment books.
     * Destabilization effects that should reduce the efficiency of enchantments would have mapping entries with this set to false.
     */
    public boolean extract = true;

    /**
     * Denotes if the enchantment should be applied back to the item when it's assembled, e.g. sharpness and sweeping would have this set
     * to false as that's handled internally in for modular items.
     */
    public boolean apply = true;

    public float multiplier = 1;


    public JsonObject toJson() {
        JsonObject result = new JsonObject();
        result.addProperty("enchantment", enchantment.delegate.name().toString());
        result.addProperty("improvement", improvement);

        if (!extract) {
            result.addProperty("extract", extract);
        }

        if (!apply) {
            result.addProperty("apply", apply);
        }

        if (multiplier != 1) {
            result.addProperty("multiplier", multiplier);
        }

        return result;
    }
}
