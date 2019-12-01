package se.mickelus.tetra.blocks.geode;

import com.google.common.collect.ImmutableSet;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.TetraItem;
import se.mickelus.tetra.items.TetraItemGroup;

import java.util.Set;

public class PristineLapisItem extends TetraItem {
    private static final String unlocalizedName = "pristine_lapis";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static PristineLapisItem instance;

    public PristineLapisItem() {
        super(new Properties().group(TetraItemGroup.instance));
        setRegistryName(unlocalizedName);
    }
}
