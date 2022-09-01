package se.mickelus.tetra.items.modular.impl.toolbelt.booster;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import se.mickelus.tetra.items.modular.impl.toolbelt.ToolbeltHelper;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class OverlayBooster implements IGuiOverlay {
    private final Minecraft mc;
    private final OverlayGuiBooster gui;

    public OverlayBooster(Minecraft mc) {
        this.mc = mc;
        gui = new OverlayGuiBooster(mc);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (TickEvent.Phase.END == event.phase && mc.player != null) {
            float fuelPercent = 0;
            ItemStack itemStack = ToolbeltHelper.findToolbelt(mc.player);
            if (UtilBooster.canBoost(itemStack)) {
                fuelPercent = UtilBooster.getFuelPercent(itemStack.getTag());
            }

            gui.setFuel(fuelPercent);
        }
    }

    @Override
    public void render(ForgeGui gui, PoseStack poseStack, float partialTick, int screenWidth, int screenHeight) {
        this.gui.draw(poseStack);
    }
}
