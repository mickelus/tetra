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


    public TooltipGetterTool(ToolAction tool) {
        localizationKey = "tetra.stats." + tool.name() + ".tooltip";

        levelGetter = new StatGetterToolLevel(tool);
        baseEfficiencyGetter = new StatGetterToolEfficiency(tool);
        attackSpeedGetter = new StatGetterAttribute(Attributes.ATTACK_SPEED);
        enchantmentGetter = new StatGetterEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, 1);
        totalEfficiencyGetter = new StatGetterToolCompoundEfficiency(baseEfficiencyGetter, attackSpeedGetter, enchantmentGetter);
    }


    @Override
    public String getTooltipBase(Player player, ItemStack itemStack) {
        double enchantmentBonus = ItemModularHandheld.getEfficiencyEnchantmentBonus((int) enchantmentGetter.getValue(player, itemStack));
        double speedMultiplier = ItemModularHandheld.getAttackSpeedHarvestModifier(attackSpeedGetter.getValue(player, itemStack));
        return I18n.get(localizationKey) + "\n \n" +
                I18n.get("tetra.stats.tool.breakdown",
                        (int) levelGetter.getValue(player, itemStack),
                        String.format("%.2f", totalEfficiencyGetter.getValue(player, itemStack)),
                        String.format("%.2f", baseEfficiencyGetter.getValue(player, itemStack)),
                        speedMultiplier < 1 ? ChatFormatting.RED : speedMultiplier > 1 ? ChatFormatting.GREEN : ChatFormatting.YELLOW,
                        String.format("%.2f", speedMultiplier),
                        enchantmentBonus > 0 ? I18n.get("tetra.stats.tool.enchantment_bonus", String.format("%.0f", enchantmentBonus)) : "");
    }

    public boolean hasExtendedTooltip(Player player, ItemStack itemStack) {
        return true;
    }

    public String getTooltipExtension(Player player, ItemStack itemStack) {
        return I18n.get("tetra.stats.tool.tooltip_extended");
    }
}
