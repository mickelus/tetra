package se.mickelus.tetra.items.forged;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.TetraCreativeTabs;
import se.mickelus.tetra.items.TetraItem;

import javax.annotation.Nullable;
import java.util.List;

public class ItemBolt extends TetraItem {
    private static final String unlocalizedName = "forged_bolt";
    @GameRegistry.ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ItemBolt instance;

    public ItemBolt() {
        setRegistryName(unlocalizedName);
        setUnlocalizedName(unlocalizedName);
        setCreativeTab(TetraCreativeTabs.getInstance());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
        tooltip.add(ChatFormatting.DARK_GRAY + I18n.format("forged_description"));
    }
}
