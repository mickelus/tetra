package se.mickelus.tetra.blocks.salvage;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import se.mickelus.mgui.gui.hud.GuiRootHud;
import se.mickelus.tetra.capabilities.CapabilityHelper;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class GuiCapabilityInteractiveOverlay extends GuiRootHud {


    public GuiCapabilityInteractiveOverlay() {

    }

    public void update(BlockState blockState, Direction face, PlayerEntity player, boolean transition) {
        if (blockState.getBlock() instanceof IBlockCapabilityInteractive) {
            IBlockCapabilityInteractive block = (IBlockCapabilityInteractive) blockState.getBlock();

            BlockInteraction[] interactions = block.getPotentialInteractions(blockState, face, CapabilityHelper.getPlayerCapabilities(player));

            if (transition) {
                Collection<GuiInteractiveOutline> previousOutlines = elements.stream()
                        .filter(element -> element instanceof GuiInteractiveOutline)
                        .map(element -> (GuiInteractiveOutline) element)
                        .collect(Collectors.toCollection(LinkedList::new));

                // animate out outlines for all interactions which are no longer possible
                previousOutlines.stream()
                        .filter(outline -> Arrays.stream(interactions)
                                .noneMatch(interaction -> interaction.equals(outline.getBlockInteraction())))
                        // todo: add methods for removing and listing children?
                        .forEach(outline -> outline.transitionOut(outline::remove));

                // add and animate outlines for all new interactions
                Arrays.stream(interactions)
                        .filter(interaction -> previousOutlines.stream()
                                .map(GuiInteractiveOutline::getBlockInteraction)
                                .noneMatch(interaction::equals))
                        .map(interaction -> new GuiInteractiveOutline(interaction, player))
                        .forEach(this::addChild);
            } else {
                clearChildren();

                Arrays.stream(interactions)
                        .map(interaction -> new GuiInteractiveOutline(interaction, player))
                        .forEach(this::addChild);

            }
        } else {
            clearChildren();
        }
    }
}
