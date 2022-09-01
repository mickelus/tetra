package se.mickelus.tetra.items.modular.impl.crossbow;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import se.mickelus.tetra.items.modular.impl.bow.GuiRangedProgress;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class CrossbowOverlay implements IGuiOverlay {
    private final Minecraft mc;
    private final GuiRangedProgress gui;

    public CrossbowOverlay(Minecraft mc) {
        this.mc = mc;
        gui = new GuiRangedProgress(mc);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (TickEvent.Phase.END == event.phase && mc.player != null) {
            ItemStack activeStack = mc.player.getUseItem();

            if (activeStack.getItem() instanceof ModularCrossbowItem) {
                ModularCrossbowItem item = (ModularCrossbowItem) activeStack.getItem();
                this.gui.setProgress(item.getProgress(activeStack, mc.player), 0);
            } else {
                this.gui.setProgress(0, 0);
            }
        }
    }

    @Override
    public void render(ForgeGui gui, PoseStack poseStack, float partialTick, int screenWidth, int screenHeight) {
        this.gui.draw(poseStack);
    }
}
