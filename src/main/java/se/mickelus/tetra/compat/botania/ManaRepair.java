package se.mickelus.tetra.compat.botania;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import se.mickelus.tetra.blocks.workbench.gui.WorkbenchStatsGui;
import se.mickelus.tetra.effect.EffectHelper;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.gui.stats.bar.GuiStatBar;
import se.mickelus.tetra.gui.stats.StatsHelper;
import se.mickelus.tetra.gui.stats.getter.IStatGetter;
import se.mickelus.tetra.gui.stats.getter.LabelGetterBasic;
import se.mickelus.tetra.gui.stats.getter.TooltipGetterInteger;
import se.mickelus.tetra.items.modular.impl.holo.gui.craft.HoloStatsGui;
import se.mickelus.tetra.util.CastOptional;
import vazkii.botania.api.mana.ManaItemHandler;

public class ManaRepair {
    public static ItemEffect effect = ItemEffect.get("manaRepair");

    @OnlyIn(Dist.CLIENT)
    public static void clientInit() {
        IStatGetter statGetter = new ManaRepairStatGetter();
        GuiStatBar statBar = new GuiStatBar(0, 0, StatsHelper.barLength, "tetra.stats.manaRepair",
                0, 400, false, false, true, statGetter, LabelGetterBasic.integerLabelInverted,
                new TooltipGetterInteger("tetra.stats.manaRepair.tooltip", statGetter));

        WorkbenchStatsGui.addBar(statBar);
        HoloStatsGui.addBar(statBar);
    }

    public static void itemInventoryTick(ItemStack itemStack, World world, Entity entity) {
        if (!world.isClientSide && world.getGameTime() % 20 == 0 && BotaniaCompat.isLoaded) {
            int manaRepairLevel = EffectHelper.getEffectLevel(itemStack, effect);
            if (manaRepairLevel > 0 && itemStack.getDamageValue() > 0) {
                CastOptional.cast(entity, PlayerEntity.class)
                        .filter(player -> ManaItemHandler.instance().requestManaExactForTool(itemStack, player, manaRepairLevel * 2, true))
                        .ifPresent(player -> itemStack.setDamageValue(itemStack.getDamageValue() - 1));
            }
        }
    }
}
