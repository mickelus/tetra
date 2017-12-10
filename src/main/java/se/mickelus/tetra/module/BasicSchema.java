package se.mickelus.tetra.module;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.capabilities.CapabilityHelper;
import se.mickelus.tetra.capabilities.ICapabilityProvider;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.items.sword.ItemSwordModular;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BasicSchema implements UpgradeSchema {

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
    public int getRequiredQuantity(ItemStack itemStack, int index, ItemStack materialStack) {
        if (index == 0) {
            return module.getDataByMaterial(materialStack).materialCount;
        }
        return 0;
    }

    @Override
    public boolean slotAcceptsMaterial(final ItemStack itemStack, final int index, final ItemStack materialStack) {
        if (index == 0) {
            return module.slotAcceptsMaterial(itemStack, materialStack);
        }
        return true;
    }

    @Override
    public boolean canUpgrade(ItemStack itemStack) {
        return item.equals(itemStack.getItem());
    }

    @Override
    public boolean canApplyUpgrade(EntityPlayer player, ItemStack itemStack, ItemStack[] materials) {
        return isMaterialsValid(itemStack, materials)
                && !isIntegrityViolation(itemStack, materials)
                && checkCapabilities(player, itemStack, materials);
    }

    @Override
    public boolean isMaterialsValid(ItemStack itemStack, ItemStack[] materials) {
        return module.canApplyUpgrade(itemStack, materials);
    }

    @Override
    public boolean isIntegrityViolation(ItemStack itemStack, final ItemStack[] materials) {
        ItemStack upgradedStack = applyUpgrade(itemStack, materials, false);
        return ItemModular.getIntegrityGain(upgradedStack) + ItemModular.getIntegrityCost(upgradedStack) < 0;
    }

    @Override
    public boolean checkCapabilities(EntityPlayer player, final ItemStack targetStack, final ItemStack[] materials) {
        return getRequiredCapabilities(targetStack, materials).stream()
                .allMatch(capability -> CapabilityHelper.getCapabilityLevel(player, capability) >= getRequiredCapabilityLevel(targetStack, materials, capability));
    }

    @Override
    public Collection<Capability> getRequiredCapabilities(final ItemStack targetStack, final ItemStack[] materials) {
        return module.getDataByMaterial(materials[0]).requiredCapabilities.getCapabilities();
    }

    @Override
    public int getRequiredCapabilityLevel(final ItemStack targetStack, final ItemStack[] materials, Capability capability) {
        return module.getDataByMaterial(materials[0]).requiredCapabilities.getCapabilityLevel(capability);
    }

    @Override
    public ItemStack applyUpgrade(final ItemStack itemStack, final ItemStack[] materials, boolean consumeMaterials) {
        ItemStack upgradedStack = itemStack.copy();

        module.addModule(upgradedStack, materials, consumeMaterials);

        return upgradedStack;
    }
}
