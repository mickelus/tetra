package se.mickelus.tetra.items;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.items.modular.impl.holo.gui.HoloGui;

import javax.annotation.Nullable;
import java.util.List;

// todo 1.16: remove together with model json & texture
@Deprecated
public class JournalItem extends TetraItem {

    private static final String unlocalizedName = "journal";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static JournalItem instance;

    public JournalItem() {
        super(new Properties()
                .maxStackSize(1)
                .group(TetraItemGroup.instance));

        setRegistryName(unlocalizedName);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        tooltip.add(new TranslationTextComponent("item.tetra.holo.tooltip1")
                .setStyle(new Style().setColor(TextFormatting.GRAY)));
        tooltip.add(new StringTextComponent(""));
        tooltip.add(new TranslationTextComponent("item.tetra.holo.tooltip2")
                .setStyle(new Style().setColor(TextFormatting.GRAY).setItalic(true)));
    }
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        if (world.isRemote) {
            showGui();
        }

        return new ActionResult<>(ActionResultType.SUCCESS, player.getHeldItem(hand));
    }

    @OnlyIn(Dist.CLIENT)
    private void showGui() {
        HoloGui gui = HoloGui.getInstance();

        Minecraft.getInstance().displayGuiScreen(gui);
        gui.onShow();
    }
    
}
