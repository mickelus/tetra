package se.mickelus.tetra.module.schema;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.capabilities.CapabilityHelper;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.module.data.GlyphData;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;


public abstract class BasicSchema implements UpgradeSchema {

    private static final String nameSuffix = ".name";
    private static final String descriptionSuffix = ".description";
    private static final String slotSuffix = ".slot1";

    protected String key;
    protected ItemModule module;
    protected Item item;

    public BasicSchema(String key, ItemModule module, Item item) {
        this.key = key;
        this.module = module;
        this.item = item;

        ItemUpgradeRegistry.instance.registerSchema(this);
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
    public String getDescription() {
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

    @Override
    public boolean canApplyUpgrade(EntityPlayer player, ItemStack itemStack, ItemStack[] materials, String slot) {
        return isMaterialsValid(itemStack, materials)
                && !isIntegrityViolation(player, itemStack, materials, slot)
                && checkCapabilities(player, itemStack, materials);
    }
    @Override
    public boolean isIntegrityViolation(EntityPlayer player, ItemStack itemStack, final ItemStack[] materials, String slot) {
        ItemStack upgradedStack = applyUpgrade(itemStack, materials, false, module.getSlot(), null);
        return ItemModular.getIntegrityGain(upgradedStack) + ItemModular.getIntegrityCost(upgradedStack) < 0;
    }

    @Override
    public boolean checkCapabilities(EntityPlayer player, final ItemStack targetStack, final ItemStack[] materials) {
        return getRequiredCapabilities(targetStack, materials).stream()
                .allMatch(capability -> CapabilityHelper.getCapabilityLevel(player, capability) >= getRequiredCapabilityLevel(targetStack, materials, capability));
    }

    protected ItemModule removePreviousModule(final ItemStack itemStack) {
        ItemModular item = (ItemModular) itemStack.getItem();
        ItemModule previousModule = item.getModuleFromSlot(itemStack, module.getSlot());
        if (previousModule != null) {
            previousModule.removeModule(itemStack);
        }
        return previousModule;
    }

    @Override
    public SchemaType getType() {
        if (module instanceof ItemModuleMajor) {
            return SchemaType.major;
        } else {
            return SchemaType.minor;
        }
    }

    @Override
    public GlyphData getGlyph() {
        return module.getDefaultData().glyph;
    }
}
