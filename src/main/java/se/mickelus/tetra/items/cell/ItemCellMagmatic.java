package se.mickelus.tetra.items.cell;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.TetraCreativeTabs;
import se.mickelus.tetra.items.TetraItem;

import javax.annotation.Nullable;
import java.util.List;

public class ItemCellMagmatic extends TetraItem {

    private static final String unlocalizedName = "magmatic_cell";
    @GameRegistry.ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ItemCellMagmatic instance;

    private final String chargedPropKey = "tetra:charged";

    public static final int maxCharge = 128;

    public ItemCellMagmatic() {
        setRegistryName(unlocalizedName);
        setUnlocalizedName(unlocalizedName);
        setCreativeTab(TetraCreativeTabs.getInstance());
        setMaxDamage(maxCharge);
        setMaxStackSize(1);

        this.addPropertyOverride(new ResourceLocation(chargedPropKey), new IItemPropertyGetter() {
            @SideOnly(Side.CLIENT)
            public float apply(ItemStack itemStack, @Nullable World worldIn, @Nullable LivingEntity entityIn) {
                return getCharge(itemStack) > 0 ? 1 : 0;
            }
        });
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        int charge = getCharge(stack);

        if (charge == maxCharge) {
            tooltip.add(I18n.format("item.magmatic_cell.charge", I18n.format("item.magmatic_cell.charge_full")));
        } else if (charge > maxCharge * 0.4) {

            tooltip.add(I18n.format("item.magmatic_cell.charge", I18n.format("item.magmatic_cell.charge_good")));
        } else if (charge > 0) {
            tooltip.add(I18n.format("item.magmatic_cell.charge", I18n.format("item.magmatic_cell.charge_low")));
        } else {
            tooltip.add(I18n.format("item.magmatic_cell.charge", I18n.format("item.magmatic_cell.charge_empty")));
        }

        tooltip.add(ChatFormatting.DARK_GRAY + I18n.format("forged_description"));
    }

    @Override
    public void getSubItems(CreativeTabs creativeTabs, NonNullList<ItemStack> itemList) {
        if (isInCreativeTab(creativeTabs)) {
            itemList.add(new ItemStack(this));

            ItemStack emptyStack = new ItemStack(this);
            emptyStack.setItemDamage(maxCharge);
            itemList.add(emptyStack);
        }
    }

    public int getCharge(ItemStack itemStack) {
        return itemStack.getMaxDamage() - itemStack.getItemDamage();
    }

    public int drainCharge(ItemStack itemStack, int amount) {
        if (itemStack.getItemDamage() + amount < itemStack.getMaxDamage()) {
            setDamage(itemStack, itemStack.getItemDamage() + amount);
            return amount;
        }

        int actualAmount = itemStack.getMaxDamage() - itemStack.getItemDamage();
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
