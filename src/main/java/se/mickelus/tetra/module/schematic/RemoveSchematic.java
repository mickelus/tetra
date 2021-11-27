package se.mickelus.tetra.module.schematic;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.ToolType;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.ToolTypes;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.SchematicRegistry;
import se.mickelus.tetra.module.data.GlyphData;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;


public class RemoveSchematic extends BaseSchematic {
    private static final String localizationPrefix = TetraMod.MOD_ID + "/schematic/";
    private static final String nameSuffix = ".name";
    private static final String descriptionSuffix = ".description";

    private String key = "remove";

    private IModularItem item;
    private String slot;

    private GlyphData glyph = new GlyphData(GuiTextures.workbench, 52, 32);

    public RemoveSchematic(IModularItem item, String slot) {
        this.item = item;
        this.slot = slot;
    }

    public static void registerRemoveSchematics(IModularItem item) {
        Stream.concat(Arrays.stream(item.getMajorModuleKeys()), Arrays.stream(item.getMinorModuleKeys()))
                .filter(slot -> !item.isModuleRequired(slot))
                .forEach(slot -> {
                    RemoveSchematic schematic = new RemoveSchematic(item, slot);
                    SchematicRegistry.instance.registerSchematic(schematic);
                });
    }

    @Override
    public String getKey() {
        return key + "/" + item.getItem().getRegistryName().getPath() + "/" + slot;
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
    public boolean canApplyUpgrade(PlayerEntity player, ItemStack itemStack, ItemStack[] materials, String slot, Map<ToolType, Integer> availableTools) {
        return !isIntegrityViolation(player, itemStack, materials, slot)
                && checkTools(itemStack, materials, availableTools);
    }

    @Override
    public boolean isMaterialsValid(ItemStack itemStack, String itemSlot, ItemStack[] materials) {
        return true;
    }

    @Override
    public ItemStack applyUpgrade(final ItemStack itemStack, final ItemStack[] materials, boolean consumeMaterials, String slot, PlayerEntity player) {
        ItemStack upgradedStack = itemStack.copy();
        IModularItem item = (IModularItem) itemStack.getItem();


        float durabilityFactor = 0;
        if (consumeMaterials && upgradedStack.isDamageable()) {
            durabilityFactor = upgradedStack.getDamage() * 1f / upgradedStack.getMaxDamage();
        }

        float honingFactor = MathHelper.clamp(1f * item.getHoningProgress(upgradedStack) / item.getHoningLimit(upgradedStack), 0, 1);

        ItemModule previousModule = item.getModuleFromSlot(upgradedStack, slot);
        if (previousModule != null) {
            previousModule.removeModule(upgradedStack);
            if (consumeMaterials) {
                previousModule.postRemove(upgradedStack, player);
            }
        }

        if (consumeMaterials) {
            if (ConfigHandler.moduleProgression.get() && IModularItem.isHoneable(upgradedStack)) {
                item.setHoningProgress(upgradedStack, (int) Math.ceil(honingFactor * item.getHoningLimit(upgradedStack)));
            }

            if (upgradedStack.isDamageable()) {
                upgradedStack.setDamage((int) (durabilityFactor * upgradedStack.getMaxDamage()));
            }
        }

        return upgradedStack;
    }

    @Override
    public Map<ToolType, Integer> getRequiredToolLevels(ItemStack targetStack, ItemStack[] materials) {
        return Collections.singletonMap(ToolTypes.hammer, 1);
    }

    @Override
    public SchematicType getType() {
        return SchematicType.other;
    }

    @Override
    public GlyphData getGlyph() {
        return glyph;
    }
}