package org.cirqoid.cnc.controller.serial;

import org.cirqoid.cnc.controller.commands.HomeCommand;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by simon on 16.06.17.
 */
public class HomeCommandTest
{
    @Test
    public void testSerialize()
    {
        HomeCommand p = new HomeCommand(new int[]{1, 2, 3, 4});
        p.setId(12345);
        byte[] packet = p.serializePacket();

        assertEquals(34, packet.length);

        // Header
        assertEquals((byte)0xAA, packet[0]);
        assertEquals((byte)0xAA, packet[1]);

        // Id
        assertEquals(0x00, packet[2]);
        assertEquals(0x00, packet[3]);
        assertEquals(0x30, packet[4]);
        assertEquals(0x39, packet[5]);

        // Type
        assertEquals(0x00, packet[6]);
        assertEquals(0x00, packet[7]);
        assertEquals(0x00, packet[8]);
        assertEquals(0x03, packet[9]);

        // Length
        assertEquals(0x00, packet[10]);
        assertEquals(0x00, packet[11]);
        assertEquals(0x00, packet[12]);
        assertEquals(0x10, packet[13]);

        // parameters[0]
        assertEquals(0x00, packet[14]);
        assertEquals(0x00, packet[15]);
        assertEquals(0x00, packet[16]);
        assertEquals(0x01, packet[17]);

        // parameters[1]
        assertEquals(0x00, packet[18]);
        assertEquals(0x00, packet[19]);
        assertEquals(0x00, packet[20]);
        assertEquals(0x02, packet[21]);

        // parameters[2]
        assertEquals(0x00, packet[22]);
        assertEquals(0x00, packet[23]);
        assertEquals(0x00, packet[24]);
        assertEquals(0x03, packet[25]);

        // parameters[3]
        assertEquals(0x00, packet[26]);
        assertEquals(0x00, packet[27]);
        assertEquals(0x00, packet[28]);
        assertEquals(0x04, packet[29]);

        // CRC
        assertEquals((byte) 0x1B, packet[30]);
        assertEquals((byte) 0xAC, packet[31]);
        assertEquals((byte) 0xA2, packet[32]);
        assertEquals((byte) 0x7F, packet[33]);
    }

}
