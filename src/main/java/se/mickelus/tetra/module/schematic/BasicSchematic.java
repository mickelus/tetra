package se.mickelus.tetra.module.schematic;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.data.GlyphData;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;


public abstract class BasicSchematic extends BaseSchematic {

    private static final String nameSuffix = ".name";
    private static final String descriptionSuffix = ".description";
    private static final String slotSuffix = ".slot1";

    protected String key;
    protected ItemModule module;
    protected Item item;

    public BasicSchematic(String key, ItemModule module, Item item) {
        this.key = key;
        this.module = module;
        this.item = item;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getName() {
        return I18n.format(key + nameSuffix);
    }

    @Override
    public String getDescription(ItemStack itemStack) {
        return I18n.format(key + descriptionSuffix);
    }

    @Override
    public int getNumMaterialSlots() {
        return 1;
    }

    @Override
    public String getSlotName(final ItemStack itemStack, final int index) {
        return I18n.format(key + slotSuffix);
    }

    @Override
    public boolean isApplicableForItem(ItemStack itemStack) {
        return item.equals(itemStack.getItem());
    }

    @Override
    public boolean isApplicableForSlot(String slot, ItemStack targetStack) {
        return module.getSlot().equals(slot);
    }

    protected ItemModule removePreviousModule(final ItemStack itemStack) {
        IModularItem item = (IModularItem) itemStack.getItem();
        ItemModule previousModule = item.getModuleFromSlot(itemStack, module.getSlot());
        if (previousModule != null) {
            previousModule.removeModule(itemStack);
        }
        return previousModule;
    }

    @Override
    public SchematicType getType() {
        if (module instanceof ItemModuleMajor) {
            return SchematicType.major;
        } else {
            return SchematicType.minor;
        }
    }

    @Override
    public GlyphData getGlyph() {
        return module.getDefaultData().glyph;
    }
}
