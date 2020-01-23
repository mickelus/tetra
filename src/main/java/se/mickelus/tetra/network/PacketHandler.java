package se.mickelus.tetra.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.tetra.TetraMod;

import java.util.ArrayList;
import java.util.function.Supplier;

/**
 * Packet pipeline class. Directs all registered packet data to be handled by the packets themselves. Based on the works
 * of sirgingalot
 */
public class PacketHandler {
    private static final Logger logger = LogManager.getLogger();

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
            logger.warn("Attempted to register packet but packet list is full: " + packetClass.toString());
            return false;
        }

        if (packets.contains(packetClass)) {
            logger.warn("Attempted to register packet but packet is already in list: " + packetClass.toString());
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
            if (ctx.get().getDirection().getReceptionSide().isServer()) {
                message.handle(ctx.get().getSender());
            } else {
                message.handle(TetraMod.proxy.getClientPlayer());
            }
        });
        ctx.get().setPacketHandled(true);

        return null;
    }

    public static void sendTo(AbstractPacket message, ServerPlayerEntity player) {
        channel.sendTo(message, player.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendToAllPlayers(AbstractPacket message) {
        channel.send(PacketDistributor.ALL.noArg(), message);
    }

    public static void sendToAllPlayersNear(AbstractPacket message, BlockPos pos, double r2, DimensionType dim) {
        channel.send(PacketDistributor.NEAR.with(PacketDistributor.TargetPoint.p(pos.getX(), pos.getY(), pos.getZ(), r2, dim)), message);
    }

    public static void sendToServer(AbstractPacket message) {
        channel.sendToServer(message);
    }
}