package se.mickelus.tetra.module.schematic;

import net.minecraft.world.item.ItemStack;
import se.mickelus.mutil.util.CastOptional;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.data.GlyphData;
import se.mickelus.tetra.module.data.ToolData;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@ParametersAreNonnullByDefault
public class OutcomePreview {
    public String moduleKey;
    public String variantName;
    public String variantKey;
    public String category;
    public int level;
    public GlyphData glyph;
    public ItemStack itemStack;

    public SchematicType type;

    public ToolData tools;
    public ItemStack[] materials;

    public OutcomePreview(String moduleKey, String variantKey, String variantName, String category, int level, GlyphData glyph, ItemStack itemStack,
            SchematicType type, ToolData tools, ItemStack[] materials) {
        this.moduleKey = moduleKey;
        this.variantKey = variantKey;
        this.variantName = variantName;
        this.category = category;
        this.level = level;
        this.glyph = glyph;
        this.itemStack = itemStack;
        this.type = type;
        this.tools = tools;
        this.materials = materials;
    }

    public boolean isApplied(ItemStack itemStack, String slot) {
        if (moduleKey != null) {
            // todo: implement for modules
            return false;
        }

        return CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .map(item -> item.getModuleFromSlot(itemStack, slot))
                .flatMap(module -> CastOptional.cast(module, ItemModuleMajor.class))
                .map(module -> module.getImprovementLevel(itemStack, variantKey) == level)
                .orElse(false);
    }

    public OutcomePreview clone() {
        return new OutcomePreview(moduleKey, variantKey, variantName, category, level, glyph, itemStack.copy(), type, tools, materials);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OutcomePreview preview = (OutcomePreview) o;
        return Objects.equals(moduleKey, preview.moduleKey)
                && Objects.equals(variantKey, preview.variantKey)
                && level == preview.level;
    }

    @Override
    public int hashCode() {
        return Objects.hash(moduleKey, variantKey);
    }
}
