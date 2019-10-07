package se.mickelus.tetra.items.journal;

import net.minecraft.util.text.TextFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.TetraItemGroup;
import se.mickelus.tetra.items.TetraItem;
import se.mickelus.tetra.items.journal.gui.GuiJournal;

import javax.annotation.Nullable;
import java.util.List;

public class ItemJournal extends TetraItem {

    private static final String unlocalizedName = "journal";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ItemJournal instance;

    public ItemJournal() {
        super();

        setRegistryName(unlocalizedName);
        setUnlocalizedName(unlocalizedName);

        setMaxStackSize(1);

        setCreativeTab(TetraItemGroup.getInstance());
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(TextFormatting.DARK_GRAY + I18n.format("item.journal.tooltip1"));
        tooltip.add("");
        tooltip.add(TextFormatting.GRAY + TextFormatting.ITALIC.toString() + I18n.format("item.journal.tooltip2"));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        if (world.isRemote) {
            showGui();
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @OnlyIn(Dist.CLIENT)
    private void showGui() {
        GuiJournal gui = GuiJournal.getInstance();

        Minecraft.getInstance().displayGuiScreen(gui);
        gui.onShow();
    }
    
}
