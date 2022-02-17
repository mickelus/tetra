package se.mickelus.tetra.module.schematic;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.common.ToolAction;
import se.mickelus.mutil.util.CastOptional;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.advancements.ImprovementCraftCriterion;
import se.mickelus.tetra.aspect.TetraEnchantmentHelper;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.data.GlyphData;
import se.mickelus.tetra.module.data.ToolData;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

@ParametersAreNonnullByDefault
public class BookEnchantSchematic implements UpgradeSchematic {
    private static final String localizationPrefix = TetraMod.MOD_ID + "/schematic/";
    private static final String key = "book_enchant";

    private static final String nameSuffix = ".name";
    private static final String descriptionSuffix = ".description";
    private static final String slotSuffix = ".slot1";

    private final GlyphData glyph = new GlyphData(GuiTextures.workbench, 80, 32);

    public BookEnchantSchematic() {
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getName() {
        return I18n.get(localizationPrefix + key + nameSuffix);
    }

    @Override
    public String getDescription(ItemStack itemStack) {
        return I18n.get(localizationPrefix + key + descriptionSuffix);
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
        return new ItemStack[]{Items.ENCHANTED_BOOK.getDefaultInstance()};
    }

    @Override
    public int getRequiredQuantity(ItemStack itemStack, int index, ItemStack materialStack) {
        return 1;
    }

    @Override
    public boolean acceptsMaterial(ItemStack itemStack, String itemSlot, int index, ItemStack materialStack) {
        if (materialStack.isEmpty()) {
            return false;
        }

        ItemModuleMajor module = CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .map(item -> item.getModuleFromSlot(itemStack, itemSlot))
                .flatMap(mod -> CastOptional.cast(mod, ItemModuleMajor.class))
                .orElse(null);

        Map<Enchantment, Integer> currentEnchantments = EnchantmentHelper.getEnchantments(itemStack);

        return module != null && materialStack.getItem() instanceof EnchantedBookItem
                && EnchantmentHelper.getEnchantments(materialStack).entrySet().stream()
                .anyMatch(entry -> acceptsEnchantment(itemStack, module, currentEnchantments.keySet(), entry.getKey(), entry.getValue()));
    }

    protected boolean stacksEnchantment(ItemStack itemStack, ItemModuleMajor module, Enchantment enchantment, int level) {
        Map<Enchantment, Integer> moduleEnchantments = module.getEnchantments(itemStack);
        if (moduleEnchantments.containsKey(enchantment)) {
            int currentLevel = moduleEnchantments.get(enchantment);
            return level >= currentLevel && currentLevel < enchantment.getMaxLevel();
        }
        return false;
    }

    protected boolean acceptsEnchantment(ItemStack itemStack, ItemModuleMajor module, Set<Enchantment> currentEnchantments, Enchantment enchantment, int level) {
        return module.acceptsEnchantment(itemStack, enchantment, false)
                && (stacksEnchantment(itemStack, module, enchantment, level)
                || EnchantmentHelper.isEnchantmentCompatible(currentEnchantments, enchantment));
    }

    @Override
    public boolean isMaterialsValid(ItemStack itemStack, String itemSlot, ItemStack[] materials) {
        return acceptsMaterial(itemStack, itemSlot, 0, materials[0]);
    }

    @Override
    public boolean isApplicableForItem(ItemStack itemStack) {
        return itemStack.getItem() instanceof IModularItem;
    }

    @Override
    public boolean isApplicableForSlot(String slot, ItemStack targetStack) {
        return CastOptional.cast(targetStack.getItem(), IModularItem.class)
                .map(item -> item.getModuleFromSlot(targetStack, slot))
                .map(module -> module.getMagicCapacityGain(targetStack) > 0)
                .orElse(false);
    }

    @Override
    public boolean canApplyUpgrade(Player player, ItemStack itemStack, ItemStack[] materials, String slot, Map<ToolAction, Integer> availableTools) {
        return isMaterialsValid(itemStack, slot, materials)
                && (player.isCreative() || player.experienceLevel >= getExperienceCost(itemStack, materials, slot));
    }

    @Override
    public boolean isIntegrityViolation(Player player, ItemStack itemStack, ItemStack[] materials, String slot) {
        return false;
    }

    @Override
    public ItemStack applyUpgrade(ItemStack itemStack, ItemStack[] materials, boolean consumeMaterials, String slot, Player player) {
        ItemStack upgradedStack = itemStack.copy();

        ItemModuleMajor module = CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .map(item -> item.getModuleFromSlot(itemStack, slot))
                .filter(mod -> mod instanceof ItemModuleMajor)
                .map(mod -> (ItemModuleMajor) mod)
                .orElse(null);


        if (module != null) {
            Map<Enchantment, Integer> currentEnchantments = EnchantmentHelper.getEnchantments(itemStack);
            EnchantmentHelper.getEnchantments(materials[0]).entrySet().stream()
                    .filter(entry -> acceptsEnchantment(itemStack, module, currentEnchantments.keySet(), entry.getKey(), entry.getValue()))
                    .forEach(entry -> {
                        int level = entry.getValue();
                        if (stacksEnchantment(upgradedStack, module, entry.getKey(), level)) {
                            level = Math.max(currentEnchantments.get(entry.getKey()) + 1, level);
                            currentEnchantments.put(entry.getKey(), level);
                            EnchantmentHelper.setEnchantments(currentEnchantments, upgradedStack);
                        } else {
                            TetraEnchantmentHelper.applyEnchantment(upgradedStack, module.getSlot(), entry.getKey(), level);
                        }

                        if (consumeMaterials && player instanceof ServerPlayer) {
                            ImprovementCraftCriterion.trigger((ServerPlayer) player, itemStack, upgradedStack, getKey(), slot,
                                    "enchantment:" + Registry.ENCHANTMENT.getKey(entry.getKey()).toString(), level, null, -1);
                        }
                    });

            if (consumeMaterials) {
                materials[0].shrink(1);
            }
        }

        return upgradedStack;
    }

    @Override
    public boolean checkTools(ItemStack targetStack, ItemStack[] materials, Map<ToolAction, Integer> availableTools) {
        return true;
    }

    @Override
    public Map<ToolAction, Integer> getRequiredToolLevels(ItemStack targetStack, ItemStack[] materials) {
        return Collections.emptyMap();
    }

    @Override
    public int getExperienceCost(ItemStack targetStack, ItemStack[] materials, String slot) {
        return CastOptional.cast(targetStack.getItem(), IModularItem.class)
                .map(item -> item.getModuleFromSlot(targetStack, slot))
                .flatMap(module -> CastOptional.cast(module, ItemModuleMajor.class))
                .map(module -> EnchantmentHelper.getEnchantments(materials[0]))
                .map(Map::values)
                .stream()
                .flatMap(Collection::stream)
                .mapToInt(lvl -> lvl)
                .sum();
    }

    @Override
    public SchematicType getType() {
        return SchematicType.improvement;
    }

    @Override
    public GlyphData getGlyph() {
        return glyph;
    }

    @Override
    public OutcomePreview[] getPreviews(ItemStack targetStack, String slot) {
        ItemModuleMajor module = CastOptional.cast(targetStack.getItem(), IModularItem.class)
                .map(item -> item.getModuleFromSlot(targetStack, slot))
                .flatMap(m -> CastOptional.cast(m, ItemModuleMajor.class))
                .orElse(null);

        if (module != null) {
            ToolData emptyTools = new ToolData();
            return Registry.ENCHANTMENT.stream()
                    .filter(enchantment -> module.acceptsEnchantment(targetStack, enchantment, false))
                    .flatMap(enchantment -> IntStream.range(enchantment.getMinLevel(), enchantment.getMaxLevel() + 1)
                            .mapToObj(level -> {
                                ItemStack enchantedStack = targetStack.copy();
                                EnchantmentHelper.getEnchantments(enchantedStack).keySet().stream()
                                        .filter(currentEnchantment -> !enchantment.isCompatibleWith(currentEnchantment))
                                        .forEach(currentEnchantment -> TetraEnchantmentHelper.removeEnchantment(enchantedStack, currentEnchantment));

                                TetraEnchantmentHelper.applyEnchantment(enchantedStack, module.getSlot(), enchantment, level);
                                return new OutcomePreviewEnchantment(enchantment.getRegistryName().toString(),
                                        TetraEnchantmentHelper.getEnchantmentName(enchantment, level), "misc", level, glyph, enchantedStack,
                                        SchematicType.improvement, emptyTools, new ItemStack[0]);
                            }))
                    .toArray(OutcomePreview[]::new);
        }

        return new OutcomePreview[0];
    }
}
