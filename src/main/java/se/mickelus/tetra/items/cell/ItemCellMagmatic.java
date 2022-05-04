package se.mickelus.tetra.items.cell;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.TetraItem;
import se.mickelus.tetra.items.TetraItemGroup;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

import static se.mickelus.tetra.blocks.forged.ForgedBlockCommon.locationTooltip;

@ParametersAreNonnullByDefault
public class ItemCellMagmatic extends TetraItem {
    public static final int maxCharge = 128;
    private static final String unlocalizedName = "magmatic_cell";
    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ItemCellMagmatic instance;
    private final String chargedPropKey = "tetra:charged";

    public ItemCellMagmatic() {
        super(new Properties()
                .stacksTo(1)
                .durability(maxCharge)
                .tab(TetraItemGroup.instance));
    }

    @Override
    public void clientInit() {
        ItemProperties.register(this, new ResourceLocation(chargedPropKey), (itemStack, world, livingEntity, i) -> getCharge(itemStack) > 0 ? 1 : 0);
    }

    @Override
    public void appendHoverText(final ItemStack stack, @Nullable final Level world, final List<Component> tooltip, final TooltipFlag advanced) {
        int charge = getCharge(stack);

        BaseComponent chargeLine = new TranslatableComponent("item.tetra.magmatic_cell.charge");

        if (charge == maxCharge) {
            chargeLine.append(new TranslatableComponent("item.tetra.magmatic_cell.charge_full"));
        } else if (charge > maxCharge * 0.4) {
            chargeLine.append(new TranslatableComponent("item.tetra.magmatic_cell.charge_good"));
        } else if (charge > 0) {
            chargeLine.append(new TranslatableComponent("item.tetra.magmatic_cell.charge_low"));
        } else {
            chargeLine.append(new TranslatableComponent("item.tetra.magmatic_cell.charge_empty"));
        }

        tooltip.add(chargeLine);
        tooltip.add(new TextComponent(" "));
        tooltip.add(locationTooltip);
    }

    @Override
    public void fillItemCategory(final CreativeModeTab itemGroup, final NonNullList<ItemStack> itemList) {
        if (allowdedIn(itemGroup)) {
            itemList.add(new ItemStack(this));

            ItemStack emptyStack = new ItemStack(this);
            emptyStack.setDamageValue(maxCharge);
            itemList.add(emptyStack);
        }
    }

    public int getCharge(ItemStack itemStack) {
        return itemStack.getMaxDamage() - itemStack.getDamageValue();
    }

    public int drainCharge(ItemStack itemStack, int amount) {
        if (itemStack.getDamageValue() + amount < itemStack.getMaxDamage()) {
            setDamage(itemStack, itemStack.getDamageValue() + amount);
            return amount;
        }

        int actualAmount = itemStack.getMaxDamage() - itemStack.getDamageValue();
        setDamage(itemStack, itemStack.getMaxDamage());
        return actualAmount;
    }

    public int recharge(ItemStack itemStack, int amount) {
        if (getDamage(itemStack) - amount >= 0) {
            setDamage(itemStack, getDamage(itemStack) - amount);
            return 0;
        }

        int overfill = amount - getDamage(itemStack);
        setDamage(itemStack, 0);
        return overfill;
    }

    // todo: change these for metered upgrade
    public boolean showDurabilityBar(ItemStack stack) {
        return false;
    }

    /*
    public double getDurabilityForDisplay(ItemStack itemStack) {
        return super.getDurabilityForDisplay(itemStack);
    }


    public int getRGBDurabilityForDisplay(ItemStack itemStack) {
        return super.getRGBDurabilityForDisplay(itemStack);
    }
     */
}
