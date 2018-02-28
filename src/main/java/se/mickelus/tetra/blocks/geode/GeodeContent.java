package se.mickelus.tetra.blocks.geode;

import net.minecraft.item.Item;

public class GeodeContent {
    public Item item;
    public int maxCount;
    public int weight;

    public GeodeContent(Item item, int maxCount, int weight) {
        this.item = item;
        this.maxCount = maxCount;
        this.weight = weight;
    }
}
