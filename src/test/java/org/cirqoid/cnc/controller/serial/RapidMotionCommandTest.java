package org.cirqoid.cnc.controller.serial;

import org.cirqoid.cnc.controller.commands.RapidMotionCommand;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by simon on 14.06.17.
 */
public class RapidMotionCommandTest
{

    @Test
    public void testSerialization()
    {
        RapidMotionCommand packet = new RapidMotionCommand(new int[]{1000, -2000, 3000, -4000});
        packet.setId(12345);
        byte[] p = packet.serializePacket();

        assertEquals(34, p.length);
        // Header
        assertEquals((byte)0xAA, p[0]);
        assertEquals((byte)0xAA, p[1]);

        // Id
        assertEquals(0x00, p[2]);
        assertEquals(0x00, p[3]);
        assertEquals(0x30, p[4]);
        assertEquals(0x39, p[5]);

        // Type
        assertEquals(0x00, p[6]);
        assertEquals(0x00, p[7]);
        assertEquals(0x00, p[8]);
        assertEquals(0x02, p[9]);

        // Length
        assertEquals(0x00, p[10]);
        assertEquals(0x00, p[11]);
        assertEquals(0x00, p[12]);
        assertEquals(0x10, p[13]);

        // positions[0]
        assertEquals((byte) 0x00, p[14]);
        assertEquals((byte) 0x00, p[15]);
        assertEquals((byte) 0x03, p[16]);
        assertEquals((byte) 0xE8, p[17]);

        // positions[1]
        assertEquals((byte) 0xFF, p[18]);
        assertEquals((byte) 0xFF, p[19]);
        assertEquals((byte) 0xF8, p[20]);
        assertEquals((byte) 0x30, p[21]);

        // positions[2]
        assertEquals((byte) 0x00, p[22]);
        assertEquals((byte) 0x00, p[23]);
        assertEquals((byte) 0x0B, p[24]);
        assertEquals((byte) 0xB8, p[25]);

        // positions[3]
        assertEquals((byte) 0xFF, p[26]);
        assertEquals((byte) 0xFF, p[27]);
        assertEquals((byte) 0xF0, p[28]);
        assertEquals((byte) 0x60, p[29]);

        // CRC
        assertEquals((byte) 0xCD, p[30]);
        assertEquals((byte) 0x4B, p[31]);
        assertEquals((byte) 0x88, p[32]);
        assertEquals((byte) 0x4B, p[33]);
    }

}
