package org.cirqoid.cnc.controller.commands;

import java.nio.ByteBuffer;
import java.util.zip.CRC32;

/**
 * Created by simon on 23.06.17.
 */
public class Response
{
    public enum Code
    {
        OK(0),
        EXECUTED(1),
        VERSION_INFO(2),

        PACKET_TOO_SHORT(101),
        CRC_MISMATCH(102),
        UNSUPPORTED_PACKET(103),

        INTERNAL_EXECUTION_EXCEPTION(201),
        AXIS_OVERTAVEL(202),
        COMMAND_TIMEOUT(203),
        NOT_INITIALIZED(204),
        NOT_HOMED(205)
        ;

        public int c;

        Code(int c)
        {
            this.c = c;
        }

        public boolean isExecutionError()
        {
            return c >= 200;
        }

        public static Code forValue(int c) throws PacketParsingException
        {
            for (Code code : values())
                if (code.c == c)
                    return code;
            throw new PacketParsingException("Unknown code: " + c);
        }
    }

    private int packetId;
    private Code code;

    public static synchronized Response parsePacket(byte[] packet) throws PacketParsingException
    {
        ByteBuffer b = ByteBuffer.wrap(packet);

        int id = b.getInt();
        int code = b.getInt();
        int length = b.getInt();
        if (length > packet.length - (4 + 4 + 4 + 4))
            throw new PacketParsingException("Packet length too short");
        byte[] payload = null;
        if (length > 0)
        {
            payload = new byte[length];
            b.get(payload);
        }
        int crc32 = b.getInt();
        CRC32 crc = new CRC32();
        crc.update(packet, 0, packet.length - 4);
        if (crc32 != (int)crc.getValue())
            throw new PacketParsingException("CRC check failed");

        Code c = Code.forValue(code);
        if (c == Code.VERSION_INFO)
            return new VersionResponse(id, payload);
        return new Response(id, c);
    }

    public Response(int packetId, Code code)
    {
        this.packetId = packetId;
        this.code = code;
    }

    public int getPacketId()
    {
        return packetId;
    }

    public Code getCode()
    {
        return code;
    }

    @Override
    public String toString()
    {
        return "Response{" +
                "packetId=" + packetId +
                ", code=" + code +
                '}';
    }
}
