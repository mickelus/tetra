package se.mickelus.tetra.items.cell;

import net.minecraft.util.text.TextFormatting;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.TetraItemGroup;
import se.mickelus.tetra.items.TetraItem;

import javax.annotation.Nullable;
import java.util.List;

public class ItemCellMagmatic extends TetraItem {

    private static final String unlocalizedName = "magmatic_cell";
    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ItemCellMagmatic instance;

    private final String chargedPropKey = "tetra:charged";

    public static final int maxCharge = 128;

    public ItemCellMagmatic() {
        super(new Item.Properties()
                .group(TetraItemGroup.getInstance())
                .maxStackSize(1)
                .maxDamage(maxCharge)
                .setNoRepair());

        setRegistryName(unlocalizedName);

        this.addPropertyOverride(new ResourceLocation(chargedPropKey), new IItemPropertyGetter() {
            @OnlyIn(Dist.CLIENT)
            public float call(@Nullable ItemStack itemStack, @Nullable World world, @Nullable LivingEntity livingEntity) {
                return getCharge(itemStack) > 0 ? 1 : 0;
            }
        });
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        int charge = getCharge(stack);

        if (charge == maxCharge) {
            tooltip.add(new TranslationTextComponent("item.magmatic_cell.charge", I18n.format("item.magmatic_cell.charge_full")));
        } else if (charge > maxCharge * 0.4) {

            tooltip.add(new TranslationTextComponent("item.magmatic_cell.charge", I18n.format("item.magmatic_cell.charge_good")));
        } else if (charge > 0) {
            tooltip.add(new TranslationTextComponent("item.magmatic_cell.charge", I18n.format("item.magmatic_cell.charge_low")));
        } else {
            tooltip.add(new TranslationTextComponent("item.magmatic_cell.charge", I18n.format("item.magmatic_cell.charge_empty")));
        }

        tooltip.add(new TranslationTextComponent("forged_description").setStyle(new Style().setColor(TextFormatting.DARK_GRAY)));
    }

    @Override
    public void fillItemGroup(ItemGroup itemGroup, NonNullList<ItemStack> itemList) {
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

    public void setCharge(ItemStack itemStack, int charge) {
        setDamage(itemStack, itemStack.getMaxDamage() - charge);
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
