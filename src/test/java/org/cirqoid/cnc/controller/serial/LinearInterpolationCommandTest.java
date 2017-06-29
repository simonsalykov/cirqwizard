package org.cirqoid.cnc.controller.serial;

import org.cirqoid.cnc.controller.commands.LinearInterpolationCommand;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by simon on 19.06.17.
 */
public class LinearInterpolationCommandTest
{

    @Test
    public void testSerialization()
    {
        LinearInterpolationCommand command = new LinearInterpolationCommand(new int[4],
                new int[]{1, 2, 3, 4}, 1000000);
        command.setId(12345);
        command.setMaxExitSpeed(3000);
        byte[] p = command.serializePacket();
        assertEquals(42, p.length);

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
        assertEquals(0x04, p[9]);

        // Length
        assertEquals(0x00, p[10]);
        assertEquals(0x00, p[11]);
        assertEquals(0x00, p[12]);
        assertEquals(0x18, p[13]);

        // positions[0]
        assertEquals((byte) 0x00, p[14]);
        assertEquals((byte) 0x00, p[15]);
        assertEquals((byte) 0x00, p[16]);
        assertEquals((byte) 0x01, p[17]);

        // positions[1]
        assertEquals((byte) 0x00, p[18]);
        assertEquals((byte) 0x00, p[19]);
        assertEquals((byte) 0x00, p[20]);
        assertEquals((byte) 0x02, p[21]);

        // positions[2]
        assertEquals((byte) 0x00, p[22]);
        assertEquals((byte) 0x00, p[23]);
        assertEquals((byte) 0x00, p[24]);
        assertEquals((byte) 0x03, p[25]);

        // positions[3]
        assertEquals((byte) 0x00, p[26]);
        assertEquals((byte) 0x00, p[27]);
        assertEquals((byte) 0x00, p[28]);
        assertEquals((byte) 0x04, p[29]);

        // feed
        assertEquals((byte) 0x00, p[30]);
        assertEquals((byte) 0x0F, p[31]);
        assertEquals((byte) 0x42, p[32]);
        assertEquals((byte) 0x40, p[33]);

        // maxExitSpeed
        assertEquals((byte) 0x00, p[34]);
        assertEquals((byte) 0x00, p[35]);
        assertEquals((byte) 0x0B, p[36]);
        assertEquals((byte) 0xB8, p[37]);

        // CRC
        assertEquals((byte) 0xAD, p[38]);
        assertEquals((byte) 0x80, p[39]);
        assertEquals((byte) 0x12, p[40]);
        assertEquals((byte) 0xAC, p[41]);
    }

}
