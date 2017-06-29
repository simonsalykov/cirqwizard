package org.cirqoid.cnc.controller.serial;

import org.cirqoid.cnc.controller.commands.PacketParsingException;
import org.cirqoid.cnc.controller.commands.Response;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by simon on 13.06.17.
 */
public class ResponseTest
{
    @Test
    public void testParsing() throws PacketParsingException
    {
        byte[] packet = new byte[] {0x00, 0x00, 0x30, 0x39, // id = 12345
                0x00, 0x00, 0x00, 0x01, // code = 1 (executed)
                0x00, 0x00, 0x00, 0x00, // length = 4
                (byte)0xAC, (byte) 0xD4, 0x1B, 0x6E // crc
            };

        Response r = Response.parsePacket(packet);
        assertEquals(12345, r.getPacketId());
        assertEquals(Response.Code.EXECUTED, r.getCode());
    }

    @Test
    public void testCrcVerification()
    {
        byte[] packet = new byte[] {0x00, 0x00, 0x30, 0x39, // id = 12345
                0x00, 0x00, 0x00, 0x00, // code = 1 (executed)
                0x00, 0x00, 0x00, 0x04, // length = 0
                0x45, 0x07, 0x7E, 0x70 // crc
            };

        try
        {
            Response.parsePacket(packet);
            fail("CRC mismatch exception should have been thrown");
        }
        catch (PacketParsingException e)
        {
        }
    }

    @Test
    public void testLengthVerification()
    {
        byte[] packet = new byte[] {0x00, 0x00, 0x30, 0x39, // id = 12345
                0x00, 0x00, 0x00, 0x00, // code = 0 (ok)
                0x00, 0x00, 0x00, 0x05, // length = 5
                0x00, 0x00, 0x00, 0x01, // status = 1
                0x45, 0x06, 0x7E, 0x70 // crc
            };

        try
        {
            Response.parsePacket(packet);
            fail("Packet too short exception should have been thrown");
        }
        catch (PacketParsingException e)
        {
        }
    }

    @Test
    public void testResponseCodeVerification()
    {
        byte[] packet = new byte[] {0x00, 0x00, 0x30, 0x39, // id = 12345
                0x0F, 0x00, 0x00, 0x00, // code = 0x0F000000 (unknown)
                0x00, 0x00, 0x00, 0x04, // length = 4
                0x00, 0x00, 0x00, 0x01, // status = 1
                0x37, (byte) 0x92, (byte) 0xE1, 0x01 // crc
        };
        try
        {
            Response.parsePacket(packet);
            fail("Unknown response code exception should have been thrown");
        }
        catch (PacketParsingException e)
        {
        }
    }
}
