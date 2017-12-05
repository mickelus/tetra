package se.mickelus.tetra.module;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.capabilities.ICapabilityProvider;
import se.mickelus.tetra.items.ItemModular;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RepairSchema implements UpgradeSchema {
    private static final String nameSuffix = ".name";
    private static final String descriptionSuffix = ".description";
    private static final String slotSuffix = ".slot1";

    private String key = "repair_schema";

    private ItemModular item;

    public RepairSchema(ItemModular item) {
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
        if (itemStack.getItem() instanceof ItemModular) {
            ItemModular item = (ItemModular) itemStack.getItem();
            return item.getRepairMaterial(itemStack).getDisplayName();
        }
        return "?";
    }

    @Override
    public int getRequiredQuantity(ItemStack itemStack, int index, ItemStack materialStack) {
        // todo: return random between 1 and materialCount based on seed and yield % of module durability based on quantity?
        return 1;
    }

    @Override
    public boolean slotAcceptsMaterial(final ItemStack itemStack, final int index, final ItemStack materialStack) {
        if (index == 0 && itemStack.getItem() instanceof ItemModular) {
            ItemModular item = (ItemModular) itemStack.getItem();
            return materialStack.isItemEqual(item.getRepairMaterial(itemStack));
        }
        return false;
    }

    @Override
    public boolean canUpgrade(ItemStack itemStack) {
        return item.getClass().isInstance(itemStack.getItem());
    }

    @Override
    public boolean canApplyUpgrade(EntityPlayer player, ItemStack itemStack, ItemStack[] materials) {
        return slotAcceptsMaterial(itemStack, 0, materials[0]);
    }

    @Override
    public boolean isMaterialsValid(ItemStack itemStack, ItemStack[] materials) {
        return slotAcceptsMaterial(itemStack, 0, materials[0]);
    }

    @Override
    public boolean isIntegrityViolation(ItemStack itemStack, final ItemStack[] materials) {
        return false;
    }

    @Override
    public ItemStack applyUpgrade(final ItemStack itemStack, final ItemStack[] materials, boolean consumeMaterials) {
        ItemStack upgradedStack = itemStack.copy();
        ItemModular item = (ItemModular) upgradedStack.getItem();

        item.repair(upgradedStack);

        if (consumeMaterials) {
            materials[0].shrink(1);
        }

        return upgradedStack;
    }

    @Override
    public boolean checkCapabilities(EntityPlayer player, final ItemStack[] materials) {
        return getRequiredCapabilities(materials).stream()
                .allMatch(capability -> getCapabilityLevel(player, capability) >= getRequiredCapabilityLevel(materials, capability));
    }

    @Override
    public Collection<Capability> getRequiredCapabilities(final ItemStack[] materials) {
        return Collections.EMPTY_LIST;
        // todo: use same capability as target module
//        return module.getDataByMaterial(materials[0]).requiredCapabilities.getCapabilities();
    }

    @Override
    public int getRequiredCapabilityLevel(final ItemStack[] materials, Capability capability) {
        return 0;
        // todo: use same capability as target module
//        return module.getDataByMaterial(materials[0]).requiredCapabilities.getCapabilityLevel(capability);
    }

    @Override
    public int getCapabilityLevel(EntityPlayer player, Capability capability) {
        return Stream.concat(player.inventory.mainInventory.stream(), player.inventory.offHandInventory.stream())
                .filter(itemStack -> !itemStack.isEmpty())
                .filter(itemStack -> itemStack.getItem() instanceof ICapabilityProvider)
                .map(itemStack -> ((ICapabilityProvider) itemStack.getItem()).getCapabilityLevel(itemStack, capability))
                .max(Integer::compare)
                .orElse(0);
    }

    @Override
    public Collection<Capability> getCapabilities(EntityPlayer player) {
        return Stream.concat(player.inventory.mainInventory.stream(), player.inventory.offHandInventory.stream())
                .filter(itemStack -> !itemStack.isEmpty())
                .filter(itemStack -> itemStack.getItem() instanceof ICapabilityProvider)
                .flatMap(itemStack -> ((ICapabilityProvider) itemStack.getItem()).getCapabilities(itemStack).stream())
                .collect(Collectors.toSet());
    }
}
