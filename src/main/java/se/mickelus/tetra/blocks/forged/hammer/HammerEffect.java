package se.mickelus.tetra.blocks.forged.hammer;

import net.minecraft.item.Item;
import net.minecraft.util.IStringSerializable;
import se.mickelus.tetra.items.forged.CombustionChamberItem;
import se.mickelus.tetra.items.forged.InsulatedPlateItem;
import se.mickelus.tetra.items.forged.PlanarStabilizerItem;

import java.util.Arrays;

public enum HammerEffect implements IStringSerializable {

    efficient(InsulatedPlateItem.instance),
    power(CombustionChamberItem.instance),
    precise(PlanarStabilizerItem.instance);
//    reliable();

    private Item item;

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
    public String getString() {
        return toString().toLowerCase();
    }
}
