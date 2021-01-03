package se.mickelus.tetra.compat.botania;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import se.mickelus.tetra.blocks.workbench.gui.WorkbenchStatsGui;
import se.mickelus.tetra.effect.EffectHelper;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.gui.statbar.GuiStatBar;
import se.mickelus.tetra.gui.statbar.GuiStats;
import se.mickelus.tetra.gui.statbar.getter.IStatGetter;
import se.mickelus.tetra.gui.statbar.getter.LabelGetterBasic;
import se.mickelus.tetra.gui.statbar.getter.StatGetterEffectLevel;
import se.mickelus.tetra.gui.statbar.getter.TooltipGetterInteger;
import se.mickelus.tetra.items.modular.impl.holo.gui.craft.HoloStatsGui;
import se.mickelus.tetra.util.CastOptional;
import vazkii.botania.api.mana.ManaItemHandler;

public class ManaRepair {
    public static ItemEffect effect = ItemEffect.get("manaRepair");

    public static final IStatGetter statGetter = new ManaRepairStatGetter();
    public static final GuiStatBar statBar = new GuiStatBar(0, 0, GuiStats.barLength, I18n.format("tetra.stats.manaRepair"),
            0, 400, false, false, true, statGetter, LabelGetterBasic.integerLabelInverted,
            new TooltipGetterInteger("tetra.stats.manaRepair.tooltip", statGetter));

    public static void init() {
        WorkbenchStatsGui.addBar(statBar);
        HoloStatsGui.addBar(statBar);
    }

    public static void itemInventoryTick(ItemStack itemStack, World world, Entity entity) {
        if (!world.isRemote && world.getGameTime() % 20 == 0 && BotaniaCompat.isLoaded) {
            int manaRepairLevel = EffectHelper.getEffectLevel(itemStack, effect);
            if (manaRepairLevel > 0 && itemStack.getDamage() > 0) {
                CastOptional.cast(entity, PlayerEntity.class)
                        .filter(player -> ManaItemHandler.instance().requestManaExactForTool(itemStack, player, manaRepairLevel * 2, true))
                        .ifPresent(player -> itemStack.setDamage(itemStack.getDamage() - 1));
            }
        }
    }
}
