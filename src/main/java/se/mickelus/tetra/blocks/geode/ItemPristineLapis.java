package se.mickelus.tetra.blocks.geode;

import com.google.common.collect.ImmutableSet;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.TetraItem;
import se.mickelus.tetra.items.TetraItemGroup;

import java.util.Set;

public class ItemPristineLapis extends TetraItem {
    private static final String unlocalizedName = "pristine_lapis";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ItemPristineLapis instance;

    public ItemPristineLapis() {
        super(new Properties().group(TetraItemGroup.instance));
        setRegistryName(unlocalizedName);
    }

    @Override
    public Set<ResourceLocation> getTags() {
        return ImmutableSet.of(new ResourceLocation("gems/lapis"));
    }
}
