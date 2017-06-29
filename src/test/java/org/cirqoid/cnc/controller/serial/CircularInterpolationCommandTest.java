package org.cirqoid.cnc.controller.serial;

import org.cirqoid.cnc.controller.commands.CircularInterpolationCommand;
import org.cirqoid.cnc.controller.interpreter.Context;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by simon on 19.06.17.
 */
public class CircularInterpolationCommandTest
{

    @Test
    public void testSerialization()
    {
        CircularInterpolationCommand packet = new CircularInterpolationCommand(new int[]{1, 2, 3, 4},
                100, new int[]{5, 6}, Context.Plane.XY, true, 1000000);
        packet.setId(12345);
        byte[] p = packet.serializePacket();
        assertEquals(58, p.length);

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
        assertEquals(0x05, p[9]);

        // Length
        assertEquals(0x00, p[10]);
        assertEquals(0x00, p[11]);
        assertEquals(0x00, p[12]);
        assertEquals(0x28, p[13]);

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

        // radius
        assertEquals((byte) 0x00, p[30]);
        assertEquals((byte) 0x00, p[31]);
        assertEquals((byte) 0x00, p[32]);
        assertEquals((byte) 0x64, p[33]);

        // centerCoordinates[0]
        assertEquals((byte) 0x00, p[34]);
        assertEquals((byte) 0x00, p[35]);
        assertEquals((byte) 0x00, p[36]);
        assertEquals((byte) 0x05, p[37]);

        // centerCoordinates[1]
        assertEquals((byte) 0x00, p[38]);
        assertEquals((byte) 0x00, p[38]);
        assertEquals((byte) 0x00, p[40]);
        assertEquals((byte) 0x06, p[41]);

        // plane
        assertEquals((byte) 0x00, p[42]);
        assertEquals((byte) 0x00, p[43]);
        assertEquals((byte) 0x00, p[44]);
        assertEquals((byte) 0x01, p[45]);

        // clockwise
        assertEquals((byte) 0x00, p[46]);
        assertEquals((byte) 0x00, p[47]);
        assertEquals((byte) 0x00, p[48]);
        assertEquals((byte) 0x01, p[49]);

        // feed
        assertEquals((byte) 0x00, p[50]);
        assertEquals((byte) 0x0F, p[51]);
        assertEquals((byte) 0x42, p[52]);
        assertEquals((byte) 0x40, p[53]);

        // CRC
        assertEquals((byte) 0x39, p[54]);
        assertEquals((byte) 0x78, p[55]);
        assertEquals((byte) 0xC8, p[56]);
        assertEquals((byte) 0x6B, p[57]);
    }

}
