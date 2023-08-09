package se.mickelus.tetra.interactions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import se.mickelus.tetra.TetraMod;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SecondaryInteractionHandler {
    private static Map<String, SecondaryInteraction> interactions = new HashMap<>();

    public static void registerInteraction(SecondaryInteraction interaction) {
        interactions.put(interaction.getKey(), interaction);
    }

    public static SecondaryInteraction getInteraction(String key) {
        return interactions.get(key);
    }

    public static Collection<SecondaryInteraction> getInteractions() {
        return interactions.values();
    }

    public static SecondaryInteraction findRelevantAction(Player player, BlockPos pos, Entity target) {
        return interactions.values().stream()
                .filter(interaction -> interaction.canPerform(player, player.level(), pos, target))
                .findFirst()
                .orElse(null);
    }

    public static void dispatchInteraction(SecondaryInteraction interaction, Player player, BlockPos pos, Entity target) {
        if (interaction.getPerformSide().runClient()) {
            interaction.perform(player, player.level(), pos, target);
        }

        if (interaction.getPerformSide().runServer()) {
            TetraMod.packetHandler.sendToServer(new SecondaryInteractionPacket(interaction.getKey(), pos, target));
        }
    }
}
