package se.mickelus.tetra.module.schema;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.capabilities.CapabilityHelper;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.data.GlyphData;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;


public class RemoveSchema implements UpgradeSchema {
    private static final String nameSuffix = ".name";
    private static final String descriptionSuffix = ".description";

    private String key = "remove_schema";

    private ItemModular item;
    private String slot;

    private GlyphData glyph = new GlyphData("textures/gui/workbench.png", 52, 32);

    public RemoveSchema(ItemModular item, String slot) {
        this.item = item;
        this.slot = slot;

        ItemUpgradeRegistry.instance.registerSchema(this);
    }

    public static void registerRemoveSchemas(ItemModular item) {
        Stream.concat(Arrays.stream(item.getMajorModuleKeys()), Arrays.stream(item.getMinorModuleKeys()))
                .filter(slot -> !item.isModuleRequired(slot))
                .forEach(slot -> new RemoveSchema(item, slot));
    }

    @Override
    public String getKey() {
        return key + "/" + item.getUnlocalizedName() + "/" + slot;
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
        return 0;
    }

    @Override
    public String getSlotName(final ItemStack itemStack, final int index) {
        return "";
    }

    @Override
    public int getRequiredQuantity(ItemStack itemStack, int index, ItemStack materialStack) {
        return 0;
    }

    @Override
    public boolean acceptsMaterial(final ItemStack itemStack, final int index, final ItemStack materialStack) {
        return false;
    }

    @Override
    public boolean isApplicableForItem(ItemStack itemStack) {
        return item.getClass().isInstance(itemStack.getItem());
    }

    @Override
    public boolean isApplicableForSlot(String slot, ItemStack targetStack) {
        return this.slot.equals(slot) && item.getModuleFromSlot(targetStack, this.slot) != null;
    }

    @Override
    public boolean canApplyUpgrade(EntityPlayer player, ItemStack itemStack, ItemStack[] materials, String slot) {
        return !isIntegrityViolation(player, itemStack, materials, slot)
                && checkCapabilities(player, itemStack, materials);
    }

    @Override
    public boolean isMaterialsValid(ItemStack itemStack, ItemStack[] materials) {
        return true;
    }

    @Override
    public boolean isIntegrityViolation(EntityPlayer player, ItemStack itemStack, final ItemStack[] materials, String slot) {
        ItemStack upgradedStack = applyUpgrade(itemStack, materials, false, slot, null);
        return ItemModular.getIntegrityGain(upgradedStack) + ItemModular.getIntegrityCost(upgradedStack) < 0;
    }

    @Override
    public ItemStack applyUpgrade(final ItemStack itemStack, final ItemStack[] materials, boolean consumeMaterials, String slot, EntityPlayer player) {
        ItemStack upgradedStack = itemStack.copy();
        ItemModular item = (ItemModular) itemStack.getItem();
        ItemModule previousModule = item.getModuleFromSlot(upgradedStack, slot);
        if (previousModule != null) {
            previousModule.removeModule(upgradedStack);
            if (consumeMaterials) {
                previousModule.postRemove(upgradedStack, player);
            }
        }

        return upgradedStack;
    }

    @Override
    public boolean checkCapabilities(EntityPlayer player,final ItemStack targetStack,  final ItemStack[] materials) {
        return getRequiredCapabilities(targetStack, materials).stream()
                .allMatch(capability -> CapabilityHelper.getCapabilityLevel(player, capability) >= getRequiredCapabilityLevel(targetStack, materials, capability));
    }

    @Override
    public Collection<Capability> getRequiredCapabilities(final ItemStack targetStack, final ItemStack[] materials) {
        if (targetStack.getItem() instanceof ItemModular) {
            ItemModule module = item.getModuleFromSlot(targetStack, slot);
            if (module != null) {
                return module.getRepairRequiredCapabilities(targetStack);
            }
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public int getRequiredCapabilityLevel(final ItemStack targetStack, final ItemStack[] materials, Capability capability) {
        if (targetStack.getItem() instanceof ItemModular) {
            ItemModule module = item.getModuleFromSlot(targetStack, slot);
            if (module != null) {
                return module.getRepairRequiredCapabilityLevel(targetStack, capability);
            }
        }
        return 0;
    }

    @Override
    public SchemaType getType() {
        return SchemaType.major;
    }

    @Override
    public GlyphData getGlyph() {
        return glyph;
    }
}