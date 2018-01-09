package org.cirqoid.cnc.controller.serial;

import org.cirqoid.cnc.controller.commands.PacketParsingException;
import org.cirqoid.cnc.controller.commands.Response;
import org.cirqwizard.logging.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.logging.Level;

public class SerialBuffer
{
    private final static int MAX_PACKET_SIZE = 2048;

    private byte buffer[] = new byte[MAX_PACKET_SIZE];
    private int bufferPointer = 0;

    public void addBytes(byte[] b)
    {
        if (b == null)
            return;
        if (bufferPointer + b.length < MAX_PACKET_SIZE)
        {
            System.arraycopy(b, 0, buffer, bufferPointer, b.length);
            bufferPointer += b.length;
        }
    }

    public Response parseBuffer()
    {
        int i = 0;
        // TODO: check for double AA
        while (buffer[i] != (byte)0xAA && i < bufferPointer)
            i++;
        if (i > 0)
        {
            LoggerFactory.getSerialLogger().log(Level.INFO, "Skipping " + i + " bytes...");
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < i; j++)
                sb.append(String.format("%02x ", buffer[j]));
            LoggerFactory.getSerialLogger().log(Level.INFO, sb.toString());
            System.arraycopy(buffer, i, buffer, 0, bufferPointer - i);
            bufferPointer -= i;
        }
        if (bufferPointer < 14)
            return null;
        ByteBuffer b = ByteBuffer.wrap(buffer, 10, 4);
        int expectedLength = b.getInt() + 2 + 4 + 4 + 4 + 4;
        if (expectedLength > MAX_PACKET_SIZE)
        {
            bufferPointer = 0;
            return null;
        }
        if (bufferPointer < expectedLength)
            return null;

        byte[] packet = new byte[expectedLength - 2];
        System.arraycopy(buffer, 2, packet, 0, expectedLength - 2);
        try
        {
            Response parsed = Response.parsePacket(packet);
            System.arraycopy(buffer, expectedLength, buffer, 0, bufferPointer - expectedLength);
            bufferPointer -= expectedLength;
            return parsed;
        }
        catch (PacketParsingException e)
        {
            System.out.println("Parsing failed: " + e.getMessage());
            System.arraycopy(buffer, bufferPointer, buffer, 0, buffer.length - bufferPointer);
            bufferPointer = 0;
        }
        return null;
    }
}
