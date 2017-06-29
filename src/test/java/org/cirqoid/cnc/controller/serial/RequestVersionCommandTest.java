package org.cirqoid.cnc.controller.serial;

import org.cirqoid.cnc.controller.commands.RequestVersionCommand;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by simon on 23.06.17.
 */
public class RequestVersionCommandTest
{

    @Test
    public void testSerialization()
    {
        RequestVersionCommand command = new RequestVersionCommand();
        command.setId(12345);
        byte[] packet = command.serializePacket();
        assertEquals(18, packet.length);

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
        assertEquals(0x06, packet[9]);

        // Length
        assertEquals(0x00, packet[10]);
        assertEquals(0x00, packet[11]);
        assertEquals(0x00, packet[12]);
        assertEquals(0x00, packet[13]);

        // CRC
        assertEquals((byte) 0x1E, packet[14]);
        assertEquals((byte) 0xF4, packet[15]);
        assertEquals((byte)0xC7, packet[16]);
        assertEquals((byte)0x7E, packet[17]);
    }

}
