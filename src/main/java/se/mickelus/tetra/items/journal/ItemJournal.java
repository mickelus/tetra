package se.mickelus.tetra.items.journal;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.TetraCreativeTabs;
import se.mickelus.tetra.items.TetraItem;
import se.mickelus.tetra.items.journal.gui.GuiJournal;

import javax.annotation.Nullable;
import java.util.List;

public class ItemJournal extends TetraItem {

    private static final String unlocalizedName = "journal";

    @GameRegistry.ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ItemJournal instance;

    public ItemJournal() {
        super();

        setRegistryName(unlocalizedName);
        setUnlocalizedName(unlocalizedName);

        setMaxStackSize(1);

        setCreativeTab(TetraCreativeTabs.getInstance());
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(ChatFormatting.DARK_GRAY + I18n.format("item.journal.tooltip1"));
        tooltip.add("");
        tooltip.add(ChatFormatting.GRAY + ChatFormatting.ITALIC.toString() + I18n.format("item.journal.tooltip2"));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (world.isRemote) {
            showGui();
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @SideOnly(Side.CLIENT)
    private void showGui() {
        GuiJournal gui = GuiJournal.getInstance();

        Minecraft.getMinecraft().displayGuiScreen(gui);
        gui.onShow();
    }
    
}
