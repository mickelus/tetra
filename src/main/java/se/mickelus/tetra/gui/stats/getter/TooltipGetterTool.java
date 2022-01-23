package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.common.ToolAction;
import se.mickelus.tetra.items.modular.ItemModularHandheld;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TooltipGetterTool implements ITooltipGetter {

    private final IStatGetter levelGetter;
    private final IStatGetter baseEfficiencyGetter;
    private final IStatGetter attackSpeedGetter;
    private final IStatGetter enchantmentGetter;
    private final IStatGetter totalEfficiencyGetter;
    private final String localizationKey;
    private final boolean includeSpeedModifier;


    public TooltipGetterTool(ToolAction tool, boolean includeSpeedModifier) {
        localizationKey = "tetra.stats." + tool.name() + ".tooltip";

        this.includeSpeedModifier = includeSpeedModifier;

        levelGetter = new StatGetterToolLevel(tool);
        baseEfficiencyGetter = new StatGetterToolEfficiency(tool);
        attackSpeedGetter = new StatGetterAttribute(Attributes.ATTACK_SPEED);
        enchantmentGetter = new StatGetterEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, 1);

        if (includeSpeedModifier) {
            totalEfficiencyGetter = new StatGetterToolCompoundEfficiency(baseEfficiencyGetter, attackSpeedGetter, enchantmentGetter);
        } else {
            totalEfficiencyGetter = new StatGetterSum(baseEfficiencyGetter, enchantmentGetter);
        }
    }


    @Override
    public String getTooltipBase(Player player, ItemStack itemStack) {
        double enchantmentBonus = ItemModularHandheld.getEfficiencyEnchantmentBonus((int) enchantmentGetter.getValue(player, itemStack));
        double speedMultiplier = ItemModularHandheld.getAttackSpeedHarvestModifier(attackSpeedGetter.getValue(player, itemStack));
        String speedString = ChatFormatting.DARK_GRAY + I18n.get("tetra.not_available");
        if (includeSpeedModifier) {
            if (speedMultiplier < 1) {
                speedString = ChatFormatting.RED.toString();
            } else if (speedMultiplier > 1) {
                speedString = ChatFormatting.GREEN.toString();
            } else {
                speedString = ChatFormatting.YELLOW.toString();
            }
            speedString += String.format("%.2fx", speedMultiplier);
        }

        return I18n.get(localizationKey) + "\n \n" +
                I18n.get("tetra.stats.tool.breakdown",
                        (int) levelGetter.getValue(player, itemStack),
                        String.format("%.2f", totalEfficiencyGetter.getValue(player, itemStack)),
                        String.format("%.2f", baseEfficiencyGetter.getValue(player, itemStack)),
                        speedString,
                        enchantmentBonus > 0 ? I18n.get("tetra.stats.tool.enchantment_bonus", String.format("%.0f", enchantmentBonus)) : "");
    }

    public boolean hasExtendedTooltip(Player player, ItemStack itemStack) {
        return true;
    }

    public String getTooltipExtension(Player player, ItemStack itemStack) {
        return I18n.get("tetra.stats.tool.tooltip_extended");
    }
}
