package se.mickelus.tetra.items.cell;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.TetraItem;
import se.mickelus.tetra.items.TetraItemGroup;

import javax.annotation.Nullable;
import java.util.List;

import static se.mickelus.tetra.blocks.forged.ForgedBlockCommon.locationTooltip;

public class ItemCellMagmatic extends TetraItem {
    private static final String unlocalizedName = "magmatic_cell";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ItemCellMagmatic instance;

    private final String chargedPropKey = "tetra:charged";

    public static final int maxCharge = 128;

    public ItemCellMagmatic() {
        super(new Properties()
                .maxStackSize(1)
                .maxDamage(maxCharge)
                .group(TetraItemGroup.instance));

        setRegistryName(unlocalizedName);
    }

    @Override
    public void clientInit() {
        ItemModelsProperties.func_239418_a_(this, new ResourceLocation(chargedPropKey), (itemStack, world, livingEntity) -> getCharge(itemStack) > 0 ? 1 : 0);
    }

    @Override
    public void addInformation(final ItemStack stack, @Nullable final World world, final List<ITextComponent> tooltip, final ITooltipFlag advanced) {
        int charge = getCharge(stack);

        TextComponent chargeLine = new TranslationTextComponent("item.tetra.magmatic_cell.charge");

        if (charge == maxCharge) {
            chargeLine.append(new TranslationTextComponent("item.tetra.magmatic_cell.charge_full"));
        } else if (charge > maxCharge * 0.4) {
            chargeLine.append(new TranslationTextComponent("item.tetra.magmatic_cell.charge_good"));
        } else if (charge > 0) {
            chargeLine.append(new TranslationTextComponent("item.tetra.magmatic_cell.charge_low"));
        } else {
            chargeLine.append(new TranslationTextComponent("item.tetra.magmatic_cell.charge_empty"));
        }

        tooltip.add(chargeLine);
        tooltip.add(new StringTextComponent(" "));
        tooltip.add(locationTooltip);
    }

    @Override
    public void fillItemGroup(final ItemGroup itemGroup, final NonNullList<ItemStack> itemList) {
        if (isInGroup(itemGroup)) {
            itemList.add(new ItemStack(this));

            ItemStack emptyStack = new ItemStack(this);
            emptyStack.setDamage(maxCharge);
            itemList.add(emptyStack);
        }
    }

    public int getCharge(ItemStack itemStack) {
        return itemStack.getMaxDamage() - itemStack.getDamage();
    }

    public int drainCharge(ItemStack itemStack, int amount) {
        if (itemStack.getDamage() + amount < itemStack.getMaxDamage()) {
            setDamage(itemStack, itemStack.getDamage() + amount);
            return amount;
        }

        int actualAmount = itemStack.getMaxDamage() - itemStack.getDamage();
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

    public double getDurabilityForDisplay(ItemStack itemStack) {
        return super.getDurabilityForDisplay(itemStack);
    }


    public int getRGBDurabilityForDisplay(ItemStack itemStack) {
        return super.getRGBDurabilityForDisplay(itemStack);
    }
}
