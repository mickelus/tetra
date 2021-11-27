package se.mickelus.tetra.module.data;

import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.module.Priority;
import se.mickelus.tetra.util.Filter;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public class ModuleData {
    /**
     * The slots that this module can go into. Has to contain at least one value.
     */
    public String[] slots = new String[0];

    /**
     * Suffixes, used when modules should have different keys depending on slot (e.g. pickaxe head keys end with
     * "_left" or "_right" so that different textures can be used depending on the slot. If there's more than one slot then this has to have
     * the same length as the slots field.
     */
    public String[] slotSuffixes = new String[0];

    public ResourceLocation type;
    public boolean replace = false;

    public Priority renderLayer = Priority.BASE;

    public ResourceLocation tweakKey;
    public ResourceLocation[] improvements = new ResourceLocation[0];
    public VariantData[] variants = new VariantData[0];

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Non-configurable stuff below
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static final ModuleData defaultValues = new ModuleData();

    public static void copyFields(ModuleData from, ModuleData to) {
        to.slots = Stream.concat(Arrays.stream(to.slots), Arrays.stream(from.slots))
                .distinct()
                .toArray(String[]::new);

        to.slotSuffixes = Stream.concat(Arrays.stream(to.slotSuffixes), Arrays.stream(from.slotSuffixes))
                .distinct()
                .toArray(String[]::new);

        if (from.type != defaultValues.type) {
            to.type = from.type;
        }

        if (!Objects.equals(from.tweakKey, defaultValues.tweakKey)) {
            to.tweakKey = from.tweakKey;
        }

        if (from.renderLayer != defaultValues.renderLayer) {
            to.renderLayer = from.renderLayer;
        }

        to.improvements = Stream.concat(Arrays.stream(to.improvements), Arrays.stream(from.improvements))
                .distinct()
                .toArray(ResourceLocation[]::new);

        to.variants = Stream.concat(Arrays.stream(to.variants), Arrays.stream(from.variants))
                .toArray(VariantData[]::new);
    }

    public ModuleData shallowCopy() {
        ModuleData copy = new ModuleData();
        copyFields(this, copy);

        return copy;
    }
}
