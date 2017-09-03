package se.mickelus.tetra.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;

import java.io.IOException;


/**
 * AbstractPacket class. Should be the parent of all packets wishing to use the PacketPipeline.
 * @author sirgingalot, mickelus
 */
public abstract class AbstractPacket {

    /**
     * Encode the packet data into the ByteBuf stream. Complex data sets may need specific data handlers (See @link{cpw.mods.fml.common.network.ByteBuffUtils})
     *
     * @param ctx    channel context
     * @param buffer the buffer to encode into
     */
    public abstract void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer);

    /**
     * Decode the packet data from the ByteBuf stream. Complex data sets may need specific data handlers (See @link{cpw.mods.fml.common.network.ByteBuffUtils})
     *
     * @param ctx    channel context
     * @param buffer the buffer to decode from
     */
    public abstract void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer);

    /**
     * Handle a packet on the client side. Note this occurs after decoding has completed.
     *
     * @param player the player reference
     */
    public abstract void handleClientSide(EntityPlayer player);

    /**
     * Handle a packet on the server side. Note this occurs after decoding has completed.
     *
     * @param player the player reference
     */
    public abstract void handleServerSide(EntityPlayer player);

    /**
     * Utility method that reads a string from a buffer object.
     * @param buffer The buffer containing the string to be read.
     * @return A string read from the buffer
     * @throws IOException
     */
    protected static String readString(ByteBuf buffer) throws IOException {
        String string = "";
        char c = buffer.readChar();

        while(c != '\0') {
            string += c;
            c = buffer.readChar();
        }

        return string;
    }

    protected static void writeString(String string, ByteBuf buffer) throws IOException {
        for (int i = 0; i < string.length(); i++) {
            buffer.writeChar(string.charAt(i));
        }
        buffer.writeChar('\0');
    }
}