package se.mickelus.tetra.interactions;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import se.mickelus.mutil.gui.GuiAttachment;
import se.mickelus.mutil.gui.GuiRoot;
import se.mickelus.tetra.client.keymap.TetraKeyMappings;

public class SecondaryInteractionOverlay extends GuiRoot implements IGuiOverlay {
    SecondaryInteraction currentInteraction;
    SecondaryInteractionGui currentDisplay;
    boolean wasKeyDown = false;

    public SecondaryInteractionOverlay(Minecraft minecraft) {
        super(minecraft);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event) {
        if (!TetraKeyMappings.secondaryUseBinding.isDown() && wasKeyDown) {
            BlockPos blockPos = mc.hitResult.getType() == HitResult.Type.BLOCK ? ((BlockHitResult) mc.hitResult).getBlockPos() : null;
            Entity entity = mc.hitResult.getType() == HitResult.Type.ENTITY ? ((EntityHitResult) mc.hitResult).getEntity() : null;

            updateCurrentInteraction(blockPos, entity);

            if (currentInteraction != null) {
                SecondaryInteractionHandler.dispatchInteraction(currentInteraction, mc.player, blockPos, entity);
            }
        }

        wasKeyDown = TetraKeyMappings.secondaryUseBinding.isDown();
    }

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        this.draw(guiGraphics);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (TickEvent.Phase.END == event.phase && (mc.level != null && mc.level.getGameTime() % 10 == 0)) {
            updateCurrentInteraction(mc.hitResult.getType() == HitResult.Type.BLOCK ? ((BlockHitResult) mc.hitResult).getBlockPos() : null,
                    mc.hitResult.getType() == HitResult.Type.ENTITY ? ((EntityHitResult) mc.hitResult).getEntity() : null);
        }
    }

    private void updateCurrentInteraction(BlockPos pos, Entity target) {
        if (mc.player == null) {
            return;
        }

        SecondaryInteraction newInteraction = SecondaryInteractionHandler.findRelevantAction(mc.player, pos, target);

        boolean changed = newInteraction != currentInteraction;
        if (currentInteraction != null && changed) {
            if (currentDisplay != null) {
                currentDisplay.hide();
            }
            currentInteraction = null;
        }

        if (newInteraction != null && changed) {
            currentInteraction = newInteraction;
            int offset = Math.min((int) (-mc.getWindow().getGuiScaledWidth() * 0.3), -120);
            currentDisplay = new SecondaryInteractionGui(offset, -1, currentInteraction);
            currentDisplay.setAttachmentPoint(GuiAttachment.middleLeft);
            currentDisplay.setAttachmentAnchor(GuiAttachment.middleRight);
            currentDisplay.show();

            addChild(currentDisplay);
        }
    }
}
