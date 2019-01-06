package se.mickelus.tetra.module;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.NBTHelper;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.capabilities.ICapabilityProvider;
import se.mickelus.tetra.module.data.ModuleData;
import se.mickelus.tetra.module.schema.Material;
import se.mickelus.tetra.module.schema.RepairDefinition;

public abstract class ItemModule<T extends ModuleData> implements ICapabilityProvider {

    protected T[] data;

    protected String slotKey;
    protected String moduleKey;
    protected String dataKey;

    protected Priority renderLayer = Priority.BASE;

    public ItemModule(String slotKey, String moduleKey) {
        this.slotKey = slotKey;
        this.moduleKey = moduleKey;
        this.dataKey = moduleKey + "_material";
    }

    public String getKey() {
        return moduleKey;
    }

    public String getUnlocalizedName() {
        return moduleKey;
    }

    public String getSlot() {
        return slotKey;
    }

    public void addModule(ItemStack targetStack, String variantKey, EntityPlayer player) {
        NBTTagCompound tag = NBTHelper.getTag(targetStack);

        tag.setString(slotKey, moduleKey);
        tag.setString(dataKey, variantKey);
    }

    public ItemStack[] removeModule(ItemStack targetStack) {
        NBTTagCompound tag = NBTHelper.getTag(targetStack);

        tag.removeTag(slotKey);
        tag.removeTag(dataKey);

        return new ItemStack[0];
    }

    public void postRemove(ItemStack targetStack, EntityPlayer player) {

    }

    public T getData(ItemStack itemStack) {
        NBTTagCompound tag = NBTHelper.getTag(itemStack);
        String dataName = tag.getString(this.dataKey);

        return Arrays.stream(data)
                .filter(moduleData -> moduleData.key.equals(dataName))
                .findAny().orElse(getDefaultData());
    }

    public T getDefaultData() {
        return data[0];
    }

    public String getName(ItemStack itemStack) {
        return I18n.format(getData(itemStack).key);
    }

    public String getItemName(ItemStack itemStack) {
        String name = getData(itemStack).key + ".name";
        if (I18n.hasKey(name)) {
            return I18n.format(name);
        } else if (I18n.hasKey(getUnlocalizedName() + ".name")) {
            return I18n.format(getUnlocalizedName() + ".name");
        }
        return null;
    }

    public Priority getItemNamePriority(ItemStack itemStack) {
        return Priority.BASE;
    }

    public String getItemPrefix(ItemStack itemStack) {
        String prefix = getData(itemStack).key + ".prefix";
        if (I18n.hasKey(prefix)) {
            return I18n.format(prefix);
        } else if (I18n.hasKey(getUnlocalizedName() + ".prefix")) {
            return I18n.format(getUnlocalizedName() + ".prefix");
        }
        return null;
    }

    public Priority getItemPrefixPriority(ItemStack itemStack) {
        return Priority.BASE;
    }


    /**
     * Returns the integrity gained from this module. Split into two methods as modules with improvements may have
     * internal gains/costs which should be visible.
     *
     * @param itemStack An itemstack containing module data for this module
     * @return Integrity gained from this module, excluding internal costs
     */
    public int getIntegrityGain(ItemStack itemStack) {
        int integrity = getData(itemStack).integrity;
        if (integrity > 0 ) {
            return integrity;
        }
        return 0;
    }

    /**
     * Returns the integrity cost of this module. Split into two methods as modules with improvements may have
     * internal gains/costs which should be visible.
     *
     * @param itemStack An itemstack containing module data for this module
     * @return Integrity cost of this module, excluding internal gains
     */
    public int getIntegrityCost(ItemStack itemStack) {
        int integrity = getData(itemStack).integrity;
        if (integrity < 0 ) {
            return integrity;
        }
        return 0;
    }


    public int getDurability(ItemStack itemStack) {
        return getData(itemStack).durability;
    }

    public Material getRepairMaterial(ItemStack itemStack) {
        RepairDefinition definition = ItemUpgradeRegistry.instance.getRepairDefinition(getData(itemStack).key);
        if (definition != null) {
            return definition.material;
        }
        return null;
    }

    public int getRepairAmount(ItemStack itemStack) {
        return getData(itemStack).durability;
    }

    public Collection<Capability> getRepairRequiredCapabilities(ItemStack itemStack) {
        RepairDefinition definition = ItemUpgradeRegistry.instance.getRepairDefinition(getData(itemStack).key);
        if (definition != null) {
            return definition.requiredCapabilities.getValues();
        }
        return Collections.emptyList();
    }

    public int getRepairRequiredCapabilityLevel(ItemStack itemStack, Capability capability) {
        RepairDefinition definition = ItemUpgradeRegistry.instance.getRepairDefinition(getData(itemStack).key);
        if (definition != null) {
            return definition.requiredCapabilities.getLevel(capability);
        }
        return 0;
    }


    public double getDamageModifier(ItemStack itemStack) {
        return getData(itemStack).damage;
    }

    public double getDamageMultiplierModifier(ItemStack itemStack) { return getData(itemStack).damageMultiplier; }

    public double getSpeedModifier(ItemStack itemStack) {
        return getData(itemStack).attackSpeed;
    }

    public double getSpeedMultiplierModifier(ItemStack itemStack) { return getData(itemStack).attackSpeedMultiplier; }

    public ResourceLocation[] getTextures(ItemStack itemStack) {
        return new ResourceLocation[] { getData(itemStack).getTextureLocation() };
    }

    public Priority getRenderLayer() {
        return renderLayer;
    }

    public ResourceLocation[] getAllTextures() {
        return Arrays.stream(data)
                .map(ModuleData::getTextureLocation)
                .toArray(ResourceLocation[]::new);
    }

    public void hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {}

    public int getEffectLevel(ItemStack itemStack, ItemEffect effect) {
        return getData(itemStack).effects.getLevel(effect);
    }

    public float getEffectEfficiency(ItemStack itemStack, ItemEffect effect) {
        return getData(itemStack).effects.getEfficiency(effect);
    }

    public Collection<ItemEffect> getEffects(ItemStack itemStack) {
        return getData(itemStack).effects.getValues();
    }

    @Override
    public int getCapabilityLevel(ItemStack itemStack, Capability capability) {
        return getData(itemStack).capabilities.getLevel(capability);
    }

    @Override
    public float getCapabilityEfficiency(ItemStack itemStack, Capability capability) {
        return getData(itemStack).capabilities.getEfficiency(capability);
    }

    @Override
    public Collection<Capability> getCapabilities(ItemStack itemStack) {
        return getData(itemStack).capabilities.getValues();
    }

    public int getSize(ItemStack itemStack) {
        return getData(itemStack).size;
    }
}
