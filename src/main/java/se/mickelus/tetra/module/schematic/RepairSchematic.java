package se.mickelus.tetra.module.schematic;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.ChatFormatting;
import net.minecraftforge.common.ToolType;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.data.GlyphData;
import se.mickelus.tetra.util.CastOptional;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RepairSchematic extends BaseSchematic {
    private static final String localizationPrefix = TetraMod.MOD_ID + "/schematic/";
    private static final String nameSuffix = ".name";
    private static final String slotSuffix = ".slot1";
    private static final String descriptionSuffix = ".description";
    private static final String extendedDescriptionSuffix = ".description_details";

    private String key = "repair";

    private IModularItem item;

    private GlyphData glyph = new GlyphData(GuiTextures.workbench, 0, 52);

    public RepairSchematic(IModularItem item) {
        this.item = item;
    }

    public String getSlot(ItemStack itemStack) {
        return CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .map(item -> item.getRepairSlot(itemStack))
                .orElse(null);
    }

    @Override
    public String getKey() {
        return key + "/" + item.getItem().getRegistryName().getPath();
    }

    @Override
    public String getName() {
        return I18n.get(localizationPrefix + key + nameSuffix);
    }

    @Override
    public String getDescription(@Nullable ItemStack itemStack) {
        return Optional.ofNullable(itemStack)
                .flatMap(stack -> CastOptional.cast(itemStack.getItem(), IModularItem.class))
                .map(item -> {
                    ItemModule[] cycle = item.getRepairCycle(itemStack);
                    ItemModule currentTarget = item.getRepairModule(itemStack).orElse(null);
                    if (currentTarget != null) {
                        return Arrays.stream(cycle)
                                .map(module -> {
                                    String name = module.getName(itemStack);
                                    return currentTarget.equals(module) ? ChatFormatting.WHITE + name + ChatFormatting.RESET : name;
                                })
                                .collect(Collectors.joining(", "));
                    }
                    return null;
                })
                .map(cycle -> I18n.get(localizationPrefix + key + extendedDescriptionSuffix, cycle))
                .orElseGet(() -> I18n.get(localizationPrefix + key + descriptionSuffix));
    }

    @Override
    public int getNumMaterialSlots() {
        return 1;
    }

    @Override
    public String getSlotName(final ItemStack itemStack, final int index) {
        return I18n.get(localizationPrefix + key + slotSuffix);
    }

    @Override
    public ItemStack[] getSlotPlaceholders(ItemStack itemStack, int index) {
        return CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .map(item -> item.getRepairDefinitions(itemStack))
                .map(Collection::stream)
                .orElse(Stream.empty())
                .map(definition -> definition.material)
                .map(OutcomeMaterial::getApplicableItemStacks)
                .flatMap(Arrays::stream)
                .toArray(ItemStack[]::new);
    }

    @Override
    public int getRequiredQuantity(ItemStack itemStack, int index, ItemStack materialStack) {
        if (index == 0 && itemStack.getItem() instanceof IModularItem) {
            IModularItem item = (IModularItem) itemStack.getItem();
            return item.getRepairMaterialCount(itemStack, materialStack);
        }
        return 0;
    }

    @Override
    public boolean acceptsMaterial(final ItemStack itemStack, String itemSlot, final int index, final ItemStack materialStack) {
        if (index == 0) {
            return CastOptional.cast(itemStack.getItem(), IModularItem.class)
                    .map(item -> item.getRepairDefinitions(itemStack))
                    .map(Collection::stream)
                    .orElse(Stream.empty())
                    .map(definition -> definition.material.getPredicate())
                    .anyMatch(predicate -> predicate.matches(materialStack));
        }
        return false;
    }

    @Override
    public boolean isApplicableForItem(ItemStack itemStack) {
        return item.getClass().isInstance(itemStack.getItem());
    }

    @Override
    public boolean isApplicableForSlot(String slot, ItemStack targetStack) {
        return slot == null;
    }

    @Override
    public ItemStack applyUpgrade(final ItemStack itemStack, final ItemStack[] materials, boolean consumeMaterials, String slot, Player player) {
        ItemStack upgradedStack = itemStack.copy();
        IModularItem item = (IModularItem) upgradedStack.getItem();
        int quantity = getRequiredQuantity(itemStack, 0, materials[0]);

        item.repair(upgradedStack);

        if (consumeMaterials) {
            materials[0].shrink(quantity);
        }

        return upgradedStack;
    }

    @Override
    public boolean isMaterialsValid(ItemStack itemStack, String itemSlot, ItemStack[] materials) {
        return acceptsMaterial(itemStack, itemSlot, 0, materials[0])
                && materials[0].getCount() >= getRequiredQuantity(itemStack, 0, materials[0]);
    }

    @Override
    public boolean isIntegrityViolation(Player player, ItemStack itemStack, final ItemStack[] materials, String slot) {
        return false;
    }

    @Override
    public Map<ToolType, Integer> getRequiredToolLevels(ItemStack targetStack, ItemStack[] materials) {
        return CastOptional.cast(targetStack.getItem(), IModularItem.class)
                .map(item -> item.getRepairRequiredToolLevels(targetStack, materials[0]))
                .orElseGet(Collections::emptyMap);
    }

    @Override
    public int getRequiredToolLevel(final ItemStack targetStack, final ItemStack[] materials, ToolType toolType) {
        return CastOptional.cast(targetStack.getItem(), IModularItem.class)
                .map(item -> item.getRepairRequiredToolLevel(targetStack, materials[0], toolType))
                .orElse(0);
    }

    @Override
    public int getExperienceCost(ItemStack targetStack, ItemStack[] materials, String slot) {
        return CastOptional.cast(targetStack.getItem(), IModularItem.class)
        .map(item -> item.getRepairRequiredExperience(targetStack))
                .orElse(0);
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
