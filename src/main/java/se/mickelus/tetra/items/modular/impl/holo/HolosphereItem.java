package se.mickelus.tetra.items.modular.impl.holo;

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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.TetraItemGroup;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.items.modular.impl.holo.gui.HoloGui;
import se.mickelus.tetra.items.modular.impl.holo.gui.scan.ScannerOverlayGui;

import javax.annotation.Nullable;
import java.util.List;

public class HolosphereItem extends ModularItem {

    // todo 1.16: rename
    private static final String unlocalizedName = "journal";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static HolosphereItem instance;

    public HolosphereItem() {
        super(new Properties()
                .maxStackSize(1)
                .group(TetraItemGroup.instance));

        canHone = false;

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
