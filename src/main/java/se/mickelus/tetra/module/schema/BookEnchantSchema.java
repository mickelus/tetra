package se.mickelus.tetra.module.schema;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.data.GlyphData;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class BookEnchantSchema implements UpgradeSchema {
    private static final String key = "book_enchant";

    private static final String nameSuffix = ".name";
    private static final String descriptionSuffix = ".description";
    private static final String slotSuffix = ".slot1";

    protected ItemModuleMajor module;

    private GlyphData glyph = new GlyphData("textures/gui/workbench.png", 81, 52);

    public BookEnchantSchema(ItemModuleMajor module) {
        this.module = module;

        ItemUpgradeRegistry.instance.registerSchema(this);
    }

    @Override
    public String getKey() {
        return module.getKey();
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
        return 1;
    }

    @Override
    public boolean acceptsMaterial(ItemStack itemStack, int index, ItemStack materialStack) {
        return !materialStack.isEmpty() && materialStack.getItem() instanceof ItemEnchantedBook;
    }

    @Override
    public boolean isMaterialsValid(ItemStack itemStack, ItemStack[] materials) {
        if (acceptsMaterial(itemStack, 0, materials[0])) {
            return EnchantmentHelper.getEnchantments(materials[0]).keySet().stream()
                    .map(ItemUpgradeRegistry.instance::getImprovementFromEnchantment)
                    .anyMatch(module::acceptsImprovement);
        }
        return false;
    }

    @Override
    public boolean canUpgrade(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ItemModular) {
            ItemModular item = (ItemModular) itemStack.getItem();
            return item.hasModule(itemStack, module);
        }
        return false;
    }

    @Override
    public boolean isApplicableForSlot(String slot) {
        return module.getSlot().equals(slot);
    }

    @Override
    public boolean isHoning() {
        return false;
    }

    @Override
    public boolean canApplyUpgrade(EntityPlayer player, ItemStack itemStack, ItemStack[] materials, String slot) {
        return isMaterialsValid(itemStack, materials);
    }

    @Override
    public boolean isIntegrityViolation(EntityPlayer player, ItemStack itemStack, ItemStack[] materials, String slot) {
        return false;
    }

    @Override
    public ItemStack applyUpgrade(ItemStack itemStack, ItemStack[] materials, boolean consumeMaterials, String slot, EntityPlayer player) {
        ItemStack upgradedStack = itemStack.copy();

        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(materials[0]);
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            String improvement = ItemUpgradeRegistry.instance.getImprovementFromEnchantment(entry.getKey());
            if (module.acceptsImprovement(improvement)) {
                module.addImprovement(upgradedStack, improvement, entry.getValue());
            }
        }

        if (consumeMaterials) {
            materials[0].shrink(1);

            if (player instanceof EntityPlayerMP) {
                // todo: add proper criteria
                CriteriaTriggers.CONSUME_ITEM.trigger((EntityPlayerMP) player, upgradedStack);
            }
        }

        return upgradedStack;
    }

    @Override
    public boolean checkCapabilities(EntityPlayer player, ItemStack targetStack, ItemStack[] materials) {
        return true;
    }

    @Override
    public Collection<Capability> getRequiredCapabilities(ItemStack targetStack, ItemStack[] materials) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public int getRequiredCapabilityLevel(ItemStack targetStack, ItemStack[] materials, Capability capability) {
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
