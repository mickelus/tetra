package se.mickelus.tetra.blocks.geode;

import net.minecraft.item.Item;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.TetraItemGroup;
import se.mickelus.tetra.items.TetraItem;
import se.mickelus.tetra.network.PacketHandler;

public class ItemPristineEmerald extends TetraItem {
    private static final String unlocalizedName = "pristine_emerald";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ItemPristineEmerald instance;

    public ItemPristineEmerald() {
        super(new Item.Properties()
                .group(TetraItemGroup.getInstance()));
        setRegistryName(unlocalizedName);
    }

    @Override
    public void init(PacketHandler packetHandler) {
        // todo 1.14: find additional forge tags
        // OreDictionary.registerOre("gemEmerald", this);
    }
}
