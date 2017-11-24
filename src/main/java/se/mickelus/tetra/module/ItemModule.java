package se.mickelus.tetra.module;

import java.util.Arrays;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameData;
import se.mickelus.tetra.DataHandler;
import se.mickelus.tetra.NBTHelper;

public abstract class ItemModule<T extends ModuleData> {

    protected T[] data;

    protected String slotKey;
    protected String moduleKey;
    protected String dataKey;

    protected RenderLayer renderLayer = RenderLayer.BASE;

    public ItemModule(String slotKey, String moduleKey, String dataKey) {
        this.slotKey = slotKey;
        this.moduleKey = moduleKey;
        this.dataKey = dataKey;
    }

    public void addModule(ItemStack targetStack, ItemStack[] materials, boolean consumeMaterials) {
        NBTTagCompound tag = NBTHelper.getTag(targetStack);
        ModuleData data = getDataByMaterial(materials[0]);

        tag.setString(slotKey, moduleKey);
        tag.setString(dataKey, data.key);

        if (consumeMaterials) {
            materials[0].shrink(data.materialCount);
        }
    }

    // todo: fix to work for upgrades with more than one material
    public boolean canApplyUpgrade(ItemStack targetStack, ItemStack[] materials) {
        return slotAcceptsMaterial(targetStack, materials[0]);
    }

    public boolean slotAcceptsMaterial(ItemStack targetStack, ItemStack material) {
        String materialName = Item.REGISTRY.getNameForObject(material.getItem()).toString();

        return Arrays.stream(data)
                .anyMatch(moduleData -> moduleData.material.equals(materialName));
    }

    public T getDataByMaterial(ItemStack itemStack) {
        String material = Item.REGISTRY.getNameForObject(itemStack.getItem()).toString();

        return Arrays.stream(data)
                .filter(moduleData -> moduleData.material.equals(material))
                .findAny().orElse(getDefaultData());
    }

    public ItemStack[] removeModule(ItemStack targetStack, ItemStack[] tools) {
        NBTTagCompound tag = NBTHelper.getTag(targetStack);

        tag.removeTag(slotKey);
        tag.removeTag(dataKey);

        return new ItemStack[0];
    }

    public T getData(ItemStack itemStack) {
        NBTTagCompound tag = NBTHelper.getTag(itemStack);
        String dataName = tag.getString(this.dataKey);

        return Arrays.stream(data)
                .filter(moduleData -> moduleData.key.equals(dataName))
                .findAny().orElse(getDefaultData());
    }

    protected T getDefaultData() {
        return data[0];
    }

    public String getName(ItemStack itemStack) {
        return I18n.format(getData(itemStack).key);
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

    public ItemStack getRepairMaterial(ItemStack itemStack) {
        Item item = Item.getByNameOrId(getData(itemStack).material);

        if (item != null) {
            return new ItemStack(item);
        } else {
            return null;
        }
    }

    public int getRepairAmount(ItemStack itemStack) {
        return getData(itemStack).durability;
    }

    public double getDamageModifier(ItemStack itemStack) { return 0; }
    public double getDamageMultiplierModifier(ItemStack itemStack) { return 1; }

    public double getSpeedModifier(ItemStack itemStack) { return 0; }
    public double getSpeedMultiplierModifier(ItemStack itemStack) { return 1; }

    public ResourceLocation[] getTextures(ItemStack itemStack) {
        return new ResourceLocation[] { getData(itemStack).getTextureLocation() };
    }

    public RenderLayer getRenderLayer() {
        return renderLayer;
    }

    public ResourceLocation[] getAllTextures() {
        return Arrays.stream(data)
                .map(ModuleData::getTextureLocation)
                .toArray(ResourceLocation[]::new);
    }

    public ResourceLocation getGlyphLocation(ItemStack itemStack) {
        return getData(itemStack).glyph.textureLocation;
    }

    public int getGlyphTint(ItemStack itemStack) {
        return getData(itemStack).glyph.tint;
    }

    public void hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {}
}
