package se.mickelus.tetra.module.schema;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.items.modular.ItemModular;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.SchemaRegistry;
import se.mickelus.tetra.module.data.GlyphData;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;


public class RemoveSchema extends BaseSchema {
    private static final String localizationPrefix = TetraMod.MOD_ID + "/schema/";
    private static final String nameSuffix = ".name";
    private static final String descriptionSuffix = ".description";

    private String key = "remove";

    private ItemModular item;
    private String slot;

    private GlyphData glyph = new GlyphData(GuiTextures.workbench, 52, 32);

    public RemoveSchema(ItemModular item, String slot) {
        this.item = item;
        this.slot = slot;
    }

    public static void registerRemoveSchemas(ItemModular item) {
        Stream.concat(Arrays.stream(item.getMajorModuleKeys()), Arrays.stream(item.getMinorModuleKeys()))
                .filter(slot -> !item.isModuleRequired(slot))
                .forEach(slot -> {
                    RemoveSchema schema = new RemoveSchema(item, slot);
                    SchemaRegistry.instance.registerSchema(schema);
                });
    }

    @Override
    public String getKey() {
        return key + "/" + item.getRegistryName().getPath() + "/" + slot;
    }

    @Override
    public String getName() {
        return I18n.format(localizationPrefix + key + nameSuffix);
    }

    @Override
    public String getDescription(ItemStack itemStack) {
        return I18n.format(localizationPrefix + key + descriptionSuffix);
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
    public boolean acceptsMaterial(final ItemStack itemStack, String itemSlot, final int index, final ItemStack materialStack) {
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
    public boolean canApplyUpgrade(PlayerEntity player, ItemStack itemStack, ItemStack[] materials, String slot, int[] availableCapabilities) {
        return !isIntegrityViolation(player, itemStack, materials, slot)
                && checkCapabilities(itemStack, materials, availableCapabilities);
    }

    @Override
    public boolean isMaterialsValid(ItemStack itemStack, String itemSlot, ItemStack[] materials) {
        return true;
    }

    @Override
    public ItemStack applyUpgrade(final ItemStack itemStack, final ItemStack[] materials, boolean consumeMaterials, String slot, PlayerEntity player) {
        ItemStack upgradedStack = itemStack.copy();
        ItemModular item = (ItemModular) itemStack.getItem();


        float durabilityFactor = 0;
        if (consumeMaterials && upgradedStack.isDamageable()) {
            durabilityFactor = upgradedStack.getDamage() * 1f / upgradedStack.getMaxDamage();
        }

        float honingFactor = Math.min(Math.max(1f * item.getHoningProgress(upgradedStack) / item.getHoningBase(upgradedStack), 0), 1);

        ItemModule previousModule = item.getModuleFromSlot(upgradedStack, slot);
        if (previousModule != null) {
            previousModule.removeModule(upgradedStack);
            if (consumeMaterials) {
                previousModule.postRemove(upgradedStack, player);
            }
        }

        if (consumeMaterials) {
            if (ConfigHandler.moduleProgression.get() && ItemModular.isHoneable(upgradedStack)) {
                item.setHoningProgress(upgradedStack, (int) Math.ceil(honingFactor * item.getHoningBase(upgradedStack)));
            }

            if (upgradedStack.isDamageable()) {
                upgradedStack.setDamage((int) (durabilityFactor * upgradedStack.getMaxDamage()));
            }
        }

        return upgradedStack;
    }

    @Override
    public Collection<Capability> getRequiredCapabilities(final ItemStack targetStack, final ItemStack[] materials) {
        return Collections.singletonList(Capability.hammer);
    }

    @Override
    public int getRequiredCapabilityLevel(final ItemStack targetStack, final ItemStack[] materials, Capability capability) {
        if (capability == Capability.hammer) {
            return 1;
        }
        return 0;
    }

    @Override
    public SchemaType getType() {
        return SchemaType.other;
    }

    @Override
    public GlyphData getGlyph() {
        return glyph;
    }
}