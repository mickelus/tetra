package se.mickelus.tetra.module.schematic;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.ToolAction;
import org.apache.commons.lang3.ArrayUtils;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.data.GlyphData;
import se.mickelus.tetra.module.improvement.DestabilizationEffect;
import se.mickelus.tetra.util.CastOptional;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;
@ParametersAreNonnullByDefault
public class CleanseSchematic implements UpgradeSchematic {
    private static final String localizationPrefix = TetraMod.MOD_ID + "/schematic/";
    private static final String key = "cleanse";

    private static final String nameSuffix = ".name";
    private static final String descriptionSuffix = ".description";
    private static final String slotLabel = "item.minecraft.lapis_lazuli";

    private GlyphData glyph = new GlyphData("textures/gui/workbench.png", 80, 32);

    public CleanseSchematic() { }

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
        return I18n.get(slotLabel);
    }

    @Override
    public ItemStack[] getSlotPlaceholders(ItemStack itemStack, int index) {
        return new ItemStack[] {Items.LAPIS_LAZULI.getDefaultInstance()};
    }

    @Override
    public int getRequiredQuantity(ItemStack itemStack, int index, ItemStack materialStack) {
        return 1;
    }

    @Override
    public boolean acceptsMaterial(ItemStack itemStack, String itemSlot, int index, ItemStack materialStack) {
        return materialStack.getItem().is(Tags.Items.GEMS_LAPIS);
    }

    @Override
    public boolean isMaterialsValid(ItemStack itemStack, String itemSlot, ItemStack[] materials) {
        return acceptsMaterial(itemStack, itemSlot, 0, materials[0]);
    }

    @Override
    public boolean isApplicableForItem(ItemStack itemStack) {
        return true;
    }

    @Override
    public boolean isApplicableForSlot(String slot, ItemStack targetStack) {
        String[] destabilizationKeys = DestabilizationEffect.getKeys();

        return CastOptional.cast(targetStack.getItem(), IModularItem.class)
                .map(item -> item.getModuleFromSlot(targetStack, slot))
                .filter(module -> module instanceof ItemModuleMajor)
                .map(module -> (ItemModuleMajor) module)
                .map(module -> Arrays.stream(module.getImprovements(targetStack)))
                .orElse(Stream.empty())
                .anyMatch(improvement -> ArrayUtils.contains(destabilizationKeys, improvement.key));
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

        String[] destabilizationKeys = DestabilizationEffect.getKeys();

        CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .map(item -> item.getModuleFromSlot(itemStack, slot))
                .filter(module -> module instanceof ItemModuleMajor)
                .map(module -> (ItemModuleMajor) module)
                .ifPresent(module -> Arrays.stream(destabilizationKeys).forEach(key -> module.removeImprovement(upgradedStack, key)));

        if (consumeMaterials) {
            materials[0].shrink(1);
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
        String[] destabilizationKeys = DestabilizationEffect.getKeys();

        int cost = CastOptional.cast(targetStack.getItem(), IModularItem.class)
                .map(item -> item.getModuleFromSlot(targetStack, slot))
                .filter(module -> module instanceof ItemModuleMajor)
                .map(module -> (ItemModuleMajor) module)
                .map(module -> Arrays.stream(module.getImprovements(targetStack)))
                .orElse(Stream.empty())
                .filter(improvement -> ArrayUtils.contains(destabilizationKeys, improvement.key))
                .mapToInt(improvement -> improvement.level + 1)
                .sum();

        cost += 3;

        return cost;
    }

    @Override
    public SchematicType getType() {
        return SchematicType.other;
    }

    @Override
    public GlyphData getGlyph() {
        return glyph;
    }

    @Override
    public OutcomePreview[] getPreviews(ItemStack targetStack, String slot) {
        return new OutcomePreview[0];
    }

    @Override
    public float getSeverity(ItemStack itemStack, ItemStack[] materials, String slot) {
        return 0;
    }
}
