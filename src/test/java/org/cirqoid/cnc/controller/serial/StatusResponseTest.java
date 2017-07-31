package org.cirqoid.cnc.controller.serial;

import org.cirqoid.cnc.controller.commands.PacketParsingException;
import org.cirqoid.cnc.controller.commands.Response;
import org.cirqoid.cnc.controller.commands.StatusResponse;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StatusResponseTest
{
    @Test
    public void testParsing() throws PacketParsingException
    {
        byte[] packet = new byte[] {0x00, 0x00, 0x30, 0x39, // id = 12345
                0x00, 0x00, 0x00, 0x03, // code = 3 (status)
                0x00, 0x00, 0x00, 0x14, // length = 20
                0x00, 0x00, 0x00, 0x01, // runLevel
                0x00, 0x00, 0x03, (byte)0xE8, // positions[0]
                0x00, 0x00, 0x07, (byte)0xD0, // positions[1]
                (byte)0xFF, (byte)0xFF, (byte)0xF4, 0x48, // positions[2]
                0x00, 0x00, 0x0F, (byte)0xA0, // positions[3]
                (byte)0x83, (byte) 0x08, 0x1F, (byte)0xED // crc
        };

        StatusResponse r = (StatusResponse) Response.parsePacket(packet);
        assertEquals(12345, r.getPacketId());
        assertEquals(1, r.getRunLevel());
        assertEquals(1000, r.getPositions()[0]);
        assertEquals(2000, r.getPositions()[1]);
        assertEquals(-3000, r.getPositions()[2]);
        assertEquals(4000, r.getPositions()[3]);
    }
}
