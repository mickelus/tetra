package se.mickelus.tetra.network;

import java.util.*;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.FMLOutboundHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import se.mickelus.tetra.TetraLogger;
import se.mickelus.tetra.TetraMod;

/**
 * Packet pipeline class. Directs all registered packet data to be handled by the packets themselves. Based on the works
 * of sirgingalot
 */
public class PacketHandler implements IMessageHandler<AbstractPacket, AbstractPacket> {

    public static final SimpleNetworkWrapper channel = NetworkRegistry.INSTANCE.newSimpleChannel(TetraMod.MOD_ID);
    private ArrayList<Class<? extends AbstractPacket>> packets = new ArrayList<>();

    public static PacketHandler instance;

    public PacketHandler() {
        instance = this;
    }

    /**
     * Register your packet with the pipeline. Discriminators are automatically set.
     *
     * @param packetClass the class to register
     * @param side The side that the handler is supposed to handle packages on (the side they are sent to, not from)
     *
     * @return whether registration was successful. Failure may occur if 256 packets have been registered or if the registry already contains this packet
     */
    public boolean registerPacket(Class<? extends AbstractPacket> packetClass, Side side) {
        if (packets.size() > 256) {
            TetraLogger.log("Attempted to register packet but packet list is full: " + packetClass.toString());
            return false;
        }

        if (packets.contains(packetClass)) {
            TetraLogger.log("Attempted to register packet but packet is already in list: " + packetClass.toString());
            return false;
        }

        channel.registerMessage(this, packetClass, (byte) packets.size(), side);
        packets.add(packetClass);
        return true;
    }

    @Override
    public AbstractPacket onMessage(AbstractPacket message, MessageContext ctx) {
        if (ctx.side.equals(Side.CLIENT)) {
            onMessageClient(message);
        } else {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> message.handle(player));
        }

        return null;
    }

    // breaking this out helps to avoid NoClassDefFoundError crashes on the server :)
    @SideOnly(Side.CLIENT)
    private void onMessageClient(AbstractPacket message) {
        Minecraft.getMinecraft().addScheduledTask(() -> message.handle(Minecraft.getMinecraft().player));
    }

    /**
     * Send this message to everyone.
     *
     * @param message The message to send
     */
    public static void sendToAll(AbstractPacket message) {
        channel.sendToAll(message);
    }

    /**
     * Send this message to the specified player.
     *
     * @param message The message to send
     * @param player  The player to send it to
     */
    public static void sendTo(AbstractPacket message, EntityPlayerMP player) {
        channel.sendTo(message, player);
    }

    /**
     * Send this message to everyone within a certain range of a point.
     *
     * @param message The message to send
     * @param point   The coordinate around which to send the message
     */
    public static void sendToAllAround(AbstractPacket message, NetworkRegistry.TargetPoint point) {
        channel.sendToAllAround(message, point);
    }

    /**
     * Send this message to everyone within the supplied dimension.
     *
     * @param message     The message to send
     * @param dimensionId The dimension id to target
     */
    public static void sendToDimension(AbstractPacket message, int dimensionId) {
        channel.sendToDimension(message, dimensionId);
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