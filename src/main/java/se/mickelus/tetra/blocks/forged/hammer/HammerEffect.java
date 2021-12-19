package se.mickelus.tetra.blocks.forged.hammer;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import se.mickelus.tetra.items.forged.CombustionChamberItem;
import se.mickelus.tetra.items.forged.InsulatedPlateItem;
import se.mickelus.tetra.items.forged.LubricantDispenser;
import se.mickelus.tetra.items.forged.PlanarStabilizerItem;

import java.util.Arrays;

public enum HammerEffect implements StringRepresentable {

    efficient(InsulatedPlateItem.instance),
    power(CombustionChamberItem.instance),
    precise(PlanarStabilizerItem.instance),
    reliable(LubricantDispenser.instance);

    private final Item item;

    HammerEffect(Item item) {
        this.item = item;
    }

    public static HammerEffect fromItem(Item item) {
        return Arrays.stream(HammerEffect.values())
                .filter(val -> val.item.equals(item))
                .findFirst()
                .orElse(null);
    }

    public Item getItem() {
        return item;
    }

    @Override
    public String getSerializedName() {
        return toString().toLowerCase();
    }
}
