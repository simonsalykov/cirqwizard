package org.cirqoid.cnc.controller.serial;

import org.cirqoid.cnc.controller.commands.PacketParsingException;
import org.cirqoid.cnc.controller.commands.Response;
import org.cirqoid.cnc.controller.commands.VersionResponse;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by simon on 23.06.17.
 */
public class VersionResponseTest
{
    @Test
    public void testParsing() throws PacketParsingException
    {
        byte[] packet = new byte[] {0x00, 0x00, 0x30, 0x39, // id = 12345
                0x00, 0x00, 0x00, 0x02, // code = 2 (version info)
                0x00, 0x00, 0x00, 0x08, // length = 8
                0x00, 0x00, 0x00, 0x01, // hardware = 1
                0x00, 0x00, 0x00, 0x02, // software = 2
                (byte)0x2C, (byte) 0x9E, 0x0F, 0x30 // crc
        };

        VersionResponse r = (VersionResponse) Response.parsePacket(packet);
        assertEquals(12345, r.getPacketId());
        assertEquals(1, r.getHardwareVersion());
        assertEquals(2, r.getSoftwareVersion());
    }
}
