package se.mickelus.tetra.module.improvement;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public class ProgressionHelper {
    public static void showHoneToastClient(ItemStack itemStack) {
        Minecraft.getMinecraft().getToastGui().add(new HoneToast(itemStack));
    }
    public static void showSettleToastClient(ItemStack itemStack, String slot) {
        Minecraft.getMinecraft().getToastGui().add(new SettleToast(itemStack, slot));
    }
}
