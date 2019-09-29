package se.mickelus.tetra.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import se.mickelus.tetra.TetraLogger;
import se.mickelus.tetra.TetraMod;

import java.util.ArrayList;
import java.util.function.Supplier;

/**
 * Packet pipeline class. Directs all registered packet data to be handled by the packets themselves. Based on the works
 * of sirgingalot
 */
public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel channel = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(TetraMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
            );
    private ArrayList<Class<? extends AbstractPacket>> packets = new ArrayList<>();

    public static PacketHandler instance;

    public PacketHandler() {
        instance = this;
    }

    /**
     * Register your packet with the pipeline. Discriminators are automatically set.
     *
     * @param packetClass the class to register
     * @param supplier A supplier returning an object instance of packetClass
     *
     * @return whether registration was successful. Failure may occur if 256 packets have been registered or if the registry already contains this packet
     */
    public <T extends AbstractPacket> boolean registerPacket(Class<T> packetClass, Supplier<T> supplier) {
        if (packets.size() > 256) {
            TetraLogger.log("Attempted to register packet but packet list is full: " + packetClass.toString());
            return false;
        }

        if (packets.contains(packetClass)) {
            TetraLogger.log("Attempted to register packet but packet is already in list: " + packetClass.toString());
            return false;
        }

        channel.messageBuilder(packetClass, packets.size())
                .encoder(AbstractPacket::toBytes)
                .decoder(buffer -> {
                    T packet = supplier.get();
                    packet.fromBytes(buffer);
                    return packet;
                })
                .consumer(this::onMessage)
                .add();

        packets.add(packetClass);
        return true;
    }

    public AbstractPacket onMessage(AbstractPacket message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity sender = ctx.get().getSender();
            message.handle(sender);
        });
        ctx.get().setPacketHandled(true);

        return null;
    }
    /**
     * Send this message to the specified player.
     *
     * @param message The message to send
     * @param player  The player to send it to
     */
    public static void sendTo(AbstractPacket message, ServerPlayerEntity player) {
        channel.sendTo(message, player.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
    }

    /**
     * Send this message to the server.
     *
     * @param message The message to send
     */
    public static void sendToServer(AbstractPacket message) {
        channel.sendToServer(message);
    }
}